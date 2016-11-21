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
    private boolean freezeSendFlag;          // todo use static lock
    private boolean freezeCompleteFlag;

    private boolean willingToCheckpoint;
    private Set<Integer> currentCohort;
    private Set<Integer> replyFromCohort;
    private int initiator;

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
    }
    public boolean isFreeze()
    {
        return freezeSendFlag || freezeCompleteFlag;
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

        operationList.remove(0);
    }


    /**
     * First phase
     * Checkpoint
     * If LLR is not null, send checkpoint message to neighbor id, piggyback llr
     */
    private void initiateCheckpoint()
    {

        initiator = obNode.id;
        takeTentativeCheckpointAndRequestCohorts();

    }
    public void receiveCheckpoint(int fromNodeId, int llr)
    {
        initiator = fromNodeId;
        int fls = obNode.FLS[fromNodeId];
        if (willingToCheckpoint && fls > 0 && llr >= fls)
        {
            // todo forward checkpoint message, how to exclude initiator?


            takeTentativeCheckpointAndRequestCohorts();
        }

        // wait for all replies from all current cohorts, then reply to fromNodeId (initiator)
        // todo reply, how to make sure each node reply to initiator?
        //Node fromNode = Node.getNode(fromNodeId);
        //SocketManager.send(fromNode.hostname, fromNode.port, nodeId, 0, Server.MESSAGE.FREEZE_REPLY.getT());


        // base case
        if (currentCohort.size() == 0)
        {
            Node fromNode = Node.getNode(fromNodeId);
            SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.FREEZE_REPLY.getT());
        }

    }

    public void takeTentativeCheckpointAndRequestCohorts()
    {
        // mark tentative checkpoint
        sequenceNum++;
        int[] cpClock = new int[Parser.numNodes];
        System.arraycopy(obNode.clock, 0, cpClock, 0, Parser.numNodes);
        obNode.checkpoints.add(cpClock);


        // todo calculate current cohort
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
     * Reply message to nodes who send the checkpoint message,
     * If received replies from all cohorts, start unFreeze()
     */
    public void receiveFreezeReply(int fromNodeId)
    {
        // todo forever broadcast
        replyFromCohort.add(fromNodeId);
        if (replyFromCohort.size() == obNode.cohort.size())
        {
            Set<Integer> cp = new HashSet<>(obNode.cohort);
            if (replyFromCohort.equals(cp))
            {
                // todo initiator should send unfreeze after receive reply from all
                if (initiator == obNode.id)
                {

                }
                else
                {
                    Node initNode = Node.getNode(initiator);
                    SocketManager.send(initNode.hostname, initNode.port, obNode.id, 0, Server.MESSAGE.FREEZE_REPLY.getT());
                }
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
        freezeSendFlag = false;
        freezeCompleteFlag = false;

        // todo move to Driver
        if (Driver.randomMessage.isStop)
        {
            Driver.randomMessage.isStop = false;
            Driver.randomMessage.nextMessage();
        }


        // todo notify next initiator
    }

    public void receiveUnfreeze(int fromNodeId)
    {

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
