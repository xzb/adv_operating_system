

/**
 * Created on 10/18/16.
 */
public class Client {

    public void send(String hostname, int port, int fromNodeId, int scalarTime, String arMsg)
    {
        SocketManager.send(hostname, port, fromNodeId, scalarTime, arMsg);
    }
}
