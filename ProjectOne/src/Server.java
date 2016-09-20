/**
 * Created by xiezebin on 9/2/16.
 */

public class Server
{
    private final static boolean SOCKET_BY_SCTP = false;
    private Node obNode;

    public Server(String arNodeId)
    {
        //obNode = new Node(Integer.valueOf(arNodeId));
        obNode = Node.getNode(Integer.valueOf(arNodeId));
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

            // broadcast COMPLETE TODO
            //for each node
            //send()
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
        if(SOCKET_BY_SCTP)
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

        final Server serverObj = new Server(args[0]);

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
