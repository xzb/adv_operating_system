import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
//import com.sun.nio.sctp.*;

/**
 * Created by xiezebin on 10/18/16.
 */
public class Server {

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
    private boolean locked_boolean;
    private int local_time;  	// increase each time of send or receive message
    private Set<Integer> permission_received_from_quorum;
    private PriorityQueue<Integer> requestQueue;

    public Server(int arNodeId)
    {
        nodeId = arNodeId;
        obNode = Node.getNode(nodeId);
        locked_boolean = false;
        local_time = 0;
        permission_received_from_quorum = new HashSet<>();
        requestQueue = new PriorityQueue<>();
    }
    public void launch()	// receive all message, update time when necessary, call Client.send()
                            // check message, call Grant(), Failed(), Inquire(), Yield()
    {
        SocketManager.receive(obNode.port, this);
    }
    public void checkMessage(String arMessage)
    {
        // fromNodeId; scalarTime; messageType
        String[] parts = arMessage.split(";");
        int fromNodeId = Integer.valueOf(parts[0]);
        int scalarTime = Integer.valueOf(parts[1]);
        String mType = parts[2];

        if (mType.equals(MESSAGE_TYPE.REQUEST.getTitle()))
        {

        }
        else if (mType.equals(MESSAGE_TYPE.GRANT.getTitle()))
        {}
        else if (mType.equals(MESSAGE_TYPE.RELEASE.getTitle()))
        {}
        else if (mType.equals(MESSAGE_TYPE.FAIL.getTitle()))
        {}
        else if (mType.equals(MESSAGE_TYPE.INQUIRE.getTitle()))
        {}
        else if (mType.equals(MESSAGE_TYPE.YIELD.getTitle()))
        {}
    }

    public void enterCS()	// add to queue, Request logic
    {
        requestQueue.add(nodeId);
        if (nodeId == requestQueue.peek())
        {
            Request();
        }
    }
    public void leaveCS()	// remove queue, Release logic
    {
        requestQueue.remove(nodeId);
        Release();
    }

    public void Request(){}	// send, add to log.txt
    {
        local_time++;
        Set<Integer> quorumSet = obNode.qset;
        for(int qid : quorumSet)
        {
            Node qNode = Node.getNode(qid);
            SocketManager.send(qNode.hostname, qNode.port, nodeId, local_time, MESSAGE_TYPE.REQUEST.getTitle());
        }
    }
    public void Grant()
    {

    }
    public void Release()	// send, add to log.txt
    {
        local_time++;
        Set<Integer> quorumSet = obNode.qset;
        for(int qid : quorumSet)
        {
            Node qNode = Node.getNode(qid);
            SocketManager.send(qNode.hostname, qNode.port, nodeId, local_time, MESSAGE_TYPE.RELEASE.getTitle());
        }
    }
    public void Fail(){}
    public void Inquire(){}
    public void Yield(){}

}
