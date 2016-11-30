package Application;

import Tool.FileIO;
import Tool.Parser;
import Tool.SocketManager;

import java.util.*;

/**
 * Created by xiezebin on 11/20/16.
 */
public class Recovery {

    private Node obNode;

    private boolean isFreezeComplete;       // todo use static lock

    private boolean willingToRoll;
    private boolean readyToRoll;            // prevent forever loop; if false, roll once received confirm message
    private Set<Integer> currentCohort;
    private Set<Integer> replyFromCohort;
    private boolean initiatorFlag;
    private Set<Integer> requestSet;
    private CheckpointInfo cpInfo;

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
        willingToRoll = true;
        readyToRoll = true;
        currentCohort = new HashSet<Integer>();
        replyFromCohort = new HashSet<Integer>();
        requestSet = new HashSet<Integer>();
    }
    public boolean isFreeze()
    {
        return isFreezeComplete;
    }



    public void initiateRecovery()
    {

        initiatorFlag = true;
        isFreezeComplete = true;
        prepareToRollAndRequestCohorts(obNode.id);

        // base case
        System.out.println("initiator cohort size: " + currentCohort.size());
        if (currentCohort.size() == 0)
        {
            sendRecoveryConfirm();

            receiveRecoveryConfirmReply(-1);      // since no cohorts, directly proceed as received all replies
        }
    }

    /**
     * main different with Checkpoint
     */
    public void receiveRecovery(int fromNodeId, int lls)
    {
        requestSet.add(fromNodeId);
        isFreezeComplete = true;
        int llr = obNode.LLR[fromNodeId];


        // if need not recovery, or already set recovery, directly REPLY
        if (llr <= lls || !readyToRoll)
        {
            directlyReply(fromNodeId);
            FileIO.writeFile("Current time: Node " + obNode.id + " clock " + Arrays.toString(obNode.clock));
        }

        else if (willingToRoll && llr > lls && readyToRoll)          // prevent forever loop
        {
            prepareToRollAndRequestCohorts(fromNodeId);         // fromNodeId is excluded when propagating

            // if cohorts is empty, directly REPLY
            if (currentCohort.size() == 0)
            {
                directlyReply(fromNodeId);
            }
        }

    }
    private void directlyReply(int fromNodeId)
    {
        Node fromNode = Node.getNode(fromNodeId);
        SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.RECOVERY_REPLY.getT());
        requestSet.remove(fromNodeId);      // prevent duplicate reply
    }

    private void prepareToRollAndRequestCohorts(int excludeNodeId)
    {
        // retrieve TENTATIVE checkpoint
        readyToRoll = false;
        if (obNode.checkpoints.isEmpty())
        {
            int[] initClock = new int[Parser.numNodes];
            int[] initLLS = new int[Parser.numNodes];
            cpInfo = new CheckpointInfo(0, initClock, initLLS);
        }
        else
        {
            cpInfo = obNode.checkpoints.get(obNode.checkpoints.size() - 1);     // need not remove
        }
        // for Testing, validation
        FileIO.writeFile("Recovery: Node " + obNode.id + " clock " + Arrays.toString(cpInfo.clock));


        // calculate current cohort
        List<Integer> cohort = obNode.cohort;
        for (int neiId : cohort)
        {
            int lls = cpInfo.LLS[neiId];
            if (neiId != excludeNodeId)         // send to all neighbor no matter lls; can exclude fromNodeId
            {
                currentCohort.add(neiId);
                Node neiNode = Node.getNode(neiId);
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, lls, Server.MESSAGE.RECOVERY.getT());
            }

        }
    }


    public void receiveRecoveryReply(int fromNodeId)
    {
        replyFromCohort.add(fromNodeId);
        if (replyFromCohort.equals(currentCohort))  // wait for all replies from all current cohorts
        {
            if (initiatorFlag)      // initiator should send unfreeze after receive reply from all
            {
                sendRecoveryConfirm();
            }
            else                    // reply to request node
            {
                for (int requestId : requestSet)
                {
                    Node reqNode = Node.getNode(requestId);
                    SocketManager.send(reqNode.hostname, reqNode.port, obNode.id, 0, Server.MESSAGE.RECOVERY_REPLY.getT());
                }
                requestSet.clear();
            }

            replyFromCohort.clear();
        }
    }

    /**
     * Second phase
     * confirm checkpoint or recovery
     *
     * todo if abort
     */
    private void sendRecoveryConfirm()
    {
        if (!isFreezeComplete)          // prevent duplicate unfreeze
        {
            return;
        }

        // if readyToRestore, restore clock, reset FLS,LLR
        if (!readyToRoll)
        {
            System.out.println("Node " + obNode.id + " clock recovery from " +
                            Arrays.toString(obNode.clock) +
                            " to " +
                            Arrays.toString(cpInfo.clock)
            );

            System.arraycopy(cpInfo.clock, 0, obNode.clock, 0, cpInfo.clock.length);    // cpInfo.clock -> obNode.clock
            for (int neiId : obNode.cohort)
            {
                obNode.FLS[neiId] = 0;
                obNode.LLR[neiId] = 0;
            }
            readyToRoll = true;
        }

        isFreezeComplete = false;
        if (RandomMessage.ins(obNode.id).isStop())            // restart random message
        {
            System.out.println("Node " + obNode.id + " restart random message.");
            RandomMessage.ins(obNode.id).nextMessage();
        }

        // send unfreeze to cohort
        for (int cohortId : currentCohort)
        {
            Node cohNode = Node.getNode(cohortId);
            SocketManager.send(cohNode.hostname, cohNode.port, obNode.id, 0, Server.MESSAGE.RECOVERY_CONFIRM.getT());
        }
    }

    public void receiveRecoveryConfirm(int fromNodeId)
    {
        // propagate unfreeze message
        sendRecoveryConfirm();

        // send REPLY to fromNodeId
        Node fromNode = Node.getNode(fromNodeId);
        SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.RECOVERY_CONFIRM_REPLY.getT());
    }


    public void receiveRecoveryConfirmReply(int fromNodeId)
    {
        if (fromNodeId >= 0)
        {
            replyFromCohort.add(fromNodeId);
        }
        if (replyFromCohort.equals(currentCohort))
        {
            replyFromCohort.clear();
            currentCohort.clear();          // clear here

            if (initiatorFlag)
            {
                // send operationComplete to all nodes, in order to notify next initiator
                Daemon.ins(obNode.id).broadcastOperationComplete();
                initiatorFlag = false;      // reset
                FileIO.writeFile("Recovery at Node " + obNode.id + " finish.");
            }
        }
    }


}
