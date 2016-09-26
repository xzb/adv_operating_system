import java.util.HashMap;

/**
 * Created by xiezebin on 9/25/16.
 */
public class UniTest {

    public static void sendTest(String arHostname, int arPort, String msg)
    {
        SocketManager.send(arHostname, arPort, msg);

    }

    public static void main(String[] args)
    {
        String hostname = "dc01.utdallas.edu";
        int port = 3332;
        if (args.length > 1)
        {
            hostname = args[0];
            port = Integer.valueOf(args[1]);
        }
        //sendTest(hostname, port, "Send Test Message.");

        FileIO.readNode(new HashMap<Integer, Node>());
    }
}
