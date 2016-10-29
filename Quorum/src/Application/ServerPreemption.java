package Application;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiezebin on 10/19/16.
 */
public class ServerPreemption extends ServerBase {

    private Set<Integer> failReceived;                  // should update when received Grant
    private Set<Integer> inquireReceived;               // clear when actual enter cs

    public ServerPreemption(int arNodeId)
    {
        super(arNodeId);
        failReceived = new HashSet<Integer>();
        inquireReceived = new HashSet<Integer>();
    }

    protected void actualEnterCS()
    {
        // discard inquire
        inquireReceived.clear();
        super.actualEnterCS();
    }

    protected void recvRequest(long scalaTime, int fromNodeId)
    {
        requestQueue.add(new long[]{scalaTime, fromNodeId});

        if (fromNodeId == (int)requestQueue.peek()[1])
        {
            if (!locked)
            {
                locked = true;
                nodeLastGrant = fromNodeId;
                sendByType(MESSAGE_TYPE.GRANT, fromNodeId);
            }
            else            // received timestamp is on top of queue, need to Inquire
            {
                if (nodeLastGrant != nodeId)    // 1. send Inquire to one of the membership set
                {
                    sendByType(MESSAGE_TYPE.INQUIRE, nodeLastGrant);
                }
                else if (!actualInCS && !failReceived.isEmpty())
                {                               // 2. has grant itself before, yield if not actual in C.S. and receive fail before
                    permission_received_from_quorum.remove(nodeId);
                    nodeLastGrant = fromNodeId;
                    sendByType(MESSAGE_TYPE.GRANT, fromNodeId);
                }
                                                // 3. if receive fail in the future, surrender its own permission
            }
        }
        else                // received timestamp is not on top of queue, reply Fail
        {
            sendByType(MESSAGE_TYPE.FAIL, fromNodeId);
        }
    }
    protected void recvGrant(int fromNodeId)
    {
        if (failReceived.contains(fromNodeId))
        {
            failReceived.remove(fromNodeId);
        }
        super.recvGrant(fromNodeId);
    }

    protected void recvFail(int fromNodeId)
    {
        failReceived.add(fromNodeId);               // fromNodeId will send Grant back in the future, because the request is saved in queue

        if (!inquireReceived.isEmpty())             // 1. yield back permission
        {
            // Yield
            for (int nid : inquireReceived)
            {
                permission_received_from_quorum.remove(nid);
                sendByType(MESSAGE_TYPE.YIELD, nid);
            }
            inquireReceived.clear();
        }
                                                    // 2. surrender its own permission
        if (nodeLastGrant == nodeId && nodeLastGrant != (int)requestQueue.peek()[1])
        {
            permission_received_from_quorum.remove(nodeId);
            nodeLastGrant = (int)requestQueue.peek()[1];
            sendByType(MESSAGE_TYPE.GRANT, nodeLastGrant);
        }
    }

    protected void recvInquire(int fromNodeId)
    {
        if (actualInCS)
        {
            // discard inquire, will send Release to fromNodeId in the future
        }
        else if (!failReceived.isEmpty())
        {
            // Yield
            permission_received_from_quorum.remove(fromNodeId);
            sendByType(MESSAGE_TYPE.YIELD, fromNodeId);
        }
        else
        {
            // buffer for future process
            inquireReceived.add(fromNodeId);
        }
    }

    protected void recvYield(int fromNodeId)
    {
        if (nodeLastGrant == fromNodeId)                    // permission inquired back
        {                                                   // same as receive Release, but not remove entry in queue
            locked = false;
            handleRequestQueue();
        }
    }

}
