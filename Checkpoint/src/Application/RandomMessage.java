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
    private Node obNode;
    private int remainNumMsg;
    public boolean isStop;

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
            Node neiNode = Node.getNode(neiId);
                                                                //todo use notify
            if(!Checkpoint.ins(obNode.id).isFreeze()) {          //todo lock to prevent receive freeze
                // update send clock, FLS
                obNode.clock[obNode.id]++;
                int clock = obNode.clock[obNode.id];
                if (obNode.FLS[neiId] == 0)
                {
                    obNode.FLS[neiId] = clock;      // assign first message after checkpoint
                }

                // Application piggyback clock as label
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, clock, Server.MESSAGE.APPLICATION.getT());
                remainNumMsg--;

                nextMessage();          // loop until freeze
            }
            else
            {
                isStop = true;
            }
        }
    }


    // update receive clock, LLR
    public static void receiveApplication(int nodeId, int fromNodeId, int label)
    {
        Node node = Node.getNode(nodeId);
        node.clock[fromNodeId]++;
        node.LLR[fromNodeId] = label;         // label is monotonically increasing

    }


}
