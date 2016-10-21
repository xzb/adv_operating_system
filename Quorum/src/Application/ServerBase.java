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

    protected int nodeId;
    protected Node obNode;
    protected boolean locked;
    protected boolean actualInCS;   // for discarding inquire
    protected int nodeLastGrant;    // save the node last granted, only need to inquire this node if needed
    private long lamportTime;       // increase each time of send or receive message
    protected Set<Integer> permission_received_from_quorum;
    protected PriorityQueue<long[]> requestQueue;              //<timestamp, id>

    protected double obExeTime;
    protected App.AppCallback obAppCallback;

    public ServerBase(int arNodeId)
    {
        nodeId = arNodeId;
        obNode = Node.getNode(nodeId);
        locked = false;
        actualInCS = false;
        nodeLastGrant = -1;
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
            SocketManager.receive(obNode.port, new ServerCallback() {
                @Override
                public void call(String message) {
                    checkMessage(message);
                }
            });
        };
        new Thread(launch).start();
    }

    public interface ServerCallback {
        void call(String message);
    }
    private void checkMessage(String arMessage)
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
        else
        {
            lamportTime++;                      // record receive event
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
            recvFail(fromNodeId);
        }
        else if (mType.equals(MESSAGE_TYPE.INQUIRE.getTitle()))
        {
            recvInquire(fromNodeId);
        }
        else if (mType.equals(MESSAGE_TYPE.YIELD.getTitle()))
        {
            recvYield(fromNodeId);
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
        handleRequestQueue();

        // if is locked, can still send Request
        sendByType(MESSAGE_TYPE.REQUEST);
    }
    protected void actualEnterCS()
    {
        // should not stop server receiving
        Runnable event = () -> {

            actualInCS = true;

            // add to log file
            String log = nodeId + " enter C.S. at lamportTime: " + lamportTime + ", exeTime: " + obExeTime;
            Tool.FileIO.writeFile(log);

            // Sleep exeTime, then call leaveCS()
            try {
                Thread.sleep((int) (obExeTime * 1000));
            } catch (Exception e) {
            }

            leaveCS();

        };
        new Thread(event).start();
    }
    private void leaveCS()	// remove queue, sendRelease, callback App
    {
        actualInCS = false;

        // add to log file
        String log = nodeId + " leave C.S. at lamportTime: " + lamportTime;
        Tool.FileIO.writeFile(log);

        locked = false;                                 // update locked
        permission_received_from_quorum.clear();

        long[] removeEntry = null;
        for(long[] entry : requestQueue)
        {
            if (entry[1] == nodeId)
            {
                removeEntry = entry;
                break;
            }
        }
        requestQueue.remove(removeEntry);               // should remove related entry

        sendByType(MESSAGE_TYPE.RELEASE);
        handleRequestQueue();                           // grant previous request here

        if (obAppCallback != null)
        {
            obAppCallback.leaveCS();
        }
    }

    protected void handleRequestQueue()
    {
        if (!locked && !requestQueue.isEmpty()) {
            int reqId = (int) requestQueue.peek()[1];

            locked = true;
            if (nodeId == reqId)
            {
                permission_received_from_quorum.add(nodeId);    // grant itself
                if (permission_received_from_quorum.equals(obNode.qset))
                {
                    actualEnterCS();                            // if Release or Yield message is the last permission that need to enter CS
                }
            }
            else
            {
                nodeLastGrant = reqId;
                sendByType(MESSAGE_TYPE.GRANT, reqId);          // grant the next process
            }
        }
    }

    /*********************************************
     * send message logic
     *********************************************/
    // broadcast to every quorum
    protected void sendByType(MESSAGE_TYPE arType)
    {
        Set<Integer> quorumSet = obNode.qset;
        for(int qid : quorumSet)
        {
            if (qid == nodeId)          //TODO should ignore itself?
                continue;
            Node qNode = Node.getNode(qid);
            SocketManager.send(qNode.hostname, qNode.port, nodeId, lamportTime, arType.getTitle());
        }

        lamportTime++;                  // increment after send, piggyback old value
    }
    // send to one quorum
    protected void sendByType(MESSAGE_TYPE arType, int dstNodeId)
    {
        Node qNode = Node.getNode(dstNodeId);
        SocketManager.send(qNode.hostname, qNode.port, nodeId, lamportTime, arType.getTitle());

        lamportTime++;
    }


    /*********************************************
     * receive message logic
     *********************************************/
    protected void recvRequest(long scalaTime, int fromNodeId)
    {
        requestQueue.add(new long[]{scalaTime, fromNodeId});

        handleRequestQueue();
    }
    protected void recvGrant(int fromNodeId)
    {
        permission_received_from_quorum.add(fromNodeId);
        if (permission_received_from_quorum.equals(obNode.qset))
        {
            actualEnterCS();                    // actually enter CS after receive grant from all
        }
    }
    private void recvRelease(int fromNodeId)
    {
        if (fromNodeId == nodeLastGrant)            // may not on top of queue, should check previous grant
        {
            locked = false;

            long[] removeEntry = null;
            for(long[] entry : requestQueue)
            {
                if (entry[1] == fromNodeId)
                {
                    removeEntry = entry;
                    break;
                }
            }
            requestQueue.remove(removeEntry);       // should remove related entry

            handleRequestQueue();
        }
    }

    /*********************************************
     * preemption logic
     *********************************************/
    protected void recvFail(int fromNodeId){}
    protected void recvInquire(int fromNodeId){}
    protected void recvYield(int fromNodeId){}

}
