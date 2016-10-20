package Application;

import java.util.Random;

/**
 * Created by xiezebin on 10/18/16.
 */
public class App {

    private int nodeId;
    private Node obNode;
    private ServerBase obServer;
    private int remainNumOfRequest;

    public App(int arId)
    {
        nodeId = arId;
        obNode = Node.getNode(nodeId);
        obServer = new ServerBase(nodeId);
        remainNumOfRequest = obNode.Total_Request;
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


}
