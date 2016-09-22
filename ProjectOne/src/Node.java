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
    /*
    public Node(int arId)
    {
        obId = arId;
        obLabel = new Random().nextInt(10) + 1;        // select a label in [1,10] when initializing

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
    */
    private Node(int arNodeId, String arHostname, int arPort, String arPath)
    {
        obId = arNodeId;
        obHostname = arHostname;
        obPort = arPort;
        obPath = obId + "," + arPath + "," + obId;          // add head and tail
        obLabel = new Random().nextInt(10) + 1;        // select a label in [1,10] when initializing
    }

    public static Node getNode(int arNodeId)
    {
        if (obPool == null)
        {
            obPool = new HashMap<>();
        }
        if (!obPool.containsKey(arNodeId))
        {
            /*
            *   Read whole config.txt file and save all nodes info
            */
            String line = "";
            try
            {
                FileReader fr = new FileReader("config.txt");
                BufferedReader bfr= new BufferedReader(fr);

                while((line = bfr.readLine()) != null){
                    char digit = line.charAt(0);
                    if (digit <= '9' && digit >= '0')
                    {
                        String[] parts = line.split(" ");
                        Node loNode = new Node(Integer.valueOf(parts[0]), parts[1], Integer.valueOf(parts[2]), parts[3]);
                        obPool.put(Integer.valueOf(parts[0]), loNode);
                    }
                }
                bfr.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            /*while (line = FileIO.read("config.txt") != null)
            {
                String[] parts = line.split(" ");
                Node loNode = new Node(Integer.valueOf(parts[0]), parts[1], Integer.valueOf(parts[2]), parts[3]);
                obPool.put(Integer.valueOf(parts[0]), loNode);
            }*/
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
}
