package Tool;

import Application.ServerBase.ServerCallback;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import com.sun.nio.sctp.MessageInfo;


/**
 * Created on xiezebin 10/18/16.
 */
public class SocketManager {
    private final static boolean SOCKET_BY_SCTP = false;

    public static void send(String arHostname, int arPort, int fromNodeId, long scalarTime, String arMsg) {
        //System.out.println("#SEND " + arHostname + ";" + arPort + ";" + arMsg + ";");
        String message = fromNodeId + ";" + scalarTime + ";" + arMsg;
        if (SOCKET_BY_SCTP) {
            SocketManager.sendBySCTP(arHostname, arPort, message);
        } else {
            SocketManager.sendByTCP(arHostname, arPort, message);
        }
    }
    public static void send(String arHostname, int arPort, int fromNodeId, String arMsg) {
        //System.out.println("#SEND " + arHostname + ";" + arPort + ";" + arMsg + ";");
        String message = fromNodeId + ";"  + arMsg;
        if (SOCKET_BY_SCTP) {
            SocketManager.sendBySCTP(arHostname, arPort, message);
        } else {
            SocketManager.sendByTCP(arHostname, arPort, message);
        }
    }





    public static void receive(int arPort, ServerCallback server) {
        if (SOCKET_BY_SCTP) {
            SocketManager.receiveBySCTP(arPort, server);
        } else {
            SocketManager.receiveByTCP(arPort, server);
        }
    }

    private static void sendByTCP(String arHostname, int arPort, String arMsg) {
        try {
            Socket clientSocket = new Socket(arHostname, arPort);

            // send path info
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            writer.println(arMsg);
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void receiveByTCP(int arPort, ServerCallback server) {
        String message = "";
        try {
            ServerSocket serverSock = new ServerSocket(arPort);

            while (true) {
                Socket sock = serverSock.accept();  // listen and accept

                BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                message = reader.readLine();        // blocked until a message is received
                reader.close();

                System.out.println("Receive: " + message);
                if (server != null) {
                    server.call(message);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void sendBySCTP(String arHostname, int arPort, String arMsg) {
        try {
            SocketAddress socketAddress = new InetSocketAddress(arHostname,arPort);
            System.out.println("open connection for socket [" + socketAddress + "]");
            SctpChannel sctpChannel = SctpChannel.open();//(socketAddress, 1 ,1 );
            ByteBuffer buf = ByteBuffer.allocateDirect(64000);
            sctpChannel.connect(socketAddress, 1 ,1);
            buf=buf.wrap(arMsg.getBytes());
            MessageInfo messageInfo=MessageInfo.createOutgoing(socketAddress,1);

            sctpChannel.send(buf, messageInfo);
            buf.flip();
            System.out.println("Send:"+arMsg);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveBySCTP(int arPort, ServerCallback server) {
        try{
        String message="";
            /***
             * SctpServerChannel ssc = SctpServerChannel.open();
             InetSocketAddress serverAddr = new InetSocketAddress(arPort);
             SocketAddress serverSocketAddress = new InetSocketAddress(arPort);
             // System.out.println("create and bind for sctp address");
             ssc.bind(serverAddr);

             ByteBuffer buf = ByteBuffer.allocateDirect(64000);

             SctpChannel sctpChannel;

             while ((sctpChannel = ssc.accept()) != null) {
             //BufferedReader reader = new BufferedReader(sctpChannel.receive(buf,1,null));
             MessageInfo messageInfo = null;

             messageInfo=sctpChannel.receive(buf,1,null);
             buf.flip();
             message = messageInfo.toString();
             // MessageInfo messageInfo = sctpChannel.receive(ByteBuffer.allocate(64000), null, null);
             System.out.println("Receive: "+message);
             if (obj != null) {
             obj.call(message);
             }


             }*/
            SocketAddress serverSocketAddress = new InetSocketAddress(arPort);
            SctpServerChannel sctpServerChannel =  SctpServerChannel.open().bind(serverSocketAddress);
            SctpChannel sctpChannel;
            while ((sctpChannel = sctpServerChannel.accept()) != null) {
                MessageInfo messageInfo = sctpChannel.receive(ByteBuffer.allocate(64000) , null, null);
                message=messageInfo.toString();

                System.out.println("Receive: " + message);
                if (server != null) {
                    server.call(message);
                }
            }

            } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

