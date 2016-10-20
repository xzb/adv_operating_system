package Tool;

import Application.ServerBase.ServerCallback;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created on xiezebin 10/18/16.
 */
public class SocketManager {
    private final static boolean SOCKET_BY_SCTP = false;

    public static void send(String arHostname, int arPort, int fromNodeId, long scalarTime, String arMsg)
    {
        //System.out.println("#SEND " + arHostname + ";" + arPort + ";" + arMsg + ";");
        String message = fromNodeId + ";" + scalarTime + ";" + arMsg;
        if(SOCKET_BY_SCTP)
        {
            SocketManager.sendBySCTP(arHostname, arPort, message);
        }
        else
        {
            SocketManager.sendByTCP(arHostname, arPort, message);
        }
    }
    public static void receive(int arPort, ServerCallback server)
    {
        if (SOCKET_BY_SCTP)
        {
            SocketManager.receiveBySCTP(arPort, server);
        }
        else
        {
            SocketManager.receiveByTCP(arPort, server);
        }
    }

    private static void sendByTCP(String arHostname, int arPort, String arMsg)
    {
        try
        {
            Socket clientSocket = new Socket(arHostname, arPort);

            // send path info
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            writer.println(arMsg);
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    private static void receiveByTCP(int arPort, ServerCallback server)
    {
        String message = "";
        try
        {
            ServerSocket serverSock = new ServerSocket(arPort);

            while(true)
            {
                Socket sock = serverSock.accept();  // listen and accept

                BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                message = reader.readLine();        // blocked until a message is received
                reader.close();

                System.out.println("Receive: " + message);
                if (server != null) {
                    server.call(message);
                }
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    private static void sendBySCTP(String arHostname, int arPort, String arMsg)
    {

    }
    private static void receiveBySCTP(int arPort, ServerCallback obj)
    {

    }
}
