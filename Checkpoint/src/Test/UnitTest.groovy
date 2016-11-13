package Test

import Application.*
import org.junit.Before
import org.junit.Test

/**
 * Created by xiezebin on 11/12/16.
 */
class UnitTest extends groovy.util.GroovyTestCase {
    private Node node;
    private RandomMessage randomMessage;
    private TwoPhaseSnapshot twoPhaseSnapshot;
    private Checkpointing checkpointing;
    private Recovery recovery;


    @Before
    public void setup() {
        node = new Node();
        randomMessage = new RandomMessage();
        twoPhaseSnapshot = new TwoPhaseSnapshot();
        checkpointing = new Checkpointing();
        recovery = new Recovery();
    }


    @Test
    public void testNode() {
        //assertEquals("", 0, node.);
        assertEquals("first test", 0, 0);
    }
}
