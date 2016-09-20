import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiezebin on 9/2/16.
 *
 * Make sure config.txt is formatted as below:
 *
 * 0 dc01.utdallas.edu 3334 1,2,3,4
 * 1 dc33.utdallas.edu 5678 3,2,4
 * 2 dc21.utdallas.edu 5231 1,2,3,4,0
 * 3 dc33.utdallas.edu 2311 4,0,1,2
 * 4 dc22.utdallas.edu 3124 1,2,3,2,3,1
 *
 */

public class Project1
{
    private final static boolean SOCKET_BY_SCTP = false;
    private Node obNode;
    private Set<Integer> obCompleteNId;

    public Project1(String arNodeId)
    {
        //obNode = new Node(Integer.valueOf(arNodeId));
        obNode = Node.getNode(Integer.valueOf(arNodeId));
        obCompleteNId = new HashSet<>();
    }
    public String getInitMsg()
    {
        return new Message(obNode.getPath(), "", 0).serialize();
    }

    /**
     * check whether the whole path is traversed
     * @param arMsg
     */
    public void checkMessage(String arMsg)
    {
        // System.out.println("Message: " + arMsg);

        /*
         * if receive COMPLETE from all server, halt and exit
         */
        String[] parts = arMsg.split(";");
        if (Message.COMPLETE.equals(parts[0]))
        {
            int fromId = Integer.valueOf(parts[1]);
            obCompleteNId.add(fromId);
            if (obCompleteNId.equals(Node.getAllNodeIds()))
            {
                System.out.println("Received COMPLETE message from all nodes. Terminate now.");
                System.exit(0);
            }
            return;
        }


        Message loMessage = Message.deSerialize(arMsg);
        if(loMessage == null)
        {
            return;
        }

        loMessage.addNode(obNode.getId(), obNode.getLabel());

        if (loMessage.getWholePath().equals(loMessage.getHasVisit()))
        {
            // print final value
            System.out.println("Node label: " + obNode.getLabel() +
                    ", path label sum: " + loMessage.getLabelSum());

            // broadcast COMPLETE
            Set<Integer> allNodeIds = Node.getAllNodeIds();
            for (int nodeId : allNodeIds)
            {
                Node loNode = Node.getNode(nodeId);
                send(loNode, Message.COMPLETE + ";" + obNode.getId());        // send COMPLETE + Id
            }
        }
        else
        {
            // forward if different
            Node loNextNode = Node.getNode(loMessage.getNextNodeId());
            send(loNextNode, loMessage.serialize());
        }

    }

    /**
     * permanent loop of server socket
     */
    public void runServer()
    {
        if (SOCKET_BY_SCTP)
        {
            SocketManager.receiveBySCTP(obNode.getPort(), this);
        }
        else
        {
            SocketManager.receiveByTCP(obNode.getPort(), this);
        }
    }

    /**
     * send path information to next node
     * @param arNextNode
     * @param arMsg
     */
    public void send(Node arNextNode, String arMsg)
    {
        if(SOCKET_BY_SCTP)
        {
            SocketManager.sendBySCTP(arNextNode.getHostname(), arNextNode.getPort(), arMsg);
        }
        else
        {
            SocketManager.sendByTCP(arNextNode.getHostname(), arNextNode.getPort(), arMsg);
        }
    }

    public static void main(String args[])
    {
        if (args.length < 1)
        {
            System.out.println("Please add serverId.");
            System.exit(1);
        }

        final Project1 serverObj = new Project1(args[0]);

        // trigger event after 30 seconds
        Runnable trigger = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30 * 1000);
                }
                catch (Exception e)
                {}
                System.out.println("Triggered an event.");
                String loStartMsg = serverObj.getInitMsg();
                serverObj.checkMessage(loStartMsg);
            }
        };
        new Thread(trigger).start();


        // run server
        serverObj.runServer();

    }

}
