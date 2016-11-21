package Application;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 11/20/16.
 */
public class Recovery {
    private Node obNode;

    private static Map<Integer, Recovery> obInstances;
    public static Recovery ins(int nid)
    {
        if (obInstances == null)
        {
            obInstances = new HashMap<Integer, Recovery>();
        }
        if (!obInstances.containsKey(nid))
        {
            Recovery rv = new Recovery(nid);
            obInstances.put(nid, rv);
        }
        return obInstances.get(nid);
    }
    private Recovery(int nid)
    {
        obNode = Node.getNode(nid);
    }

    public void initiateRecovery()
    {

    }
    public void receiveRecovery(int fromNodeId, int lls)
    {

    }

    /**
     * Reply message to nodes who send the recovery message,
     * If received replies from all cohorts, start unFreeze()
     */
    public void receiveRecoveryReply(int fromNodeId)
    {

    }

    /**
     * Second phase
     * confirm checkpoint or recovery
     */
    private void sendRecoveryConfirm()
    {

    }

    public void receiveRecoveryConfirm(int fromNodeId)
    {

    }

    /**
     * If received replies from all cohorts, send operationComplete to notify next initiator
     */
    public void receiveRecoveryConfirmReply(int fromNodeId)
    {

    }
}
