package Test

import Application.*
import Tool.*
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertEquals

/**
 * Created by xiezebin on 11/12/16.
 */
class UnitTest {

    Application.Node node = Node.getNode(0);

    @Before
    public void setup() {
    }


    @Test
    public void testParser() {
        assertEquals("cohortMap size:", Parser.numNodes, Parser.cohorts.size())
        assertEquals("operationList size:", Parser.numOperations, Parser.operationList.size())
    }
    @Test
    public void testNode() {
        assertEquals("cohort size of node 0:", 1, node.cohort.size())
    }
    @Test
    public void testRandomMessage() {
        Parser.numRandomMessages = 3;
        Server server0 = new Server(0);
        Server server1 = new Server(1);

        RandomMessage randomMessage = new RandomMessage(0);
        RandomMessage randomMessage1 = new RandomMessage(1);
        randomMessage.nextMessage();
        randomMessage1.nextMessage();

        assertEquals("send clock of node 0:", Parser.numRandomMessages, node.clock[0])
        assertEquals("receive clock of node 0:", Parser.numRandomMessages, node.clock[1])
        assertEquals("FLS of node 0:", 1, node.FLS[1])
        assertEquals("LLR of node 0:", Parser.numRandomMessages, node.LLR[1])
        assertEquals("LLS of node 0:", 0, node.LLS[1])
    }

    @Test
    public void testTwoPhaseSnapshot() {
    }

    @Test
    public void testCheckpoint() {

    }
}
