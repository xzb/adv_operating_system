package Application;

import Tool.SocketManager;

/**
 * Created by xiezebin on 11/13/16.
 */
public class Server {

    enum MESSAGE {
        APPLICATION("APPLICATION"),

        CHECKPOINT("CHECKPOINT"),
        RECOVERY("RECOVERY"),
        UNFREEZE("UNFREEZE"),

        FREEZE_REPLY("FREEZE_REPLY"),
        UNFREEZE_REPLY("UNFREEZE_REPLY"),

        OPERATION_COMPLETE("OPERATION_COMPLETE");

        private String title;
        private MESSAGE(String arTitle) {
            title = arTitle;
        }
        public String getT() {
            return this.title;
        }
    }

    protected Node obNode;


    public Server(int nodeId)
    {
        obNode = Node.getNode(nodeId);

        launch();
    }
    private void launch()
    {
        Runnable launch = new Runnable() {
            @Override
            public void run() {
                SocketManager.receive(obNode.id, obNode.port, new SocketManager.ServerCallback() {
                    @Override
                    public void call(String message) {
                        checkMessage(message);
                    }
                });
            }
        };
        new Thread(launch).start();
    }


    /*********************************************
     * when receive message, check type and dispatch event
     *********************************************/
    protected void checkMessage(String arMessage)
    {
        // fromNodeId; piggyback; messageType
        String[] parts = arMessage.split(";");
        int fromNodeId = Integer.valueOf(parts[0]);
        int piggyback = Integer.valueOf(parts[1]);
        String mType = parts[2];


        if (mType.equals(MESSAGE.APPLICATION.getT()))
        {
            RandomMessage.receiveApplication(obNode.id, fromNodeId, piggyback);
        }
        else if (mType.equals(MESSAGE.CHECKPOINT.getT()))
        {
            Checkpoint.ins(obNode.id).receiveCheckpoint(fromNodeId, piggyback);
        }
        else if (mType.equals(MESSAGE.RECOVERY.getT()))
        {
            //Checkpoint.ins(obNode.id).receiveRecovery(fromNodeId, piggyback);
        }
        else if (mType.equals(MESSAGE.UNFREEZE.getT()))
        {
            Checkpoint.ins(obNode.id).receiveUnfreeze(fromNodeId);
        }
        else if (mType.equals(MESSAGE.FREEZE_REPLY.getT()))
        {
            Checkpoint.ins(obNode.id).receiveFreezeReply(fromNodeId);
        }
        else if (mType.equals(MESSAGE.UNFREEZE_REPLY.getT()))
        {
            Checkpoint.ins(obNode.id).receiveUnfreezeReply(fromNodeId);
        }
        else if (mType.equals(MESSAGE.OPERATION_COMPLETE.getT()))
        {
            //Checkpoint.ins(obNode.id).receiveOperationComplete(fromNodeId);
        }
    }




}
