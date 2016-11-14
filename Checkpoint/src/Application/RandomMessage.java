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
    private boolean freezeSendFlag;
    private boolean freezeCompleteFlag;
    private int remainNumMsg;

    public RandomMessage(int nid)
    {
        obNode = Node.getNode(nid);
        freezeSendFlag = false;
        freezeCompleteFlag = false;
        remainNumMsg = Parser.numRandomMessages;
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

            if(!freezeSendFlag && !freezeCompleteFlag) {
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, obNode.clockstr(), Server.MESSAGE.APPLICATION.getT());
                remainNumMsg--;
            }
        }
    }


    /*
    * interact function with TwoPhaseSnapshot
    */
    public void freezeSend()
    {

    }
    public void freezeComplete()
    {

    }
}
