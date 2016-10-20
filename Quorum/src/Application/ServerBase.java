package Application;

import Tool.SocketManager;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Created by xiezebin on 10/18/16.
 */
public class ServerBase {

    private boolean APPROACH_REQUEST_IN_ORDER = true;

    enum MESSAGE_TYPE {
        REQUEST("REQUEST"),
        GRANT("GRANT"),
        RELEASE("RELEASE"),
        FAIL("FAIL"),
        INQUIRE("INQUIRE"),
        YIELD("YIELD");

        private String title;
        private MESSAGE_TYPE(String arTitle) {
            title = arTitle;
        }
        public String getTitle() {
            return this.title;
        }
    }

    private int nodeId;
    private Node obNode;
    private boolean locked;
    private long lamportTime;  	// increase each time of send or receive message
    private Set<Integer> permission_received_from_quorum;
    private PriorityQueue<long[]> requestQueue;              //<timestamp, id>

    private double obExeTime;
    private App.AppCallback obAppCallback;

    public ServerBase()
    {}
    public ServerBase(int arNodeId)
    {
        nodeId = arNodeId;
        obNode = Node.getNode(nodeId);
        locked = false;
        lamportTime = 0;
        permission_received_from_quorum = new HashSet<>();

        Comparator<long[]> com = new Comparator<long[]>() {
            @Override
            public int compare(long[] o1, long[] o2) {
                if (o1[0] < o2[0])
                    return -1;
                else if (o1[0] > o2[0])
                    return 1;
                else
                    return (int) (o1[1] - o2[1]);   //de-tier by id
            }
        };
        requestQueue = new PriorityQueue<>(com);

        launch();
    }
    private void launch()	// receive all message, update time when necessary, call Client.send()
                            // check message, call recvGrant(), recvFail(), recvInquire(), recvYield()
    {
        Runnable launch = () -> {
            SocketManager.receive(obNode.port, this);
        };
        new Thread(launch).start();
    }

    public void checkMessage(String arMessage)
    {
        // fromNodeId; scalarTime; messageType
        String[] parts = arMessage.split(";");
        int fromNodeId = Integer.valueOf(parts[0]);
        long scalarTime = Long.valueOf(parts[1]);
        String mType = parts[2];

        if (scalarTime >= lamportTime)
        {
            lamportTime = scalarTime + 1;
        }

        if (mType.equals(MESSAGE_TYPE.REQUEST.getTitle()))
        {
            recvRequest(scalarTime, fromNodeId);
        }
        else if (mType.equals(MESSAGE_TYPE.GRANT.getTitle()))
        {
            recvGrant(fromNodeId);
        }
        else if (mType.equals(MESSAGE_TYPE.RELEASE.getTitle()))
        {
            recvRelease(fromNodeId);
        }
        else if (mType.equals(MESSAGE_TYPE.FAIL.getTitle()))
        {
            recvFail();
        }
        else if (mType.equals(MESSAGE_TYPE.INQUIRE.getTitle()))
        {
            recvInquire();
        }
        else if (mType.equals(MESSAGE_TYPE.YIELD.getTitle()))
        {
            recvYield();
        }
    }

    public void enterCS(double exeTime, App.AppCallback app)	// add to queue, sendRequest
    {
        // add to log file
        String log = nodeId + " request C.S. at lamportTime: " + lamportTime;
        Tool.FileIO.writeFile(log);

        obExeTime = exeTime;
        obAppCallback = app;

        requestQueue.add(new long[]{lamportTime, nodeId});
        if (!locked && nodeId == requestQueue.peek()[1])
        {
            locked = true;                                  // update locked
            permission_received_from_quorum.add(nodeId);
            sendByType(MESSAGE_TYPE.REQUEST);
        }
    }
    private void actualEnterCS()
    {
        // add to log file
        String log = nodeId + " enter C.S. at lamportTime: " + lamportTime + ", exeTime: " + obExeTime;
        Tool.FileIO.writeFile(log);

        // Sleep exeTime, then call leaveCS()
        try {
            Thread.sleep((int) (obExeTime * 1000));
        }
        catch (Exception e) {}

        leaveCS();
    }
    private void leaveCS()	// remove queue, sendRelease, callback App
    {
        if (nodeId == (int)requestQueue.peek()[1])
        {
            // add to log file
            String log = nodeId + " leave C.S. at lamportTime: " + lamportTime;
            Tool.FileIO.writeFile(log);


            locked = false;                                 // update locked
            permission_received_from_quorum.clear();
            requestQueue.remove();
            sendByType(MESSAGE_TYPE.RELEASE);

            if (obAppCallback != null)
            {
                obAppCallback.leaveCS();
            }
        }
    }

    /*********************************************
     * send message logic
     *********************************************/
    // broadcast to every quorum
    private void sendByType(MESSAGE_TYPE arType)
    {
        lamportTime++;
        Set<Integer> quorumSet = obNode.qset;
        for(int qid : quorumSet)
        {
            if (qid == nodeId)          //TODO should ignore itself?
                continue;
            Node qNode = Node.getNode(qid);
            SocketManager.send(qNode.hostname, qNode.port, nodeId, lamportTime, arType.getTitle());
        }
    }
    // send to one quorum
    private void sendByType(MESSAGE_TYPE arType, int dstNodeId)
    {
        lamportTime++;
        Node qNode = Node.getNode(dstNodeId);
        SocketManager.send(qNode.hostname, qNode.port, nodeId, lamportTime, arType.getTitle());
    }


    /*********************************************
     * receive message logic
     *********************************************/
    private void recvRequest(long scalaTime, int fromNodeId)
    {
        requestQueue.add(new long[]{scalaTime, fromNodeId});

        if (!locked && fromNodeId == (int)requestQueue.peek()[1])
        {
            locked = true;
            sendByType(MESSAGE_TYPE.GRANT, fromNodeId);
        }
    }
    private void recvGrant(int fromNodeId)
    {
        permission_received_from_quorum.add(fromNodeId);
        if (permission_received_from_quorum.equals(obNode.qset))
        {
            actualEnterCS();                    // actually enter CS after receive grant from all
        }
    }
    private void recvRelease(int fromNodeId)
    {
        if (fromNodeId == (int)requestQueue.peek()[1])
        {
            locked = false;
            requestQueue.remove();              // should remove top

            if (!requestQueue.isEmpty()) {
                long[] pair = requestQueue.peek();

                locked = true;
                if (nodeId == pair[1])                                  // request for itself
                {
                    permission_received_from_quorum.add(nodeId);
                    sendByType(MESSAGE_TYPE.REQUEST);
                }
                else
                {
                    sendByType(MESSAGE_TYPE.GRANT, (int) pair[1]);     // grant the next process
                }
            }
        }
    }

    /*********************************************
     * preemption logic
     *********************************************/
    private void recvFail(){}
    private void recvInquire(){}
    private void recvYield(){}

}