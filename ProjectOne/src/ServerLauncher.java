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

public class ServerLauncher
{
    private Node obNode;
    private Set<Integer> obCompleteNId;

    public ServerLauncher(int arNodeId)
    {
        //obNode = Node.getNode(arNodeId, arHostname, arPort, arPath);
        obNode = Node.getNode(arNodeId);
        obCompleteNId = new HashSet<>();
    }


    /**
     * Callback function when server receives messages.
     * Check whether the whole path is traversed.
     * @param arMsg
     */
    public void checkMessage(String arMsg)
    {
        //System.out.println("# Message: " + arMsg);

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
                System.out.println("# Received COMPLETE message from all nodes. Terminate now.");
                System.exit(0);
            }
            return;
        }


        /*
         * check if destination is reached, otherwise append current node to path
         */
        Message loMessage = Message.deSerialize(arMsg);
        if(loMessage == null)
        {
            return;
        }

        loMessage.addNode(obNode.getId(), obNode.getLabel());

        if (loMessage.getWholePath().equals(loMessage.getHasVisit()))
        {
            // print final value
            String resultMsg = "# Node Id: " + obNode.getId() + ", label: " + obNode.getLabel() + ", path label sum: " + loMessage.getLabelSum();
            System.out.println(resultMsg);
            FileIO.writeResultToFile("ResultOfNode" + obNode.getId(), resultMsg);

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
            //System.out.println("# Forward an event.");
            //System.out.println("# hostname: " + loNextNode.getHostname());
            //System.out.println("# port: " + loNextNode.getPort());
            //System.out.println("# msg: " + loMessage.serialize());

            send(loNextNode, loMessage.serialize());
        }

    }


    /**
     * permanent loop of server socket
     */
    public void runServer()
    {
        SocketManager.receive(obNode.getPort(), this);
    }

    /**
     * send path information to next node
     * @param arNextNode
     * @param arMsg
     */
    public void send(Node arNextNode, String arMsg)
    {
        SocketManager.send(arNextNode.getHostname(), arNextNode.getPort(), arMsg);
    }

    public static void main(String args[])
    {
        if (args.length < 2)
        {
            //System.out.println("# Please add serverId, hostname, port, path.");
            System.out.println("# Please add serverId, configFile.");
            System.exit(1);
        }

        int loNodeId = Integer.valueOf(args[0]);
        FileIO.CONFIG_FILE = args[1];

        ServerLauncher server = new ServerLauncher(loNodeId);


        // run server
        server.runServer();

    }

}
