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
    private Node node;
    private RandomMessage randomMessage;
    private TwoPhaseSnapshot twoPhaseSnapshot;
    private Checkpoint checkpoint;


    @Before
    public void setup() {
        node = Node.getNode(0);
        randomMessage = new RandomMessage(0);
        twoPhaseSnapshot = new TwoPhaseSnapshot();
        checkpoint = new Checkpoint();
    }


    @Test
    public void testParser() {
        assertEquals("cohortMap size:", Parser.numNodes, Parser.cohorts.size())
        assertEquals("operationList size:", Parser.numOperations, Parser.operationList.size())
    }
    @Test
    public void testNode() {
        assertEquals("cohort size of node 0:", 2, node.cohort.size())
    }
}
