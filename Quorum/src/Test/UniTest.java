package Test;

import Application.*;
import Tool.*;

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

    public static void serverTest()
    {
        // lamport time check

        // two server running
        ServerBase serverA = new ServerBase(0);
        ServerBase serverB = new ServerBase(1);

        // first process request
        serverA.enterCS(10, null);
        sleep(1);

        // second process request
        serverB.enterCS(2, null);
    }

    public static void serverPreemptionTest()
    {
        ServerBase serverA = new ServerPreemption(0);
        ServerBase serverB = new ServerPreemption(1);

        serverA.enterCS(2, null);
        serverB.enterCS(2, null);
    }

    public static void appTest()
    {
        App appA = new App(0);
        App appB = new App(1);

        appA.nextRequest();
        appB.nextRequest();
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
        //serverTest();
        //serverPreemptionTest();
        appTest();
        assert true;

        //System.exit(0);
    }
}
