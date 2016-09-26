import java.io.*;
import java.util.Map;

/**
 * Created by xiezebin on 9/25/16.
 */
public class FileIO {

    public static String CONFIG_FILE = "config.txt";

    public static void readNode(Map<Integer, Node> storage)
    {
        String line = "";
        int section = 0;

        try
        {
            FileReader fr = new FileReader(CONFIG_FILE);
            BufferedReader bfr= new BufferedReader(fr);

            while((line = bfr.readLine()) != null)
            {
                // retrieve node line
                //System.out.println(line);
                if (line.indexOf("Identifier") > 0)
                {
                    section++;
                }
                else if (section == 1 && line.matches("\\t*[0-9]+.*"))      // begin with number
                {
                    String[] parts = line.split("\\t+");
                    int i = parts[0].length() == 0 ? 1 : 0;

                    int loNodeId = Integer.valueOf(parts[i]);
                    String loHostname = parts[i + 1];
                    int loPort = Integer.valueOf(parts[i + 2]);

                    Node loNode = new Node(loNodeId, loHostname, loPort, "");
                    storage.put(loNodeId, loNode);
                }
                else if (section == 2 && line.matches("\\t*[0-9]+.*"))
                {
                    String[] parts = line.split("\\t+");
                    int i = parts[0].length() == 0 ? 1 : 0;

                    int loNodeId = Integer.valueOf(parts[i]);
                    String loPath = parts[i + 1];

                    storage.get(loNodeId).setPath(loPath);
                }

                //char digit = line.charAt(0);
                /*
                if (digit <= '9' && digit >= '0')
                {
                    String[] parts = line.split(" ");
                    Node loNode = new Node(Integer.valueOf(parts[0]), parts[1], Integer.valueOf(parts[2]), parts[3]);
                    storage.put(Integer.valueOf(parts[0]), loNode);
                }
                */
            }
            bfr.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    // this function does not work, hasn't debug
    public static void writeResultToFile(String arFile, String arContent)
    {
        try {
            int last = CONFIG_FILE.lastIndexOf("\\");
            String dir = CONFIG_FILE.substring(0, last + 1);

            FileWriter fw = new FileWriter(dir + arFile, false);
            BufferedWriter bfw = new BufferedWriter(fw);
            bfw.write(arContent + "\n");
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
