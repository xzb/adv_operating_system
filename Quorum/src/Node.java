import java.util.Objects;
import java.util.Set;

/**
 * Created by yxl154630 on 10/18/16.
 */
public class Node {
    int id;
    int port;
    String hostname;
    Parser ps;
    Set<Integer> qset;
    Set<Integer> mset;
    Node(int ID){
        this.id=ID;
        ps=new Parser();
        ps.parse();
        this.hostname=ps.hostName[ID];
        this.port=ps.numPort[ID];
        this.qset=ps.qs.get(ID);
        this.mset=ps.ms.get(ID);
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
