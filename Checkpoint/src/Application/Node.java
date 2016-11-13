package Application;

import Tool.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by xiezebin on 11/12/16.
 */
public class Node {
    private static Parser ps;

    private int id;
    public String hostname;
    public int port;

    private Set<Integer> cohort;
    private int[] clock;
    private int[] FLS;
    private int[] LLR;
    private int[] LLS;
    private List<Integer> checkpoints;

    public Node(int ID){
        if (ps == null)             // only one call is needed
        {
            ps = new Parser();
        }

        // initial node fields
        this.id=ID;
        this.hostname = ps.hostnames[ID];
        this.port = ps.ports[ID];
        this.cohort = ps.cohorts.get(ID);

        clock = new int[ps.numNodes];
        int numNeighbor = ps.cohorts.get(ID).size();
        FLS = new int[numNeighbor];
        LLR = new int[numNeighbor];
        LLS = new int[numNeighbor];
        checkpoints = new ArrayList<Integer>();

    }

    public static Node getNode(int ID){
        Node node=new Node(ID);     //todo singleton
        return node;
    }
}
