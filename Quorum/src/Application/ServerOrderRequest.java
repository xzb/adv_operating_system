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

        launch();
    }

    private void launch()	// receive all message, update time when necessary, call Client.send()
    // check message, call recvGrant(), recvFail(), recvInquire(), recvYield()
    {
        Runnable launch = new Runnable() {
            @Override
            public void run() {
                SocketManager.receive(obNode.port, new ServerBase.ServerCallback() {
                    @Override
                    public void call(String message) {
                        checkMessage(message);
                    }
                });
            }
        };
        new Thread(launch).start();
    }

    public interface ServerCallback {
        void call(String message);
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
        Set<Integer> quorumSet = obNode.qset;           //TODO 可以改成挨个删除？
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


    protected void sendRelease()
    {
        Set<Integer> quorumSet = obNode.qset;
        int i=0;
        for(int qid : quorumSet)
        {
            if (qid == nodeId)          //TODO should ignore itself? when exe leaveCS lock=false and handlerequeue
                continue;
            Node qNode = Node.getNode(qid);
            SocketManager.send(qNode.hostname, qNode.port, nodeId,  "RELEASE");


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

            if (!lock&&!reQueue.isEmpty()) {
                lock = true;
                int reqId = reQueue.poll();
                nodeLastGrant = reqId;

                if (nodeId == reqId)
                {
                    permission_received_from_quorum.add(nodeId);    // grant itself
                    if (permission_received_from_quorum.equals(obNode.qset))
                    {
                        actualEnterCS();                            // if Release or Yield message is the last permission that need to enter CS
                    }
                    else sendRequest(nodeId);
                }
                else
                if (nodeId != reqId)
                    sendByType(ServerBase.MESSAGE_TYPE.GRANT, reqId);          // grant the next process

            }


    }



    private void firstRequest(){


        lock=true;
        for(int i:qSet){
            sendFirst(i);
            break;
        }
    }




    private void sendFirst(int toId){
        Node toNode= Node.getNode(toId);
        SocketManager.send(toNode.hostname, toNode.port, nodeId, "REQUEST");

    }



    protected void recvRequest( int fromNodeId)
    {
        reQueue.add(fromNodeId);

        handleRequest();
    }
    protected void recvGrant(int fromNodeId)
    {
        permission_received_from_quorum.add(fromNodeId);
        if (permission_received_from_quorum.equals(obNode.qset))
        {
            actualEnterCS();                    // actually enter CS after receive grant from all
        }
        else
        sendRequest(fromNodeId);

    }
    protected void recvRelease(int fromNodeId)
    {
        if (fromNodeId == nodeLastGrant)            // may not on top of queue, should check previous grant
        {
            locked = false;

            int removeEntry=9999;
            for(int entry : reQueue)        //TODO why have this
            {
                if (entry == fromNodeId)
                {
                    removeEntry = entry;
                    break;
                }
            }
            if(removeEntry!=9999)
            reQueue.remove(removeEntry);       // should remove related entry

            handleRequestQueue();
        }
    }



    @Override
    protected void actualEnterCS()
    {
        // should not stop server receiving
        Runnable event = new Runnable() {
            @Override
            public void run() {

                actualInCS = true;

                // add to log file
                String log = nodeId + " enter C.S. , exeTime: " + obExeTime;
                Tool.FileIO.writeFile(log);

                // Sleep exeTime, then call leaveCS()
                try {
                    Thread.sleep((int) (obExeTime * 1000));
                } catch (Exception e) {
                }

                leaveCS();
            }
        };
        new Thread(event).start();
    }
    private void leaveCS(){
        String log ="Node"+ nodeId + " leave C.S. ";
        Tool.FileIO.writeFile(log);
        lock=false;
        permission_received_from_quorum.clear();
        int temp=999;
        for(int i : reQueue) {
            if (i == nodeId) {
                temp = i;
            }
            break;
        }
         if(temp!=999) reQueue.remove(temp);
        sendRelease();
        handleRequest();
    }
}
