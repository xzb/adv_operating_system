package Application;

import Tool.SocketManager;

/**
 * Created by xiezebin on 11/13/16.
 */
public class Server {

    enum MESSAGE {
        APPLICATION("APPLICATION"),
        FREEZE_SEND("FREEZE_SEND"),
        FREEZE_COMPLETE("FREEZE_COMPLETE"),
        UNFREEZE("UNFREEZE"),
        REPLY("REPLY"),
        CHECKPOINT("CHECKPOINT"),
        RECOVERY("RECOVERY");

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
                SocketManager.receive(obNode.port, new SocketManager.ServerCallback() {
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
        // fromNodeId; scalarTime; messageType
        String[] parts = arMessage.split(";");
        int fromNodeId = Integer.valueOf(parts[0]);
        String vectorTime = parts[1];
        String mType = parts[2];



        if (mType.equals(MESSAGE.APPLICATION.getT()))
        {
            // todo update vector time
            receiveApplication(fromNodeId);
        }
        else if (mType.equals(MESSAGE.FREEZE_SEND.getT()))
        {
        }
        else if (mType.equals(MESSAGE.FREEZE_COMPLETE.getT()))
        {
        }
        else if (mType.equals(MESSAGE.UNFREEZE.getT()))
        {
        }
        else if (mType.equals(MESSAGE.REPLY.getT()))
        {
        }
        else if (mType.equals(MESSAGE.CHECKPOINT.getT()))
        {
        }
        else if (mType.equals(MESSAGE.RECOVERY.getT()))
        {
        }
    }

    public void receiveApplication(int fromNodeId)
    {
        obNode.clock[fromNodeId]++;

    }
}
