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
    private Parser ps;
    private Node node;
    private RandomMessage randomMessage;
    private TwoPhaseSnapshot twoPhaseSnapshot;
    private Checkpointing checkpointing;
    private Recovery recovery;


    @Before
    public void setup() {
        ps = new Parser();
        node = new Node(0);
        randomMessage = new RandomMessage();
        twoPhaseSnapshot = new TwoPhaseSnapshot();
        checkpointing = new Checkpointing();
        recovery = new Recovery();
    }


    @Test
    public void testParser() {
        assertEquals("cohortMap size:", ps.numNodes, ps.cohorts.size())
        assertEquals("operationList size:", ps.numOperations, ps.operationList.size())
    }
    @Test
    public void testNode() {
        //assertEquals("", 0, node.);
        assertEquals("first test", 0, 0);
    }
}
