package Application;

import Tool.Parser;

import java.util.*;

/**
 * Created by xiezebin on 11/12/16.
 */
public class Node {
    public int id;
    public String hostname;
    public int port;

    public List<Integer> cohort;
    public int[] clock;
    public int[] FLS;
    public int[] LLR;
    public int[] LLS;
    public List<Integer> checkpoints;

    private static Map<Integer, Node> nodePool;             // singleton

    private Node(int nid){

        // initial node fields
        this.id = nid;
        this.hostname = Parser.hostnames[nid];
        this.port = Parser.ports[nid];
        this.cohort = Parser.cohorts.get(nid);

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

    /*
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
    public void receiveClock(String clockstr, int fromNodeId)
    {
        String[] parts = clockstr.split(",");

    }*/
}
