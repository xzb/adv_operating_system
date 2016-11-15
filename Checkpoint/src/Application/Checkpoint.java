package Application;

import Tool.Parser;
import Tool.SocketManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by xiezebin on 11/12/16.
 */
public class Checkpoint {

    private Node obNode;
    private List<String> operationList;

    private static boolean freezeSendFlag;          // todo use static lock
    private static boolean freezeCompleteFlag;


    public Checkpoint(int nid)
    {
        obNode = Node.getNode(nid);
        operationList = new LinkedList<>(Parser.operationList);
    }
    public static boolean isFreeze()
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
        // mark tentative checkpoint
        int[] cpClock = new int[Parser.numNodes];
        System.arraycopy(obNode.clock, 0, cpClock, 0, Parser.numNodes);
        obNode.checkpoints.add(cpClock);

        List<Integer> cohort = obNode.cohort;
        for (int neiId : cohort)
        {
            int llr = obNode.LLR[neiId];
            if (llr > 0)
            {
                Node neiNode = Node.getNode(neiId);
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, llr, Server.MESSAGE.CHECKPOINT.getT());
                obNode.LLR[neiId] = 0;          // reset LLR
            }
        }

    }
    public static void receiveCheckpoint(int nodeId, int fromNodeId, int llr)
    {
        Node node = Node.getNode(nodeId);
        int fls = node.FLS[fromNodeId];
        if (fls > 0 && llr >= fls)
        {
            // todo forward checkpoint message,


        }
        // todo reply

    }

    /**
     * Recovery
     */
    private void initiateRecovery()
    {

    }
    public static void receiveRecovery(int nodeId, int fromNodeId, int lls)
    {

    }

    /**
     * Reply message back to initiator,
     * If received replies from all cohorts, start unFreeze()
     */
    public static void receiveFreezeReply(int nodeId, int fromNodeId)
    {

    }

    /**
     * Second phase
     * confirm checkpoint or recovery
     * todo update LLS, reset FLS, LLR
     */
    private static void unFreeze()
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

    public static void receiveUnfreeze(int nodeId, int fromNodeId)
    {

    }
    /**
     * If received replies from all cohorts, send operationComplete to notify next initiator
     */
    public static void receiveUnfreezeReply(int nodeId, int fromNodeId)
    {

    }

    /**
     * When received this message, it schedules start nextOperation
     */
    public static void receiveOperationComplete(int nodeId, int fromNodeId)
    {

    }
}
