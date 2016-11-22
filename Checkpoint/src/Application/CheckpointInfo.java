package Application;

import Tool.Parser;

/**
 * Created by xiezebin on 11/21/16.
 */
public class CheckpointInfo {
    public int sequenceNum;
    public int[] clock;
    public int[] LLS;

    public CheckpointInfo(int sNum, int[] srcClock, int[] srcLLS)
    {
        sequenceNum = sNum;
        clock = new int[srcClock.length];
        LLS = new int[srcLLS.length];
        System.arraycopy(srcClock, 0, clock, 0, srcClock.length);
        System.arraycopy(srcLLS, 0, LLS, 0, srcLLS.length);
    }
}
