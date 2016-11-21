package Application;

/**
 * Created on 11/20/16.
 */
public class Recovery {


    private void initiateRecovery()
    {

    }
    public void receiveRecovery(int fromNodeId, int lls)
    {

    }

    /**
     * Reply message to nodes who send the recovery message,
     * If received replies from all cohorts, start unFreeze()
     */
    public void receiveFreezeReply(int fromNodeId)
    {

    }

    /**
     * Second phase
     * confirm checkpoint or recovery
     */
    private void unFreeze()
    {

    }

    public void receiveUnfreeze(int fromNodeId)
    {

    }

    /**
     * If received replies from all cohorts, send operationComplete to notify next initiator
     */
    public void receiveUnfreezeReply(int fromNodeId)
    {

    }
}
