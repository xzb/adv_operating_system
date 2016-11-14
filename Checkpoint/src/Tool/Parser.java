package Tool;
/**
 * Created by on 10/18/16.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {

    public static String config = "config.txt";

    private static Parser ps = new Parser();             // initial everything

    public static int numNodes;
    public static int numOperations;
    public static int minInstanceDelay;
    public static int minSendDelay;
    public static int numRandomMessages;

    public static String[] hostnames;
    public static int[] ports;
    public static HashMap<Integer,ArrayList<Integer>> cohorts;
    public static List<String> operationList;

    public Parser()
    {
        parse();
    }
    private void parse() {
        try
        {
            FileReader fr = new FileReader(config);
            BufferedReader in = new BufferedReader(fr);

            // scan first line
            String[] tmp = skipLines(in).split(" ");
            numNodes = Integer.parseInt(tmp[0]);
            numOperations = Integer.parseInt(tmp[1]);
            minInstanceDelay = Integer.parseInt(tmp[2]);
            minSendDelay = Integer.parseInt(tmp[3]);
            numRandomMessages = Integer.parseInt(tmp[4]);
            hostnames = new String[numNodes];
            ports = new int[numNodes];
            cohorts = new HashMap<Integer,ArrayList<Integer>>();
            operationList = new ArrayList<String>();

            // scan hostnames and ports
            for (int i = 0; i < numNodes; i++) {
                // System.out.println(in);
                String[] tmp2 = skipLines(in).split(" ");
                int nodeId = Integer.valueOf(tmp2[0]);
                hostnames[nodeId] = tmp2[1];
                ports[nodeId] = Integer.parseInt(tmp2[2].replaceAll(" ",""));
                cohorts.put(nodeId, new ArrayList<Integer>());

            }

            // scan cohorts
            for (int i = 0; i < numNodes; i++) {

                String[] parts = skipLines(in).split(" ");
                int nodeId = Integer.valueOf(parts[0]);
                for (int j = 1; j < parts.length; j++)
                {
                    int cha=Integer.valueOf(parts[j]);
                    cohorts.get(nodeId).add(cha);
                }

            }

            // scan operations
            for (int i = 0; i < numOperations; i++)
            {
                String line = skipLines(in);
                operationList.add(line);
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static String skipLines(BufferedReader in) throws IOException {
        String res = "";
        String tmp = "";

        while ((tmp=in.readLine())!=null) {

            if (tmp.length() == 0) continue;
            else if (tmp.charAt(0) >= '0' && tmp.charAt(0) <= '9' || tmp.charAt(0) == '(') {
                res = tmp;
                break;
            }
        }
        //   System.out.println(res);
        return res;
    }


}
