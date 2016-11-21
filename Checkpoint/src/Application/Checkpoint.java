package Application;

import Tool.Parser;
import Tool.SocketManager;

import java.util.*;

/**
 * Created by xiezebin on 11/12/16.
 */
public class Checkpoint {

    private Node obNode;
    private List<String> operationList;

    private int sequenceNum;
    //private boolean freezeSendFlag;          // todo use static lock
    private boolean freezeCompleteFlag;

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
        operationList = new LinkedList<>(Parser.operationList);
        sequenceNum = 0;
        willingToCheckpoint = true;
        currentCohort = new HashSet<Integer>();
        replyFromCohort = new HashSet<Integer>();
        requestSet = new HashSet<Integer>();
    }
    public boolean isFreeze()
    {
        //return freezeSendFlag || freezeCompleteFlag;
        return freezeCompleteFlag;
    }

    public void nextOperation()
    {
        if (!operationList.isEmpty())
        {
            Random rand = new Random();
            double delayLambda = 1.0 / Parser.minSendDelay;
            double delay = Math.log(1 - rand.nextDouble()) / (-delayLambda);

            try {
                Thread.sleep((int) delay);
            }
            catch (Exception e) {}

            handleOperationList();

        }
    }

    /**
     * handle top of operation list, if match current node id, initiate checkpoint or recovery
     */
    private void handleOperationList()
    {
        if (operationList.isEmpty())
        {
            return;
        }

        String opPair = operationList.get(0);
        String[] parts = opPair.substring(1, opPair.length() - 1).split(",");   // (c,1) => [c, 1]
        char opType = parts[0].charAt(0);
        int opId = Integer.valueOf(parts[1]);

        if (obNode.id == opId)
        {
            if (opType == 'c')
            {
                initiateCheckpoint();
            }
            else if (opType == 'r')
            {
                initiateRecovery();
            }
        }

        // todo when to remove
        operationList.remove(0);
    }


    /**
     * First phase
     * Checkpoint
     * If LLR is not null, send checkpoint message to neighbor id, piggyback llr
     */
    private void initiateCheckpoint()
    {

        initiatorFlag = true;
        takeTentativeCheckpointAndRequestCohorts();

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
        int fls = obNode.FLS[fromNodeId];
        if (willingToCheckpoint && fls > 0 && llr >= fls)
        {
            // todo forward checkpoint message, how to exclude initiator?


            takeTentativeCheckpointAndRequestCohorts();
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

    public void takeTentativeCheckpointAndRequestCohorts()
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
            if (llr > 0)
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

        }
    }

    /**
     * Second phase
     * confirm checkpoint or recovery
     * todo update LLS, reset FLS, LLR
     */
    private void unFreeze()
    {
        //freezeSendFlag = false;
        freezeCompleteFlag = false;

        // todo move to Driver
        if (RandomMessage.ins(obNode.id).isStop)
        {
            RandomMessage.ins(obNode.id).isStop = false;
            RandomMessage.ins(obNode.id).nextMessage();
        }

        // todo send unfreeze
        for (int cohortId : currentCohort)
        {
            Node cohNode = Node.getNode(cohortId);
            SocketManager.send(cohNode.hostname, cohNode.port, obNode.id, 0, Server.MESSAGE.UNFREEZE.getT());
        }


        // todo notify next initiator
    }

    public void receiveUnfreeze(int fromNodeId)
    {
        freezeCompleteFlag = false;

        currentCohort.clear();        // todo where to clear
        replyFromCohort.clear();

    }
    /**
     * If received replies from all cohorts, send operationComplete to notify next initiator
     */
    public void receiveUnfreezeReply(int fromNodeId)
    {

    }

    /**
     * When received this message, it schedules start nextOperation
     */
    public void receiveOperationComplete(int fromNodeId)
    {

    }



    /**
     * Recovery
     */
    private void initiateRecovery()
    {

    }
    public void receiveRecovery(int fromNodeId, int lls)
    {

    }

}
