package Application;

import Tool.Parser;

import java.util.*;

/**
 * Created by xiezebin on 11/12/16.
 */
public class Node {
    public static Parser ps;

    public int id;
    public String hostname;
    public int port;

    public List<Integer> cohort;
    public int[] clock;
    public int[] FLS;
    public int[] LLR;
    public int[] LLS;
    public List<Integer> checkpoints;

    private static Map<Integer, Node> nodePool;

    private Node(int nid){
        if (ps == null)             // only one call is needed
        {
            ps = new Parser();
        }

        // initial node fields
        this.id = nid;
        this.hostname = ps.hostnames[nid];
        this.port = ps.ports[nid];
        this.cohort = ps.cohorts.get(nid);

        clock = new int[Parser.numNodes];
        //int numNeighbor = ps.cohorts.get(nid).size();     // FLS, LLR, LLS entry num = numNodes
        FLS = new int[Parser.numNodes];
        LLR = new int[Parser.numNodes];
        LLS = new int[Parser.numNodes];
        checkpoints = new ArrayList<Integer>();

    }

    public static Node getNode(int nid){
        if (nodePool == null)
        {
            nodePool = new HashMap<Integer, Node>();
        }
        if (!nodePool.containsKey(nid))
        {
            Node node = new Node(nid);
            nodePool.put(nid, node);
        }
        return nodePool.get(nid);
    }

    public String clockstr()
    {
        String str = "";
        for(int c : clock)
        {
            str = str + c + ',';
        }
        str = str.substring(0, str.length() - 1);
        return str;
    }
}
