package xusheng.word2vec;

import fig.basic.LogInfo;
import xusheng.misc.StopWordLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class WordEmbedder {

    public static String path = "/home/xusheng/word2vec";
    public static String predpPath = "/home/xusheng/data_0911/pred_index.aaai";
    public static String randPath = "/home/xusheng/word2vec/Random_Patty_120.tsv";
    public static String pattyPath = "/home/xusheng/word2vec/wikipedia-patterns.txt";
    public static String stopPath = "/home/xusheng/word2vec/stop.simple";

    public static void work(String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(randPath));
        String line = ""; int cnt = 0;
        HashSet<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) set.add(line.split("_")[0]);
        br.close();
        br = new BufferedReader(new FileReader(pattyPath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        while ((line = br.readLine()) != null) {
            String idx = line.split("\t")[0];
            if (set.contains(idx)) {
                bw.write(idx);
                String[] content = line.split("\t")[1].split(" ");
                HashSet<String> tmpSet = new HashSet<>();
                for (String item: content) {
                    boolean flag = true;
                    for (int i=0; i<item.length(); i++)
                        if (item.charAt(i)<'a' || item.charAt(i)>'z') {
                            flag = false;
                            break;
                        }
                    if (flag && !stopSet.contains(item) && !tmpSet.contains(item)) {
                        bw.write("\t" + item);
                        tmpSet.add(item);
                    }
                }
                bw.write("\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static HashSet<String> stopSet = null;
    public static void main(String[] args) throws Exception {
        stopSet = StopWordLoader.getStopSet(stopPath);
        work(path + "/random_patty_keywords.txt");
    }
}
