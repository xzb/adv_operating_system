package Application;

import Tool.SocketManager;

/**
 * Created by xiezebin on 11/13/16.
 */
public class Server {

    enum MESSAGE {
        APPLICATION("APPLICATION"),
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
        // fromNodeId; piggyback; messageType
        String[] parts = arMessage.split(";");
        int fromNodeId = Integer.valueOf(parts[0]);
        int piggyback = Integer.valueOf(parts[1]);
        String mType = parts[2];


        if (mType.equals(MESSAGE.APPLICATION.getT()))
        {
            receiveApplication(fromNodeId, piggyback);
        }
        else if (mType.equals(MESSAGE.CHECKPOINT.getT()))
        {
            receiveCheckpoint(fromNodeId);
        }
        else if (mType.equals(MESSAGE.RECOVERY.getT()))
        {
            receiveRecovery(fromNodeId);
        }
        else if (mType.equals(MESSAGE.REPLY.getT()))
        {
            receiveReply(fromNodeId);
        }
        else if (mType.equals(MESSAGE.UNFREEZE.getT()))
        {
            receiveUnfreeze(fromNodeId);
        }
    }

    // update receive clock, LLR
    public void receiveApplication(int fromNodeId, int label)
    {
        obNode.clock[fromNodeId]++;
        obNode.LLR[fromNodeId] = label;         // label is monotonically increasing

    }

    public void receiveCheckpoint(int fromNodeId)
    {

    }
    public void receiveRecovery(int fromNodeId)
    {

    }
    public void receiveReply(int fromNodeId)
    {

    }

    // confirm checkpoint
    // todo update LLS, reset FLS, LLR
    public void receiveUnfreeze(int fromNodeId)
    {

    }
}
