package Tool;
/**
 * Created by yxl154630 on 10/18/16.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.sun.nio.sctp.*;
public class Parser {

    public HashMap<Integer,HashSet<Integer>> ms;
    public HashMap<Integer,HashSet<Integer>> qs;
    // int minPerActive;
    // int maxPerActive;
    // int minSendDelay;
    // int snapshotDelay;
    // int maxNumber;
    String config="config.txt";

    public String[] portPath;
    public String[] hostName;
    public int[] numPort;
    public int allNodes;
    public int REQUEST_DELAY;
    public int CS_EXE_TIME;
    public int Total_Request;


    public Parser()
    {
        parse();
    }
    void parse() {
        try
         {
            FileReader fr = new FileReader(config);
            BufferedReader in = new BufferedReader(fr);

            //System.out.println(in);
            String[] tmp = skipLines(in).split(" ");
            this.allNodes=Integer.parseInt(tmp[0]);
            this.REQUEST_DELAY=Integer.parseInt(tmp[1]);
            this.CS_EXE_TIME=Integer.parseInt(tmp[2]);
            this.Total_Request=Integer.parseInt(tmp[3]);
            this.portPath=new String[allNodes];
            this.hostName=new String[allNodes];
            this.numPort=new int[allNodes];
            //1016 addition
            this.ms=new HashMap<Integer,HashSet<Integer>>();
            this.qs=new HashMap<Integer,HashSet<Integer>>();



            for (int i = 0; i < allNodes; i++) {
                // System.out.println(in);
                String[] tmp2 = skipLines(in).split(" ");
                hostName[i] = tmp2[1];
                numPort[i] = Integer.parseInt(tmp2[2].replaceAll(" ",""));
                HashSet<Integer> sett1=new HashSet<Integer>();
                HashSet<Integer> sett2=new HashSet<Integer>();
                qs.put(i,sett1);
                ms.put(i,sett2);

            }

            for (int i = 0; i < allNodes; i++) {

                String tmp3 = skipLines(in);
                //System.out.println(tmp3.length());
                for(int j=1;j<tmp3.length();j++){

                    int cha=tmp3.charAt(j)-'0';
                    qs.get(i).add(cha);
                    ms.get(cha).add(i);
                    // System.out.println("这是第i="+i+"j="+j);
                }
                //      portPath[i]=ss.replace("(","");
                //      portPath[i]=portPath[i].replace(")","")+i;
                //      portPath[i]=portPath[i].replaceAll(" ","");
                // System.out.println(portPath[i]);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

/***
    public static void main(String[] args) {




        String config = "/Users/yql/Desktop/config.txt";
        Parser se=new Parser();
        se.parse();
        // System.out.println(se.allNodes);
        //System.out.println(se.portPath[3]);
        //   for(Object key: se.qs){
        //     for(Object key2:se.qs.get(key)){
        System.out.println("se.REQUEST_DELAY:"+se.hostName[3]);
        System.out.println("se.REQUEST_DELAY:"+se.REQUEST_DELAY);
        System.out.println("se.CS_EXE_TIME:"+se.CS_EXE_TIME);
        System.out.println("se.Total_Request:"+se.Total_Request);
          for(int i=0;i<5;i++) {
         System.out.println("qs of node"+i+":");
         for (Object key : se.qs.get(i)) {
         System.out.print(key);
         }
         System.out.println("");
         System.out.println("ms of node"+i+":");
         for (Object key : se.ms.get(i)) {
         System.out.print(key);
         }
         System.out.println("");
         }
         ****/
   // }



    private static String skipLines(BufferedReader in) throws IOException {
        String res = "";
        String tmp = "";

        while ((tmp=in.readLine())!=null) {

            if (tmp.length() == 0) continue;
            else if (tmp.charAt(0) >= '0' && tmp.charAt(0) <= '9') {
                res = tmp;
                break;
            }
        }
        //   System.out.println(res);
        return res;
    }


}
