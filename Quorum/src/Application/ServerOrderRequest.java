package Application;

import Tool.SocketManager;


import java.util.*;
import Tool.SocketManager;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
/**
 * Created by on 10/19/16.yql赶紧做！
 */
public class ServerOrderRequest extends ServerBase  {

/***
    protected int nodeId;
    protected Node obNode;
    protected HashSet<Integer> permission_received_from_quorum;
    protected PriorityQueue<long[]> requestQueue;              //<timestamp, id>
    protected double obExeTime;
    protected App.AppCallback obAppCallback;
    private Set<Integer> qSet;
    private  Set<Integer> mSet;
    private ArrayList<Integer> qSet1;
    private ArrayList<Integer> mSet1;

    private boolean[] checkGrant;
    boolean lock;
    protected int index=0;
    protected int nodeLastGrant=0;
    //private Comparator compa;
***/
    private ArrayList<Integer> qSet1;
    private ArrayList<Integer> mSet1;
    private Set<Integer> qSet;
    private  Set<Integer> mSet;
    private Queue<Integer> reQueue;
    private int index;
    public ServerOrderRequest(int arNodeId)
    {

       super(arNodeId);
        this.index=0;
        Node obNode=Node.getNode(arNodeId);
        qSet=obNode.qset;
        mSet=obNode.mset;
        reQueue = new LinkedList<>();
        qSet1=new ArrayList<>();
        mSet1=new ArrayList<>();
        permission_received_from_quorum=new HashSet<>() ;
        for(int i:qSet){
            qSet1.add(i);
        }
        for(int i:mSet){
            mSet1.add(i);
        }
        reQueue=new LinkedList<>();


        System.out.println(arNodeId+"：qset:"+qSet1.get(0)+"--"+qSet1.get(1));



        Collections.sort(qSet1);
        Collections.sort(mSet1);



        //checkGrant=new boolean[qSet.size()];
        //this.lock=false;

        //launch();
    }
/*
/***
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






private void launch()	// receive all message, update time when necessary, call Client.send()
// check message, call recvGrant(), recvFail(), recvInquire(), recvYield()
{
    Runnable launch = new Runnable() {
        public void run() {
            SocketManager.receive(obNode.port, new ServerBase.ServerCallback() {

                public void call(String message) {
                    checkMessage(message);
                }
            });
        }
    };
    new Thread(launch).start();
}****/





@Override
    protected void checkMessage(String arMessage)
    {
        // fromNodeId; scalarTime; messageType
        String[] parts = arMessage.split(";");
        int fromNodeId = Integer.valueOf(parts[0]);
        long scalarTime = Long.valueOf(parts[1]);
        String mType = parts[2];



        if (mType.equals(MESSAGE_TYPE.REQUEST.getTitle()))
        {
            recvRequest(fromNodeId);
        }
        else if (mType.equals(MESSAGE_TYPE.GRANT.getTitle()))
        {
            recvGrant(fromNodeId);
        }
        else if (mType.equals(MESSAGE_TYPE.RELEASE.getTitle()))
        {
            recvRelease(fromNodeId);
        }

    }
   @Override
    protected void sendRequest(long lamport)           // Request timestamp is critical
    {

        int toID=qSet1.get(index);
        System.out.println("send retuest to NO."+toID);
            Node qNode = Node.getNode(toID);
            SocketManager.send(qNode.hostname, qNode.port, nodeId, lamport, MESSAGE_TYPE.REQUEST.getTitle());


                     // increment after send, piggyback old value
    }

    @Override
    public void enterCS(double exeTime, App.AppCallback app)	// add to queue, sendRequest
    {
        // add to log file
        String log = nodeId + " request C.S.";
        Tool.FileIO.writeFile(log);

        obExeTime = exeTime;
        obAppCallback = app;

        reQueue.add(nodeId);
        handleRequestQueue();

        // if is locked, can still send Request
        sendRequest(0);
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
                String log = nodeId + " enter C.S. " + ", exeTime: " + obExeTime;
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

    private void leaveCS()	// remove queue, sendRelease, callback App
    {
        actualInCS = false;

        // add to log file
        String log = nodeId + " leave C.S. ";
        Tool.FileIO.writeFile(log);

        locked = false;                                 // update locked
       index=0;
       // permission_received_from_quorum.clear();

        int removeEntry = 0;
        for(int entry : reQueue)
        {
            if (entry == nodeId)
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



    @Override
    protected void handleRequestQueue()
    {System.out.println("node:"+nodeId+"   qset1 size="+qSet1.size()+"index:"+index);
        System.out.println("node:"+nodeId+";lock="+locked);
        if (!locked && !reQueue.isEmpty()) {

            locked = true;
            int reqId = (int) reQueue.poll();
            System.out.println("node:"+nodeId+"   chuli:"+reqId+"  ;index:"+index);
            nodeLastGrant = reqId;


            if (reqId == nodeId)
            {
               index++;  // grant itself
                System.out.println("qset1 size="+qSet1.size());
                if (index==qSet1.size())
                {   System.out.println("!!!!!!qset1 size="+index);
                    actualEnterCS();                            // if Release or Yield message is the last permission that need to enter CS
                }else {sendRequest(0); }
                handleRequestQueue();
            }
            else if(reqId!=nodeId)
            {
                sendByType(MESSAGE_TYPE.GRANT, reqId);          // grant the next process
            }handleRequestQueue();
        }
    }
  /***
    @Override
    protected void sendByType(MESSAGE_TYPE arType) {

        int toID=qSet1.get(index);
        Node qNode = Node.getNode(toID);

        SocketManager.send(qNode.hostname, qNode.port, nodeId, 0, arType.getTitle());


        // increment after send, piggyback old value
    }***/

    @Override
    protected void sendByType(MESSAGE_TYPE arType, int dstNodeId)
    {   //if(arType.getTitle().equals("GRANT")){
        System.out.println("281 HANG");
    //}

        Node qNode = Node.getNode(dstNodeId);
        SocketManager.send(qNode.hostname, qNode.port, nodeId, 0, arType.getTitle());

    }

    protected void recvRequest(int fromNodeId)
    {
        reQueue.add(fromNodeId);
        System.out.println(nodeId+" from id:"+fromNodeId);
        handleRequestQueue();
    }

    @Override
    protected void recvGrant(int fromNodeId)
    {
        index++;
        System.out.println("Grant from:" +fromNodeId);



        if (index==qSet1.size())
        {   index=0;
            actualEnterCS();                    // actually enter CS after receive grant from all
        }
        else {
            int toID=qSet1.get(index);
            Node qNode = Node.getNode(toID);
            SocketManager.send(qNode.hostname, qNode.port, nodeId, 0, "REQUEST");handleRequestQueue();}
    }

    @Override
    protected void recvRelease(int fromNodeId)
    {

            locked = false;

            int removeEntry = 999;
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
            handleRequestQueue();
    }
@Override
protected void sendByType(MESSAGE_TYPE arType)
{
    Set<Integer> quorumSet = obNode.qset;
    long currentTime = 0;                     // prevent change when broadcasting
    for(int qid : quorumSet)
    {

        Node qNode = Node.getNode(qid);
        SocketManager.send(qNode.hostname, qNode.port, nodeId, currentTime, arType.getTitle());
    }

                      // increment after send, piggyback old value
}

/****

    //===========
    private void sendRequest(int in){
             //TODO 可以改成挨个删除？

        int nextId=qSet1.get(in);
        Node qNode=Node.getNode(nextId);
        sendByType(ServerBase.MESSAGE_TYPE.REQUEST,nextId);
        //SocketManager.send(qNode.hostname, qNode.port, nodeId, );



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
            sendByType(ServerBase.MESSAGE_TYPE.RELEASE,qid);
           // SocketManager.send(qNode.hostname, qNode.port, nodeId,  "RELEASE");


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

                int reqId = reQueue.poll();
                nodeLastGrant = reqId;

                if (nodeId == reqId)
                {   lock = true;
                    permission_received_from_quorum.add(nodeId);    // grant itself
                    if (permission_received_from_quorum.equals(qSet1))
                    {
                        actualEnterCS();
                        // if Release or Yield message is the last permission that need to enter CS
                    }

                    else{index++; sendRequest(index);}
                }
                else{
                    lock = true;
                    sendByType(ServerBase.MESSAGE_TYPE.GRANT, reqId);   }       // grant the next process

            }


    }



    private void firstRequest(){

      index=0;

       // reQueue.add(qSet1.get(index));

        sendFirst(qSet1.get(index));

    }
    public void enterCS(double exeTime, App.AppCallback app)	// add to queue, sendRequest
    {
        // add to log file
        String log = nodeId + " request C.S.  " ;
        Tool.FileIO.writeFile(log);

        obExeTime = exeTime;
        obAppCallback = app;

        reQueue.add(nodeId);
        handleRequest();

        // if is locked, can still send Request
        firstRequest();
    }



    private void sendFirst(int toId){
        Node toNode= Node.getNode(toId);
        sendByType(ServerBase.MESSAGE_TYPE.REQUEST,toId);
        //SocketManager.send(toNode.hostname, toNode.port, nodeId, "REQUEST");

    }



    protected void recvRequest( int fromNodeId)
    {
        reQueue.add(fromNodeId);


        handleRequest();
    }
    protected void recvGrant(int fromNodeId)
    {   System.out.println("Node"+nodeId+" receive grant from Node "+fromNodeId+"haha:"+index);
        permission_received_from_quorum.add(fromNodeId);
        if (permission_received_from_quorum.equals(qSet))
        {
            actualEnterCS();                    // actually enter CS after receive grant from all
        }
        else
        if(index<1)index++;
        sendRequest(fromNodeId);


    }
    protected void recvRelease(int fromNodeId)
    {
        if (fromNodeId == nodeLastGrant)            // may not on top of queue, should check previous grant
        {
            lock = false;

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

            handleRequest();
        }
    }



   // @Override
    protected void actualEnterCS()
    {
        // should not stop server receiving
        Runnable event = new Runnable() {
            @Override
            public void run() {

               // actualInCS = true;

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

    ***/

}
