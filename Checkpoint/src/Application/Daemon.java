package Application;

import Tool.Parser;
import Tool.SocketManager;

import java.util.*;

/**
 * Created by xiezebin on 11/21/16.
 */
public class Daemon {
    private int obNodeId;
    private List<String> operationList;

    private static Map<Integer, Daemon> obInstances;
    public static Daemon ins(int nid)
    {
        if (obInstances == null)
        {
            obInstances = new HashMap<Integer, Daemon>();
        }
        if (!obInstances.containsKey(nid))
        {
            Daemon dae = new Daemon(nid);
            obInstances.put(nid, dae);
        }
        return obInstances.get(nid);
    }
    private Daemon(int nid)
    {
        obNodeId = nid;
        operationList = new LinkedList<>(Parser.operationList);
    }

    /**
     * Check node id on top of operationList,
     * if not itself, do nothing, wait for operationCOMPLETE message;
     * if is itself, start corresponding checkpoint or recovery;
     *   at the end of unfreeze process, call broadcastOperationComplete.
     */
    public void nextOperation()
    {
        Runnable launch = new Runnable() {
            @Override
            public void run() {
                nextOperationHelper();
            }
        };
        new Thread(launch).start();
    }
    private void nextOperationHelper()
    {
        if (!operationList.isEmpty())
        {
            String opPair = operationList.get(0);
            String[] parts = opPair.substring(1, opPair.length() - 1).split(",");   // (c,1) => [c, 1]
            char opType = parts[0].charAt(0);
            int opId = Integer.valueOf(parts[1]);

            if (obNodeId == opId)       // proceed if top of operationList is current node
            {
                // exponential delay
                Random rand = new Random();
                double delayLambda = 1.0 / Parser.minInstanceDelay;
                double delay = Math.log(1 - rand.nextDouble()) / (-delayLambda);
                try {
                    Thread.sleep((int) delay);
                }
                catch (Exception e) {}


                System.out.println("Initiator node: " + obNodeId);
                if (opType == 'c')
                {
                    Checkpoint.ins(obNodeId).initiateCheckpoint();
                }
                else if (opType == 'r')
                {
                    Recovery.ins(obNodeId).initiateRecovery();
                }
            }
        }
    }

    /**
     * Daemon process should connect to all nodes, in order to notify next initiator.
     * Callback function for Checkpoint or Recovery
     */
    public void broadcastOperationComplete()
    {
        // send operationComplete message to all nodes
        Collection<Node> allNodes = Node.getAllNodes();

        for (Node node : allNodes)
        {
            if (node.id != obNodeId)
            {
                SocketManager.send(node.hostname, node.port, obNodeId, 0, Server.MESSAGE.OPERATION_COMPLETE.getT());
            }
            else
            {
                receiveOperationComplete(obNodeId);         // initiator itself
            }
        }
    }

    /**
     * When received this message, it schedules start nextOperation
     */
    public void receiveOperationComplete(int fromNodeId)
    {
        // remove top of list todo check top id
        operationList.remove(0);

        // start next
        nextOperation();
    }

}
