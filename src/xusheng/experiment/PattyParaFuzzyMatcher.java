package xusheng.experiment;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashSet;

/**
 * Created by Xusheng on 1/17/2016.
 */
public class PattyParaFuzzyMatcher {

    public static String dir = "/home/kangqi/workspace/UniformProject/resources/paraphrase/emnlp2015/" +
            "PATTY120_Matt-Fb2m_med_gGD_s20_len3_fb1_sh0_aT0_c150_c21.2_aD1_SF1_SL1_cov0.10_pH10_dt1.0_sz30000_aI1";
    public static String pattyFile = "/home/xusheng/wikipedia-patterns.txt";
    public static String ppdbFile = "/home/xusheng/ppdb-1.0-s-phrasal";
    public static String paralexFile = "/home/xusheng/";

    public static boolean verbose = true;

    public static HashSet<String> patty120idx, patty120 = new HashSet<>();


    public static void extract120() throws IOException{
        try {
            File root = new File(dir);
            File[] files = root.listFiles();
            for (int i=0; i<files.length; i++) {
                if (files[i].isDirectory()) {
                    File file = new File(files[i].getAbsolutePath() + "/schema");
                    if (!file.exists()) continue;
                    String name = files[i].getName();
                    if (verbose) LogInfo.logs(name);
                    String idx = name.split("_")[0];
                    patty120idx.add(idx);
                }
            }
            if (verbose) LogInfo.logs(patty120idx.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new FileReader(pattyFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/patty120.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            String idx = line.split("\t")[0];
            if (patty120idx.contains(idx))
                patty120.add(line);
            bw.write(line + "\n");
        }
        br.close();
        bw.close();
        pattyFile = "/home/xusheng/patty120.txt";
    }

    public static void work() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(pattyFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String idx = spt[0];
            String pattern = spt[1];

        }
        br = new BufferedReader(new FileReader(ppdbFile));

    }

    public static void main(String[] args) throws IOException {
        extract120();
        //work();
    }

}
