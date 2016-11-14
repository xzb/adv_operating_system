package Application;

import Tool.Parser;

/**
 * Created by xiezebin on 11/13/16.
 */
public class Driver {

    public static Server server;
    public static RandomMessage randomMessage;
    public static Checkpoint checkpoint;


    /* user case:
    * run Server in a thread, forever loop
    * run RandomMessage in a thread, execute per SendDelay
    * run Checkpoint in a thread, execute per InstanceDelay
    */
    public static void main(String[] args) {

        if (args.length < 1)
        {
            System.out.println("Please enter nodeId.");
            System.exit(1);
        }
        int nid = Integer.valueOf(args[0]);
        if (args.length > 1)
        {
            Parser.config = args[1];        // config file dir
        }

        // initialize objects
        server = new Server(nid);
        randomMessage = new RandomMessage(nid);
        checkpoint = new Checkpoint();

        // wait 10 seconds to let all servers ready
        try {
            Thread.sleep(10 * 1000);
        }
        catch (Exception e) {}

        // start random message and checkpoint
        randomMessage.nextMessage();
        checkpoint.nextOperation();

    }
}
