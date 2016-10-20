package Application;

import java.util.Random;

/**
 * Created by xiezebin on 10/18/16.
 */
public class App {

    private int nodeId;
    private Node obNode;
    private Server obServer;
    private int remainNumOfRequest;

    public App(int arId)
    {
        nodeId = arId;
        obNode = Node.getNode(nodeId);
        obServer = new Server(nodeId);
        remainNumOfRequest = obNode.Total_Request;
    }
    public void enterCS(double exeTime)
    {
        obServer.enterCS(exeTime, this);     // pass exeTime, Server will callback leaveCS() once execute exeTime
    }

    public void leaveCS()
    {
        run();
    }

    public void run()
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
            //double delay = 1;
            //double exeTime = 2;

            try {
                Thread.sleep((int) (1000 * delay));
            }
            catch (Exception e) {}

            enterCS(exeTime);      // stay at least CS_EXE_TIME

            remainNumOfRequest--;
        }

    }

    public static void main(String[] args) {

        int nodeId = 1;         // TODO read from argument
        App app = new App(nodeId);

        app.run();
    }


}
