import java.util.Objects;
import java.util.Set;

/**
 * Created by yxl154630 on 10/18/16.
 */
public class Node {
    int id;
    int port;
    String hostname;
    private static Parser ps;
    Set<Integer> qset;              // should include self
    Set<Integer> mset;

    int REQUEST_DELAY;              // global parameter
    int CS_EXE_TIME;
    int Total_Request;

    Node(int ID){
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
