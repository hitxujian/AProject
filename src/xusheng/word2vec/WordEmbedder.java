package xusheng.word2vec;

import fig.basic.LogInfo;
import xusheng.misc.StopWordLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WordEmbedder {

    public static String path = "/home/xusheng/word2vec";
    public static String predpPath = "/home/xusheng/data_0911/pred_index.aaai";
    public static String randPath = "/home/xusheng/word2vec/Random_Patty_120.tsv";
    public static String pattyPath = "/home/xusheng/word2vec/wikipedia-patterns.txt";
    public static String stopPath = "/home/xusheng/word2vec/stop.simple";

    public static void workForKeywordExtraction(String outFile) throws Exception {
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

    public static void workForVecCreation(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(predpPath));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t")[1].split("\\.");
            for (String item : spt) {
                String[] spt2 = item.split("_");
                for (String word: spt2) predWordSet.add(word);
            }
        }
        br.close();
        br = new BufferedReader(new FileReader(path + "/random_patty_keywords.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/pred-rel-word.similarity"));
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            for (String word: spt) relWordSet.add(word);
        }

        for (String predWord: predWordSet) {
            for (String relWord: relWordSet) {
                if (VecLoader.vectors.containsKey(predWord) && VecLoader.vectors.containsKey(relWord))
                    bw.write(predWord + "\t" + relWord + "\t" +
                            multi(VecLoader.vectors.get(predWord), VecLoader.vectors.get(relWord)) + "\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static String multi(ArrayList<Double> arrA, ArrayList<Double> arrB) {
        double sum = 0;
        for (int i=0; i<arrA.size(); i++) {
            for (int j=0; j<arrB.size(); j++) sum += arrA.get(i) * arrB.get(j);
        }
        return String.valueOf(sum);
    }

    public static HashSet<String> stopSet = null, predWordSet = new HashSet<>(), relWordSet = new HashSet<>();

    public static void main(String[] args) throws Exception {
        //stopSet = StopWordLoader.getStopSet(stopPath);
        //workForKeywordExtraction(path + "/random_patty_keywords.txt");
        VecLoader.load();
        workForVecCreation(path + "");
    }
}
