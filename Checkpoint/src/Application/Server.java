package Application;

import Tool.SocketManager;

/**
 * Created by xiezebin on 11/13/16.
 */
public class Server {

    enum MESSAGE {
        APPLICATION("APPLICATION"),

        CHECKPOINT("CHECKPOINT"),
        CHECKPOINT_CONFIRM("CHECKPOINT_CONFIRM"),
        CHECKPOINT_REPLY("CHECKPOINT_REPLY"),
        CHECKPOINT_CONFIRM_REPLY("CHECKPOINT_CONFIRM_REPLY"),

        RECOVERY("RECOVERY"),
        RECOVERY_CONFIRM("RECOVERY_CONFIRM"),
        RECOVERY_REPLY("RECOVERY_REPLY"),
        RECOVERY_CONFIRM_REPLY("RECOVERY_CONFIRM_REPLY"),

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
            RandomMessage.ins(obNode.id).receiveApplication(fromNodeId, piggyback);
        }

        else if (mType.equals(MESSAGE.CHECKPOINT.getT()))
        {
            Checkpoint.ins(obNode.id).receiveCheckpoint(fromNodeId, piggyback);
        }
        else if (mType.equals(MESSAGE.CHECKPOINT_CONFIRM.getT()))
        {
            Checkpoint.ins(obNode.id).receiveCheckpointConfirm(fromNodeId);
        }
        else if (mType.equals(MESSAGE.CHECKPOINT_REPLY.getT()))
        {
            Checkpoint.ins(obNode.id).receiveCheckpointReply(fromNodeId);
        }
        else if (mType.equals(MESSAGE.CHECKPOINT_CONFIRM_REPLY.getT()))
        {
            Checkpoint.ins(obNode.id).receiveCheckpointConfirmReply(fromNodeId);
        }

        else if (mType.equals(MESSAGE.RECOVERY.getT()))
        {
            Recovery.ins(obNode.id).receiveRecovery(fromNodeId, piggyback);
        }
        else if (mType.equals(MESSAGE.RECOVERY_CONFIRM.getT()))
        {
            Recovery.ins(obNode.id).receiveRecoveryConfirm(fromNodeId);
        }
        else if (mType.equals(MESSAGE.RECOVERY_REPLY.getT()))
        {
            Recovery.ins(obNode.id).receiveRecoveryReply(fromNodeId);
        }
        else if (mType.equals(MESSAGE.RECOVERY_CONFIRM_REPLY.getT()))
        {
            Recovery.ins(obNode.id).receiveRecoveryConfirmReply(fromNodeId);
        }

        else if (mType.equals(MESSAGE.OPERATION_COMPLETE.getT()))
        {
            Daemon.ins(obNode.id).receiveOperationComplete(fromNodeId);
        }
    }




}
