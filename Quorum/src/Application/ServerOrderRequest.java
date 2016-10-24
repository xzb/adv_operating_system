package Application;

import Tool.SocketManager;
import sun.jvm.hotspot.runtime.Threads;

import java.util.*;

/**
 * Created by on 10/19/16.yql赶紧做！
 */
public class ServerOrderRequest extends ServerBase {

    enum MESSAGE_TYPE {
        REQUEST("REQUEST"),
        GRANT("GRANT"),
        RELEASE("RELEASE");

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
    protected Set<Integer> permission_received_from_quorum;
    protected PriorityQueue<long[]> requestQueue;              //<timestamp, id>
    protected double obExeTime;
    protected App.AppCallback obAppCallback;
    private Set<Integer> qSet;
    private  Set<Integer> mSet;
private Queue<Integer> reQueue;
    private boolean[] checkGrant;
    boolean lock;


    public ServerOrderRequest(int arNodeId)
    {
       super(arNodeId);
        qSet=super.obNode.qset;
        mSet=super.obNode.mset;
        checkGrant=new boolean[qSet.size()];
        this.lock=false;


    }

    private void checkMessage(String arMessage){
        String[] parts = arMessage.split(";");
        int fromNodeId = Integer.valueOf(parts[0]);
      //  long scalarTime = Long.valueOf(parts[1]);
        String mType = parts[1];
        if (mType.equals(ServerOrderRequest.MESSAGE_TYPE.REQUEST.getTitle()))
        {
            recvRequest(fromNodeId);



        }
        else if (mType.equals(ServerOrderRequest.MESSAGE_TYPE.GRANT.getTitle()))
        {
            recvGrant(fromNodeId);
           // sendRequest(fromNodeId);
        }
        else if (mType.equals(ServerOrderRequest.MESSAGE_TYPE.RELEASE.getTitle()))
        {
            recvRelease(fromNodeId);
        }
    }




    //===========
    private void sendRequest(int lastId){
        Set<Integer> quorumSet = obNode.qset;
        int i=0;
        int nextId=0;
        Iterator it = quorumSet.iterator();
        while(!it.equals(lastId)&&it.hasNext()){
            it.next();
            i++;
        }
        if(it.equals(lastId)){

            nextId= (int) it.next();
        }
        Node qNode = Node.getNode(nextId);
       if(checkGrant[i]){
           SocketManager.send(qNode.hostname, qNode.port, nodeId,  "REQUEST");
       }


    }


    protected void sendRelease(int toId)
    {
        Set<Integer> quorumSet = obNode.qset;
        int i=0;
        for(int qid : quorumSet)
        {
            if (qid == nodeId)          //TODO should ignore itself?
                continue;
            Node qNode = Node.getNode(qid);
            SocketManager.send(qNode.hostname, qNode.port, nodeId,  "RELEASE");
            if((checkGrant[i])==true){
                continue;
            }
        }

        //lamportTime++;                  // increment after send, piggyback old value
    }
    // send to one quorum
    protected void sendByType(ServerBase.MESSAGE_TYPE arType, int dstNodeId)
    {
        Node qNode = Node.getNode(dstNodeId);
        SocketManager.send(qNode.hostname, qNode.port, nodeId, arType.getTitle());

        //lamportTime++;
    }

    //=======handle request
    private void handleRequest(){
        while(lock){

        }
            if (!reQueue.isEmpty()) {
                int reqId = reQueue.poll();
                nodeLastGrant = reqId;

                lock = true;
                if (nodeId == reqId)
                {
                    permission_received_from_quorum.add(nodeId);    // grant itself
                    if (permission_received_from_quorum.equals(obNode.qset))
                    {
                        actualEnterCS();                            // if Release or Yield message is the last permission that need to enter CS
                    }
                }
                else
                if (nodeId != reqId)
                    sendByType(ServerBase.MESSAGE_TYPE.GRANT, reqId);          // grant the next process

            }


    }
    private void handleGrant(){

    }


    private void firstRequest(){
        while(lock){

        }
        lock=true;
        for(int i:qSet){
            sendFirst(i);
            break;
        }
    }




    private void sendFirst(int toId){
        Node toNode= Node.getNode(toId);
        SocketManager.send(toNode.hostname, toNode.port, nodeId,  "REQUEST");

    }



    protected void recvRequest( int fromNodeId)
    {
        reQueue.add(fromNodeId);

        handleRequest();
    }
    protected void recvGrant(int fromNodeId)
    {
        permission_received_from_quorum.add(fromNodeId);

        sendRequest(fromNodeId);
        if (permission_received_from_quorum.equals(obNode.qset))
        {
            actualEnterCS();                    // actually enter CS after receive grant from all
        }
    }
    protected void recvRelease(int fromNodeId)
    {
        if (fromNodeId == nodeLastGrant)            // may not on top of queue, should check previous grant
        {
            locked = false;

            int removeEntry=0;
            for(int entry : reQueue)
            {
                if (entry == fromNodeId)
                {
                    removeEntry = entry;
                    break;
                }
            }
            reQueue.remove(removeEntry);       // should remove related entry

            handleRequestQueue();
        }
    }



    @Override
    public void enterCS(double exeTime, App.AppCallback app)
    {

    }
}
