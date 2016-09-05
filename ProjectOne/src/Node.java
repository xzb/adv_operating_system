import java.util.Random;

/**
 * Created by xiezebin on 9/4/16.
 */
public class Node
{
    private int obId;
    private String obHostname;
    private int obPort;
    private String obPath;
    private int obLabel;

    public Node(int arId)
    {
        obId = arId;
        obLabel = new Random().nextInt(100);        // select a label value when initializing

        switch (arId)
        {
            case 0:
                obHostname = "dc01.utdallas.edu";
                obPort = 3332;
                obPath = "0,1,2,3,4,0";
                break;
            case 1:
                obHostname = "dc33.utdallas.edu";
                obPort = 5678;
                obPath = "1,3,2,4,1";
                break;
            case 2:
                obHostname = "dc21.utdallas.edu";
                obPort = 5231;
                obPath = "2,1,2,3,4,0,2";
                break;
            case 3:
                obHostname = "dc33.utdallas.edu";
                obPort = 2311;
                obPath = "3,4,0,1,2,3";
                break;
            case 4:
                obHostname = "dc22.utdallas.edu";
                obPort = 3124;
                obPath = "4,1,2,3,2,3,1,4";
                break;
            default:
        }

    }

    public int getPort()
    {
        return obPort;
    }
    public String getHostname()
    {
        return obHostname;
    }
    public int getId() {
        return obId;
    }
    public int getLabel() {
        return obLabel;
    }
    public String getPath() {
        return obPath;
    }
}
