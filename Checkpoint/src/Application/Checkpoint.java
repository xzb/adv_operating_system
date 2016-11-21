package Application;

import Tool.Parser;
import Tool.SocketManager;

import java.util.*;

/**
 * Created by xiezebin on 11/12/16.
 */
public class Checkpoint {

    private Node obNode;

    private int sequenceNum;
    //private boolean freezeSendFlag;          // todo use static lock
    private boolean isFreezeComplete;

    private boolean willingToCheckpoint;
    private Set<Integer> currentCohort;
    private Set<Integer> replyFromCohort;
    private boolean initiatorFlag;
    private Set<Integer> requestSet;

    private static Map<Integer, Checkpoint> obInstances;
    public static Checkpoint ins(int nid)
    {
        if (obInstances == null)
        {
            obInstances = new HashMap<Integer, Checkpoint>();
        }
        if (!obInstances.containsKey(nid))
        {
            Checkpoint ckpt = new Checkpoint(nid);
            obInstances.put(nid, ckpt);
        }
        return obInstances.get(nid);
    }
    private Checkpoint(int nid)
    {
        obNode = Node.getNode(nid);
        sequenceNum = 0;
        willingToCheckpoint = true;
        currentCohort = new HashSet<Integer>();
        replyFromCohort = new HashSet<Integer>();
        requestSet = new HashSet<Integer>();
    }
    public boolean isFreeze()
    {
        //return freezeSendFlag || isFreezeComplete;
        return isFreezeComplete;
    }


    /**
     * First phase
     * Checkpoint
     * If LLR is not null, send checkpoint message to neighbor id, piggyback llr
     */
    public void initiateCheckpoint()
    {

        initiatorFlag = true;
        isFreezeComplete = true;
        takeTentativeCheckpointAndRequestCohorts(obNode.id);

        // base case
        if (currentCohort.size() == 0)
        {
            unFreeze();
        }
    }

    /**
     * Logic: when receive checkpoint message from p, check whether should take checkpoint;
     *        If should take, broadcast request to cohorts (need calculate);
     *          If cohorts is not empty, wait to receive REPLY message.
     *          If cohorts is empty, directly REPLY to p.
     *        If need not take, directly REPLY to p.
     * @param fromNodeId
     * @param llr
     */
    public void receiveCheckpoint(int fromNodeId, int llr)
    {
        requestSet.add(fromNodeId);
        isFreezeComplete = true;
        int fls = obNode.FLS[fromNodeId];
        if (willingToCheckpoint && fls > 0 && llr >= fls)
        {
            // todo forward checkpoint message, how to exclude initiator?


            takeTentativeCheckpointAndRequestCohorts(fromNodeId);
        }


        // todo check checkpoint already taken by sequenceNum
        // if cohorts is empty, or need not take checkpoint, directly REPLY
        if (currentCohort.size() == 0 || (willingToCheckpoint && fls == 0))
        {
            Node fromNode = Node.getNode(fromNodeId);
            SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.FREEZE_REPLY.getT());
            requestSet.remove(fromNodeId);      // prevent duplicate reply
        }

    }

    public void takeTentativeCheckpointAndRequestCohorts(int excludeNodeId)
    {
        // mark tentative checkpoint
        sequenceNum++;
        int[] cpClock = new int[Parser.numNodes];
        System.arraycopy(obNode.clock, 0, cpClock, 0, Parser.numNodes);
        obNode.checkpoints.add(cpClock);


        // calculate current cohort
        List<Integer> cohort = obNode.cohort;
        for (int neiId : cohort)
        {
            int llr = obNode.LLR[neiId];
            if (llr > 0 && neiId != excludeNodeId)  // todo can exclude fromNodeId
            {
                currentCohort.add(neiId);
                Node neiNode = Node.getNode(neiId);
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, llr, Server.MESSAGE.CHECKPOINT.getT());
                obNode.LLR[neiId] = 0;          // reset LLR
            }

            obNode.FLS[neiId] = 0;              // reset FLS, will not take another tentative checkpoint
        }
    }


    /**
     * receive REPLY message
     * If received replies from all cohorts, start unFreeze() or REPLY to request node
     */
    public void receiveFreezeReply(int fromNodeId)
    {
        replyFromCohort.add(fromNodeId);
        if (replyFromCohort.equals(currentCohort))  // wait for all replies from all current cohorts
        {
            if (initiatorFlag)      // initiator should send unfreeze after receive reply from all
            {
                unFreeze();
            }
            else                    // reply to request node
            {
                for (int requestId : requestSet)
                {
                    Node reqNode = Node.getNode(requestId);
                    SocketManager.send(reqNode.hostname, reqNode.port, obNode.id, 0, Server.MESSAGE.FREEZE_REPLY.getT());
                }
                requestSet.clear();
            }

            replyFromCohort.clear();
        }
    }

    /**
     * Second phase
     * confirm checkpoint or recovery
     * todo update LLS, reset FLS, LLR
     */
    private void unFreeze()
    {
        if (!isFreezeComplete)          // prevent duplicate unfreeze
        {
            return;
        }

        isFreezeComplete = false;
        if (RandomMessage.ins(obNode.id).isStop)            // restart random message
        {
            RandomMessage.ins(obNode.id).isStop = false;
            RandomMessage.ins(obNode.id).nextMessage();
        }

        // send unfreeze to cohort
        for (int cohortId : currentCohort)
        {
            Node cohNode = Node.getNode(cohortId);
            SocketManager.send(cohNode.hostname, cohNode.port, obNode.id, 0, Server.MESSAGE.UNFREEZE.getT());
        }


        // todo notify next initiator
    }

    public void receiveUnfreeze(int fromNodeId)
    {
        // propagate unfreeze message
        unFreeze();

        // send REPLY to fromNodeId, todo reply until receive all replies from cohorts
        Node fromNode = Node.getNode(fromNodeId);
        SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.UNFREEZE_REPLY.getT());

    }
    /**
     * If received replies from all cohorts, send operationComplete to notify next initiator
     */
    public void receiveUnfreezeReply(int fromNodeId)
    {
        replyFromCohort.add(fromNodeId);
        if (replyFromCohort.equals(currentCohort))
        {
            replyFromCohort.clear();
            currentCohort.clear();      //todo where

            if (initiatorFlag)
            {
                // todo how does next initiator know its turn
                // send operation complete to all nodes
                Daemon.ins(obNode.id).broadcastOperationComplete();
            }
        }

    }




}
