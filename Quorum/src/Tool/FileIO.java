package Tool;

import java.io.*;

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

    public static void readFile(readLineCallback callback)
    {
        String str="";
        try{
            FileReader fr = new FileReader(resultFile);
            BufferedReader bfr= new BufferedReader(fr);

            while((str = bfr.readLine()) != null)
            {
                if (callback != null)
                {
                    callback.call(str);
                }
                //System.out.println(str);
            }
            bfr.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface readLineCallback {
        void call(String line);
    }
}
