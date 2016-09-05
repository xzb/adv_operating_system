/**
 * Created by xiezebin on 9/2/16.
 */
import java.io.*;
import java.net.*;

public class Server
{
    private Node obNode;

    public Server(String arNodeId)
    {
        obNode = new Node(Integer.valueOf(arNodeId));
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
        }
        else
        {
            // forward if different
            Node loNextNode = new Node(loMessage.getNextNodeId());
            send(loNextNode, loMessage.serialize());
        }

    }

    /**
     * permanent loop of server socket
     */
    public void runServer()
    {
        String message = "";
        try
        {
            ServerSocket serverSock = new ServerSocket(obNode.getPort());

            while(true)
            {
                Socket sock = serverSock.accept();  // listen and accept

                BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                message = reader.readLine();        // blocked until a message is received
                reader.close();

                checkMessage(message);
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * send path information to next node
     * @param arNextNode
     * @param arMsg
     */
    public void send(Node arNextNode, String arMsg)
    {
        try
        {
            Socket clientSocket = new Socket(arNextNode.getHostname(), arNextNode.getPort());

            // send path info
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            writer.println(arMsg);
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        if (args.length < 1)
        {
            System.out.println("Please add server id.");
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
