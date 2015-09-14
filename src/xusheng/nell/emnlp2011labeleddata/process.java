package xusheng.nell.emnlp2011labeleddata;

import fig.basic.LogInfo;
import xusheng.nell.EntityIndex;

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

    public static void processFile(String inFile, String outFile) throws Exception {
        File f = new File(inFile);
        bw = new BufferedWriter(new FileWriter(outFile));
        File[] files = f.listFiles();
        for (File file : files) {
            String path = file.getAbsolutePath();
            String rel = file.getName();
            if (rel.startsWith("_")) {
                LogInfo.logs("Process file %s", path);
                extractPos(rel, path);
            }
        }
    }

    public static void transform2idx(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("###")) {
                bw.write(line + "\n");
                continue;
            }
            String[] spt = line.split("\t");
            String idx1 = EntityIndex.getIdx(spt[0]);
            String idx2 = EntityIndex.getIdx(spt[1]);
            if (idx1 != null && idx2 != null) bw.write(idx1 + "\t" + idx2 + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job Done.");
    }

    public static void main(String[] args) throws Exception {
        processFile(args[0], args[1]);
        //EntityIndex.initialize(args[2]);
        //transform2idx(args[0], args[1]);
    }
}
