/**
 * Created by xiezebin on 9/2/16.
 */

public class Message
{
    public final static String COMPLETE = "COMPLETE";

    private String obWholePath;
    private String obHasVisit;
    private int obLabelSum;

    public Message(String arWholePath, String arStartPath, int arStartLabel)
    {
        obWholePath = arWholePath;
        obHasVisit = arStartPath;
        obLabelSum = arStartLabel;
    }

    public String getWholePath() {
        return obWholePath;
    }

    public String getHasVisit() {
        return obHasVisit;
    }

    public int getLabelSum() {
        return obLabelSum;
    }

    public void addNode(int arNodeId, int arLabel)
    {
        if (obHasVisit.length() == 0)
        {
            obHasVisit += arNodeId;
        }
        else
        {
            obHasVisit = obHasVisit + "," + arNodeId;
        }
        obLabelSum += arLabel;
    }

    public int getNextNodeId()
    {
        String[] loWholePaths = obWholePath.split(",");
        if (obHasVisit.length() == 0)
        {
            return Integer.valueOf(loWholePaths[0]);
        }

        String[] loVisitedPaths = obHasVisit.split(",");
        return Integer.valueOf(loWholePaths[loVisitedPaths.length]);    //check
    }

    public String serialize()
    {
        // Format
        return obWholePath + ";" + obHasVisit + ";" + obLabelSum;
    }
    public static Message deSerialize(String arMsg)
    {
        String[] parts = arMsg.split(";");
        if(parts.length < 3)
        {
            return null;
        }

        return new Message(parts[0], parts[1], Integer.valueOf(parts[2]));
    }
}
