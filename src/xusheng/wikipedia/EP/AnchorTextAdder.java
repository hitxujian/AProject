package xusheng.wikipedia.EP;

import fig.basic.LogInfo;

import java.io.*;

/**
 * Created by angrymidiao on 8/30/15.
 */
public class AnchorTextAdder {

    public static void work(String inFile, String outFile) throws Exception {
        File file = new File(inFile);
        if (!file.exists()) return;
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        while ((line = br.readLine()) != null) {

        }


    }

    public static void main(String[] args) throws Exception {
        AliasTitle.initialize(args[0]);
        for (char Ch ='A'; Ch<='D'; Ch++) {
            for (char ch = 'A'; ch <= 'Z'; ch++) {
                for (int i = 0; i <= 9; i++) {
                    String inFile = String.format("%s/%s%s/wiki_0%d", args[1], Ch, ch, i);
                    LogInfo.logs(inFile);
                    try {
                        work(inFile, args[2]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                for (int i = 10; i <= 99; i++) {
                    String inFile = String.format("%s/%s%s/wiki_%d", args[1], Ch, ch, i);
                    LogInfo.logs(inFile);
                    try {
                        work(inFile, args[2]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

    }
}
