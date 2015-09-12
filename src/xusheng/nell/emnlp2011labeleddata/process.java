package xusheng.nell.emnlp2011labeleddata;

import fig.basic.LogInfo;

import java.io.*;

/**
 * Created by Administrator on 2015/9/11.
 */
public class process {

    public static BufferedWriter bw;

    public static void extractPos(String rel, String inFile) throws Exception {
        bw.write("###\t" + rel + "\n");
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split(",");
            if (spt.length < 2) continue;
            String[] right = spt[1].split(" ");
            for (String arg2: right) bw.write(spt[0] + "\t" + arg2 + "\n");
        }
        br.close();
    }

    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);
        bw = new BufferedWriter(new FileWriter(args[1]));
        File[] files = f.listFiles();
        for (File file : files) {
            String path = file.getAbsolutePath();
            String rel = file.getName();
            if (! path.startsWith("_")) {
                LogInfo.logs("Process file %s", path);
                extractPos(rel, path);
            }
        }
    }
}
