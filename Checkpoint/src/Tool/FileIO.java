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
            // set file directory for script running.
            int last = Parser.config.lastIndexOf("/");
            String dir = Parser.config.substring(0, last + 1);
            resultFile = dir + "log.txt";
            //System.out.println("==LOG dir: " + dir);

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
