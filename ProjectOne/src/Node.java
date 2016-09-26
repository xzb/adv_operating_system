import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

    private static Map<Integer, Node> obPool;

    public Node(int arNodeId, String arHostname, int arPort, String arPath)
    {
        obId = arNodeId;
        obHostname = arHostname;
        obPort = arPort;
        obPath = obId + "," + arPath + "," + obId;          // add head and tail
        obLabel = new Random().nextInt(10) + 1;        // select a label in [1,10] when initializing

        //System.out.println("# INITIAL NODE: " + arNodeId + ";" + arHostname + ";" + arPort + ";" + arPath + ";");
    }

    public static Node getNode(int arNodeId)
    {
        if (obPool == null)
        {
            obPool = new HashMap<>();
        }
        if (!obPool.containsKey(arNodeId))
        {
            FileIO.readNode(obPool);
        }
        return obPool.get(arNodeId);
    }

    public static Set<Integer> getAllNodeIds()
    {
        if (obPool == null)
        {
            return new HashSet<Integer>();
        }
        return obPool.keySet();
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
    public void setPath(String path)
    {
        obPath = path.replaceAll("[ \\(\\)]", "");
        obPath = obPath + "," + obId;                           // add destination
        //System.out.println(obPath);
    }
}
