import java.util.Random;

/**
 * Created by xiezebin on 10/19/16.
 */
public class UniTest {

    public static void nodeTest()
    {
        Node node=new Node(1);
        System.out.println("port " + node.port);
        System.out.println("delay " + node.REQUEST_DELAY);
        System.out.println("exe " + node.CS_EXE_TIME);
        System.out.println("total " + node.Total_Request);

        Tool.FileIO.writeFile("hello world2.");

    }

    public static void socketTest()
    {
        Runnable server1 = () -> {
            Node node = new Node(0);
            SocketManager.receive(node.port, null);
        };
        Runnable client1 = () -> {
            int srcId = 1;
            Node dst = new Node(0);
            SocketManager.send(dst.hostname, dst.port, srcId, 10, "test message.");
        };

        new Thread(server1).start();
        new Thread(client1).start();
    }

    public static void appTest()
    {

    }

    public static void serverTest()
    {
        /*
        Runnable server0 = () -> {
            Server server = new Server(0);
            server.launch();
        };
        Runnable server1 = () -> {
            Server server = new Server(1);
            server.launch();
        };
        new Thread(server0).start();
        new Thread(server1).start();
        */

        // lamport time check




        // one process request C.S.
        Server server0 = new Server(0);
        Server server1 = new Server(1);

        server0.enterCS();
        sleep(2);
        server0.leaveCS();
        sleep(2);
        server1.enterCS();
        sleep(2);
        server1.leaveCS();

        // two process request C.S. with different ts




    }

    public static void sleep(int second)
    {
        try {
            Thread.sleep(second * 1000);
        }
        catch (Exception e)
        {
        }
    }
    public static void main(String[] args) {

        //nodeTest();
        //socketTest();
        serverTest();
        assert true;

        System.exit(0);
    }
}
