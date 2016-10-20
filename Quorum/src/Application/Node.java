package Application;

import Tool.Parser;
import java.util.Set;

/**
 * Created by yxl154630 on 10/18/16.
 */
public class Node {
    public int id;
    public int port;
    public String hostname;
    private static Parser ps;
    public Set<Integer> qset;              // should include self
    public Set<Integer> mset;

    public int REQUEST_DELAY;              // global parameter
    public int CS_EXE_TIME;
    public int Total_Request;

    public Node(int ID){
        this.id=ID;
        if (ps == null)             // only one call is needed
        {
            ps = new Parser();
        }
        this.hostname=ps.hostName[ID];
        this.port=ps.numPort[ID];
        this.qset=ps.qs.get(ID);
        this.mset=ps.ms.get(ID);

        REQUEST_DELAY = ps.REQUEST_DELAY;
        CS_EXE_TIME = ps.CS_EXE_TIME;
        Total_Request = ps.Total_Request;
    }
    public static Node getNode(int ID){
        Node node=new Node(ID);
        return node;

    }
    public static void main(String[] args){
        Node node=new Node(1);
        System.out.print("port:"+node.port);
    }


}
