package Application;

import Tool.SocketManager;

import java.util.*;

/**
 * Created by xiezebin on 11/20/16.
 */
public class Recovery {

    private Node obNode;

    private int sequenceNum;
    private boolean isFreezeComplete;       // todo use static lock

    private boolean willingToCheckpoint;
    private Set<Integer> currentCohort;
    private Set<Integer> replyFromCohort;
    private boolean initiatorFlag;
    private Set<Integer> requestSet;

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
        sequenceNum = 0;
        willingToCheckpoint = true;
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
        takeTentativeCheckpointAndRequestCohorts(obNode.id);

        // base case
        System.out.println("initiator cohort size: " + currentCohort.size());
        if (currentCohort.size() == 0)
        {
            sendRecoveryConfirm();

            receiveRecoveryConfirmReply(-1);      // since no cohorts, directly proceed as received all replies
        }
    }


    public void receiveRecovery(int fromNodeId, int llr)
    {
        requestSet.add(fromNodeId);
        isFreezeComplete = true;
        int fls = obNode.FLS[fromNodeId];
        if (willingToCheckpoint && fls > 0 && llr >= fls)
        {
            takeTentativeCheckpointAndRequestCohorts(fromNodeId);       // fromNodeId is excluded when propagating
        }


        // todo check checkpoint already taken by sequenceNum
        // if cohorts is empty, or need not take checkpoint, directly REPLY
        if (currentCohort.size() == 0 || (willingToCheckpoint && fls == 0))
        {
            Node fromNode = Node.getNode(fromNodeId);
            SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.CHECKPOINT_REPLY.getT());
            requestSet.remove(fromNodeId);      // prevent duplicate reply
        }

    }

    private void takeTentativeCheckpointAndRequestCohorts(int excludeNodeId)
    {
        // mark TENTATIVE checkpoint
        // save LLS, sequenceNum as info
        sequenceNum++;
        CheckpointInfo cpInfo = new CheckpointInfo(sequenceNum, obNode.clock, obNode.LLS);
        obNode.checkpoints.add(cpInfo);


        // calculate current cohort
        List<Integer> cohort = obNode.cohort;
        for (int neiId : cohort)
        {
            int llr = obNode.LLR[neiId];
            if (llr > 0 && neiId != excludeNodeId)  // can exclude fromNodeId
            {
                currentCohort.add(neiId);
                Node neiNode = Node.getNode(neiId);
                SocketManager.send(neiNode.hostname, neiNode.port, obNode.id, llr, Server.MESSAGE.CHECKPOINT.getT());
            }

            obNode.LLR[neiId] = 0;              // reset LLR
            obNode.FLS[neiId] = 0;              // reset FLS, will not take another tentative checkpoint
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
                    SocketManager.send(reqNode.hostname, reqNode.port, obNode.id, 0, Server.MESSAGE.CHECKPOINT_REPLY.getT());
                }
                requestSet.clear();
            }

            replyFromCohort.clear();
        }
    }

    /**
     * Second phase
     * confirm checkpoint or recovery
     */
    private void sendRecoveryConfirm()
    {
        if (!isFreezeComplete)          // prevent duplicate unfreeze
        {
            return;
        }

        isFreezeComplete = false;
        if (RandomMessage.ins(obNode.id).isStop())            // restart random message
        {
            RandomMessage.ins(obNode.id).nextMessage();
        }

        // send unfreeze to cohort
        for (int cohortId : currentCohort)
        {
            Node cohNode = Node.getNode(cohortId);
            SocketManager.send(cohNode.hostname, cohNode.port, obNode.id, 0, Server.MESSAGE.CHECKPOINT_CONFIRM.getT());
        }
    }

    public void receiveRecoveryConfirm(int fromNodeId)
    {
        // propagate unfreeze message
        sendRecoveryConfirm();

        // send REPLY to fromNodeId, todo is it better to reply until receive all replies from cohorts
        Node fromNode = Node.getNode(fromNodeId);
        SocketManager.send(fromNode.hostname, fromNode.port, obNode.id, 0, Server.MESSAGE.CHECKPOINT_CONFIRM_REPLY.getT());
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
            }
        }
    }


}
