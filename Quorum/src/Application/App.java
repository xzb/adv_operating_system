package Application;

import Tool.*;

import java.util.Random;

/**
 * Created by xiezebin on 10/18/16.
 */
public class App {

    private int nodeId;
    private Node obNode;
    private ServerBase obServer;
    private int remainNumOfRequest;

    private static boolean APPROACH_REQUEST_IN_ORDER = true;

    public App(int arId)
    {
        nodeId = arId;
        obNode = Node.getNode(nodeId);
        remainNumOfRequest = obNode.Total_Request;

        if (APPROACH_REQUEST_IN_ORDER)
        {
            obServer = new ServerOrderRequest(nodeId);
        }
        else
        {
            obServer = new ServerPreemption(nodeId);
        }
    }
    private void enterCS(double exeTime)
    {
        // pass exeTime, Server will callback App's leaveCS() and continue next request
        obServer.enterCS(exeTime, new AppCallback() {
            @Override
            public void leaveCS() {
                nextRequest();
            }
        });
    }

    public interface AppCallback {
        void leaveCS();
    }

    public void nextRequest()
    {
        // use case: after REQUEST_DELAY, call enterCS();
        //          when receive all reply stay in CS for CS_EXE_TIME, then call leaveCS()
        //          Loop above process Total_Request times.

        if (remainNumOfRequest > 0)
        {
            Random rand = new Random();
            double delayLambda = 1.0 / obNode.REQUEST_DELAY;
            double exeLambda = 1.0 / obNode.CS_EXE_TIME;

            double delay = Math.log(1 - rand.nextDouble()) / (-delayLambda);
            double exeTime = Math.log(1 - rand.nextDouble()) / (-exeLambda);

            try {
                Thread.sleep((int) (1000 * delay));
            }
            catch (Exception e) {}

            enterCS(exeTime);      // stay at least CS_EXE_TIME

            remainNumOfRequest--;
        }

    }

    public static void main(String[] args) {
        if (args.length < 1)
        {
            System.out.println("Please enter nodeId, approach(0:orderRequest/1:preemption), socket(0:sctp/1:tcp).");
            System.out.println("Default: orderRequest, sctp.");
            System.exit(1);
        }
        if (args.length > 1)
        {
            // approach option, 0 for order request, 1 for preemption, default request in order
            APPROACH_REQUEST_IN_ORDER = "0".equals(args[1]);
        }
        if (args.length > 2)
        {
            // socket option, 0 for sctp, 1 for tcp, default sctp
            SocketManager.SOCKET_BY_SCTP = "0".equals(args[2]);
        }
        if (args.length > 3)
        {
            Parser.config = args[3];
        }

        // launch server
        App app = new App(Integer.valueOf(args[0]));

        // wait 20 seconds to let all servers ready
        try {
            Thread.sleep(20 * 1000);
        }
        catch (Exception e)
        {
        }

        // start request events
        app.nextRequest();
    }

}
