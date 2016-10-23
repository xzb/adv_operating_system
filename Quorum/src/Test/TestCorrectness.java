package Test;

import Tool.FileIO;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 10/18/16.
 */
public class TestCorrectness {

    private static String currentProcessInCS;
    private static Map<String, Integer> numsEnterCS;

    public static boolean checkLog()
    {
        currentProcessInCS = null;
        numsEnterCS = new HashMap<>();

        FileIO.readLineCallback callback = line -> processLineOfLog(line);
        FileIO.readFile(callback);

        for (Map.Entry<String, Integer> entry : numsEnterCS.entrySet())
        {
            System.out.println("Process " + entry.getKey() + " enters C.S. " + entry.getValue() + " times.");
        }
        System.out.println("Processes enter C.S. sequentially.");

        return true;
    }
    private static void processLineOfLog (String line)
    {
        String[] parts = line.split(" ");
        String processId = parts[0];
        String eventType = parts[1];

        if ("enter".equals(eventType))
        {
            if (currentProcessInCS != null)
            {
                System.out.println("Warings: Multiple processes enter critical section! " +
                        "Process id: " + currentProcessInCS + ", " + processId);
                System.exit(1);
            }
            else
            {
                currentProcessInCS = processId;
            }
        }
        else if ("leave".equals(eventType) && currentProcessInCS.equals(processId))
        {
            if (!numsEnterCS.containsKey(currentProcessInCS))
            {
                numsEnterCS.put(currentProcessInCS, 1);
            }
            else
            {
                numsEnterCS.put(currentProcessInCS, numsEnterCS.get(currentProcessInCS) + 1);
            }
            currentProcessInCS = null;
        }
    }

}
