package Tool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by xiezebin on 10/19/16.
 */
public class FileIO {

    private static String resultFile = "log.txt";

    public static void writeFile(String arContent)
    {
        System.out.println(arContent);

        try {
            //int last = CONFIG_FILE.lastIndexOf("\\");
            //String dir = CONFIG_FILE.substring(0, last + 1);

            FileWriter fw = new FileWriter(resultFile, true);   // append file
            BufferedWriter bfw = new BufferedWriter(fw);
            bfw.write(arContent + "\n");
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
