import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by xiezebin on 9/19/16.
 */
public class SocketManager {

    public static void sendByTCP(String arHostname, int arPort, String arMsg)
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
    public static void receiveByTCP(int arPort, Project1 obj)
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

                obj.checkMessage(message);
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    public static void sendBySCTP(String arHostname, int arPort, String arMsg)
    {}
    public static void receiveBySCTP(int arPort, Project1 obj)
    {}
}
