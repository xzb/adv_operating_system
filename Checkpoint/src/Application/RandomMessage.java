package Application;

import Tool.Parser;
import Tool.SocketManager;

import java.util.Random;

/**
 * Created by xiezebin on 11/12/16.
 */
public class RandomMessage
{
    private Node obNode;
    private int remainNumMsg;
    public boolean isStop;

    public RandomMessage(int nid)
    {
        obNode = Node.getNode(nid);
        remainNumMsg = Parser.numRandomMessages;
        isStop = false;
    }

    /*
    * For every sendDelay, a node selects neighbor uniformly at random and sends a message to that neighbor
    * update vector clock
    * update FLS, LLR, LLS
    */
    public void nextMessage()
    {
        if (remainNumMsg > 0)
        {
            Random rand = new Random();
            double delayLambda = 1.0 / Parser.minSendDelay;
            double delay = Math.log(1 - rand.nextDouble()) / (-delayLambda);

            try {
                Thread.sleep((int) delay);
            }
            catch (Exception e) {}

            int randIndex = rand.nextInt(obNode.cohort.size());
            int neiId = obNode.cohort.get(randIndex);
            Node neiNode = Node.getNode(neiId);

            if(!TwoPhaseSnapshot.isFreeze()) {
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, Server.MESSAGE.APPLICATION.getT());
                remainNumMsg--;

                nextMessage();          // loop until freeze
            }
            else
            {
                isStop = true;
            }
        }
    }


    /*
    * interact function with TwoPhaseSnapshot
    */
}
