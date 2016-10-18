import java.util.Set;

/**
 * Created by xiezebin on 10/18/16.
 */
public class Server {

    int Nodeid;
    boolean locked_boolean;
    int local_time;  	// self increment to enterCS time, or receive message
    Set<Integer> permission_received_from_quorum;
    Set<Integer> priority_queue_of_request;

    public Server(int nodeId)
    {

    }
    public void launch()	// receive all message, update time when necessary, call Client.send()
                            // check message, call Grant(), Failed(), Inquire(), Yield()
    {

    }

    public void enterCS(){}	// add to queue, Request logic
    public void leaveCS(){}	// remove queue, Release logic

    public void Request(){}	// send, add to log.txt
    public void Grant(){}
    public void Release(){}	// send, add to log.txt
    public void Failed(){}
    public void Inquire(){}
    public void Yield(){}

}
