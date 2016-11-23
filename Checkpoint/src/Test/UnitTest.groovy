package Test

import Application.*
import Tool.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.assertEquals

/**
 * Created by xiezebin on 11/12/16.
 */
class UnitTest {

    Application.Node node = Application.Node.getNode(0);
    RandomMessage randomMessage0 = RandomMessage.ins(0);
    RandomMessage randomMessage1 = RandomMessage.ins(1);
    RandomMessage randomMessage2 = RandomMessage.ins(2);

    @BeforeClass
    public static void setup() {
        Server server0 = new Server(0);
        Server server1 = new Server(1);
        Server server2 = new Server(2);
    }


    @Test
    public void testParser() {
        assertEquals("cohortMap size:", Parser.numNodes, Parser.cohorts.size())
        assertEquals("operationList size:", Parser.numOperations, Parser.operationList.size())
    }
    //@Test
    public void testNode() {
        assertEquals("cohort size of node 0:", 1, node.cohort.size())
    }
    //@Test todo remove magic number
    public void testRandomMessage() {

        randomMessage0.nextMessage();
        randomMessage1.nextMessage();

        assertEquals("send clock of node 0:", Parser.numRandomMessages, node.clock[0])
        assertEquals("receive clock of node 0:", Parser.numRandomMessages, node.clock[1])
        assertEquals("FLS of node 0:", 1, node.FLS[1])
        assertEquals("LLR of node 0:", Parser.numRandomMessages, node.LLR[1])
        assertEquals("LLS of node 0:", 0, node.LLS[1])
    }


    @Test
    public void testRandomMsgWithOperation() {
        randomMessage0.nextMessage();   // each next function is run in a single thread
        randomMessage1.nextMessage();
        randomMessage2.nextMessage();

        // test daemon
        Daemon daemon = Daemon.ins(0);
        daemon.nextOperation();

        while (1) ;
    }

    //@Test
    public void testDirectMsgWithOperation()
    {
        randomMessage0.directMessage(1);
        randomMessage1.directMessage(2);
        randomMessage2.directMessage(0);

        // two 'c' test pass
        // two 'r' test pass
        Daemon daemon = Daemon.ins(0);
        daemon.nextOperation();

        while (1) ;
    }


}
