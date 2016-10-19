import java.util.Random;

/**
 * Created by xiezebin on 10/18/16.
 */
public class App {

    private int nodeId;
    private Node obNode;
    private Server obServer;

    App(int arId)
    {
        nodeId = arId;
        obNode = Node.getNode(nodeId);
        obServer = new Server(nodeId);
    }
    public void enterCS(double exeTime)
    {
        obServer.enterCS();     //TODO check actual enter, may wait here


    }

    public void leaveCS()
    {
        obServer.leaveCS();
    }

    public void run() throws Exception
    {
        // use case: after REQUEST_DELAY, call enterCS();
        //          when receive all reply stay in CS for CS_EXE_TIME, then call leaveCS()
        //          Loop above process Total_Request times.

        Random rand = new Random();
        double delayLambda = 1.0 / obNode.REQUEST_DELAY;
        double exeLambda = 1.0 / obNode.CS_EXE_TIME;

        for (int round = 0; round < obNode.Total_Request; round++)
        {
            //double delay = Math.log(1 - rand.nextDouble()) / (-delayLambda);
            double delay = 1;
            //double exeTime = Math.log(1 - rand.nextDouble()) / (-exeLambda);
            double exeTime = 5;

            Thread.sleep((int) (1000 * delay));

            enterCS(exeTime);      // stay at least CS_EXE_TIME

            leaveCS();

        }

    }

    public static void main(String[] args) throws Exception{

        int nodeId = 1;         // TODO read from argument
        App app = new App(nodeId);

        app.run();
    }


}
