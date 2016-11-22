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
    private boolean isFreezeComplete;       // todo use static lock

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
        System.out.println("initiator cohort size: " + currentCohort.size());
        if (currentCohort.size() == 0)
        {
            sendCheckpointConfirm();

            receiveCheckpointConfirmReply(-1);      // since no cohorts, directly proceed as received all replies
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
            takeTentativeCheckpointAndRequestCohorts(fromNodeId);       // fromNodeId is excluded when propagating
        }


        // todo check checkpoint already taken by sequenceNum
        // if cohorts is empty, or need not take checkpoint, directly REPLY
        if (currentCohort.size() == 0 || (willingToCheckpoint && fls == 0))
        {
            Node fromNode = Node.getNode(fromNodeId);
            SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.CHECKPOINT_REPLY.getT());
            requestSet.remove(fromNodeId);      // prevent duplicate reply
        }

    }

    public void takeTentativeCheckpointAndRequestCohorts(int excludeNodeId)
    {
        // mark TENTATIVE checkpoint
        sequenceNum++;                      // todo add as checkpoint info
        int[] cpClock = new int[Parser.numNodes];
        System.arraycopy(obNode.clock, 0, cpClock, 0, Parser.numNodes);
        obNode.checkpoints.add(cpClock);


        // calculate current cohort
        List<Integer> cohort = obNode.cohort;
        for (int neiId : cohort)
        {
            int llr = obNode.LLR[neiId];
            if (llr > 0 && neiId != excludeNodeId)  // can exclude fromNodeId
            {
                currentCohort.add(neiId);
                Node neiNode = Node.getNode(neiId);
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, llr, Server.MESSAGE.CHECKPOINT.getT());
            }

            obNode.LLR[neiId] = 0;              // reset LLR
            obNode.FLS[neiId] = 0;              // reset FLS, will not take another tentative checkpoint
        }
    }


    /**
     * receive REPLY message
     * If received replies from all cohorts, start sendCheckpointConfirm() or REPLY to requested node
     */
    public void receiveCheckpointReply(int fromNodeId)
    {
        replyFromCohort.add(fromNodeId);
        if (replyFromCohort.equals(currentCohort))  // wait for all replies from all current cohorts
        {
            if (initiatorFlag)      // initiator should send unfreeze after receive reply from all
            {
                sendCheckpointConfirm();
            }
            else                    // reply to request node
            {
                for (int requestId : requestSet)
                {
                    Node reqNode = Node.getNode(requestId);
                    SocketManager.send(reqNode.hostname, reqNode.port, obNode.id, 0, Server.MESSAGE.CHECKPOINT_REPLY.getT());
                }
                requestSet.clear();
            }

            replyFromCohort.clear();
        }
    }

    /**
     * Second phase
     * confirm checkpoint or recovery
     * call in three situation: 1. initiator has no cohorts
     *                          2. initiator received all checkpoint replies
     *                          3. nodes receive confirm message and propagate
     * todo update LLS
     */
    private void sendCheckpointConfirm()
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
            SocketManager.send(cohNode.hostname, cohNode.port, obNode.id, 0, Server.MESSAGE.CHECKPOINT_CONFIRM.getT());
        }
    }

    public void receiveCheckpointConfirm(int fromNodeId)
    {
        // propagate unfreeze message
        sendCheckpointConfirm();

        // send REPLY to fromNodeId, todo is it better to reply until receive all replies from cohorts
        Node fromNode = Node.getNode(fromNodeId);
        SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.CHECKPOINT_CONFIRM_REPLY.getT());
    }

    /**
     * If initiator has no cohorts, or received replies from all cohorts,
     * send operationComplete to notify next initiator
     */
    public void receiveCheckpointConfirmReply(int fromNodeId)
    {
        if (fromNodeId >= 0)
        {
            replyFromCohort.add(fromNodeId);
        }
        if (replyFromCohort.equals(currentCohort))
        {
            replyFromCohort.clear();
            currentCohort.clear();          // clear here

            if (initiatorFlag)
            {
                // send operationComplete to all nodes, in order to notify next initiator
                Daemon.ins(obNode.id).broadcastOperationComplete();
            }
        }
    }


}
