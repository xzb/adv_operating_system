/**
 * Created by xiezebin on 9/2/16.
 */

import java.io.*;
import java.net.*;

public class SampleClient
{
    public void go()
    {
        String message;
        try
        {
            //Create a client socket and connect to server at 127.0.0.1 port 5000
            Socket clientSocket = new Socket("127.0.0.1", 5000);
            //Read messages from server. Input stream are in bytes. They are converted to characters by InputStreamReader
            //Characters from the InputStreamReader are converted to buffered characters by BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //The method readLine is blocked until a message is received
            message = reader.readLine();
            System.out.println("Server says:" + message);
            reader.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    public static void main(String args[])
    {
        SampleClient SampleClientObj = new SampleClient();
        SampleClientObj.go();
    }
}
