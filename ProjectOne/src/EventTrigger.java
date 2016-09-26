/**
 * Created by xiezebin on 9/25/16.
 */
public class EventTrigger {

    public static String getInitMsg(Node arNode)
    {
        int startLabel = arNode.getLabel();
        return new Message(arNode.getPath(), "", 0).serialize();
    }

    public static void main(String[] args) {

        if (args.length < 2)
        {
            System.out.println("# Please add serverId, configFile.");
            System.exit(1);
        }

        int loNodeId = Integer.valueOf(args[0]);
        FileIO.CONFIG_FILE = args[1];
        Node loNode = Node.getNode(loNodeId);
        String loStartMsg = getInitMsg(loNode);

        Message loMessage = Message.deSerialize(loStartMsg);
        Node loNextNode = Node.getNode(loMessage.getNextNodeId());
        SocketManager.send(loNextNode.getHostname(), loNextNode.getPort(), loMessage.serialize());

        //ServerLauncher server = new ServerLauncher(loNodeId);
        //server.checkMessage(loStartMsg);
    }
}
