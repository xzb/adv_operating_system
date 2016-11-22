package Application;

import Tool.Parser;
import Tool.SocketManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by xiezebin on 11/12/16.
 */
public class RandomMessage
{
    private boolean isDebug = false;
    private int msgDstId;

    private Node obNode;
    private int remainNumMsg;
    private boolean isStop;

    private static Map<Integer, RandomMessage> obInstances;
    public static RandomMessage ins(int nid)
    {
        if (obInstances == null)
        {
            obInstances = new HashMap<Integer, RandomMessage>();
        }
        if (!obInstances.containsKey(nid))
        {
            RandomMessage randm = new RandomMessage(nid);
            obInstances.put(nid, randm);
        }
        return obInstances.get(nid);
    }
    private RandomMessage(int nid)
    {
        obNode = Node.getNode(nid);
        remainNumMsg = Parser.numRandomMessages;
        isStop = false;
    }
    public boolean isStop()
    {
        return isStop;
    }

    /*
    * For every sendDelay, a node selects neighbor uniformly at random and sends a message to that neighbor
    * update vector clock
    * update FLS, LLR, LLS
    */
    public void nextMessage()
    {
        Runnable launch = new Runnable() {
            @Override
            public void run() {
                nextMessageHelper();
            }
        };
        new Thread(launch).start();
    }

    private void nextMessageHelper()
    {
        isStop = false;

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
            if (isDebug)                    // if in debug mode, direct msg instead of random
            {
                neiId = msgDstId;
            }
            Node neiNode = Node.getNode(neiId);
                                                                 // todo use notify, or lock
            if(!isFreeze()) {          // if FREEZE, cannot send
                // update send clock, FLS
                obNode.clock[obNode.id]++;
                int clock = obNode.clock[obNode.id];
                if (obNode.FLS[neiId] == 0)
                {
                    obNode.FLS[neiId] = clock;      // assign first message after checkpoint
                }
                // update LLS
                obNode.LLS[neiId] = clock;

                // Application piggyback clock as label
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, clock, Server.MESSAGE.APPLICATION.getT());
                remainNumMsg--;

                if (!isDebug)               // if in debug mode, do not loop
                {
                    nextMessage();          // loop until freeze
                }
            }
            else
            {
                isStop = true;
            }
        }
    }


    // update receive clock, LLR
    public void receiveApplication(int fromNodeId, int label)
    {
        if(!isFreeze())   // if FREEZE, cannot receive
        {
            obNode.clock[fromNodeId]++;
            obNode.LLR[fromNodeId] = label;         // label is monotonically increasing
        }
    }

    private boolean isFreeze()
    {
        return Checkpoint.ins(obNode.id).isFreeze() || Recovery.ins(obNode.id).isFreeze();
    }

    public void directMessage(int nid)
    {
        isDebug = true;
        msgDstId = nid;
        nextMessageHelper();

        isDebug = false;
    }
}
