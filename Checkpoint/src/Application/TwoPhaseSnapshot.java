package Application;

/**
 * Created by xiezebin on 11/12/16.
 */
public class TwoPhaseSnapshot {

    private static boolean freezeSendFlag;
    private static boolean freezeCompleteFlag;

    public static boolean isFreeze()
    {
        return freezeSendFlag || freezeCompleteFlag;
    }

    public static void unFreeze()
    {
        freezeSendFlag = false;
        freezeCompleteFlag = false;

        // todo move to Driver
        if (Driver.randomMessage.isStop)
        {
            Driver.randomMessage.isStop = false;
            Driver.randomMessage.nextMessage();
        }
    }
}
