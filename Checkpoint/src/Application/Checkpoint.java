package Application;

import Tool.Parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by xiezebin on 11/12/16.
 */
public class Checkpoint {

    private int remainNumOperation;
    private List<String> operationList;

    public Checkpoint()
    {
        remainNumOperation = Parser.numOperations;
        operationList = new LinkedList<>(Parser.operationList);
    }

    public void nextOperation()
    {
        if (remainNumOperation > 0)
        {
            Random rand = new Random();
            double delayLambda = 1.0 / Parser.minSendDelay;
            double delay = Math.log(1 - rand.nextDouble()) / (-delayLambda);

            try {
                Thread.sleep((int) delay);
            }
            catch (Exception e) {}

            handleOperationList();
            remainNumOperation--;

            nextOperation();
        }
    }

    public void handleOperationList()
    {

        operationList.remove(0);
    }

    public void initiateCheckpoint()
    {
        //SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, Server.MESSAGE.APPLICATION.getT());

    }
    public void initiateRecovery()
    {

    }
}
