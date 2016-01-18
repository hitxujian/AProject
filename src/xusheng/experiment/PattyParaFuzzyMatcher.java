package xusheng.experiment;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.misc.StopWordLoader;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Xusheng on 1/17/2016.
 */
public class PattyParaFuzzyMatcher {

    public static String dir = "/home/kangqi/workspace/UniformProject/resources/paraphrase/emnlp2015/" +
            "PATTY120_Matt-Fb2m_med_gGD_s20_len3_fb1_sh0_aT0_c150_c21.2_aD1_SF1_SL1_cov0.10_pH10_dt1.0_sz30000_aI1";
    public static String dataFile = "/home/xusheng/AProject/data";
    public static String pattyFile = dataFile + "/patty/wikipedia-patterns.txt";
    public static String ppdbFile = dataFile + "/ppdb-1.0-s-phrasal";
    public static String stopWFile = dataFile + "/misc/stop.simple";
    public static String paralexFile = dataFile + "";

    public static boolean verbose = true;

    public static HashSet<String> patty120idx = null;

    public static void extract120() throws IOException{
        patty120idx = new HashSet<>();
        try {
            File root = new File(dir);
            File[] files = root.listFiles();
            if (verbose) LogInfo.logs(files.length);
            for (int i=0; i<files.length; i++) {
                if (files[i].isDirectory()) {
                    File file = new File(files[i].getAbsolutePath() + "/schema");
                    if (!file.exists()) continue;
                    String name = files[i].getName();
                    if (verbose) LogInfo.logs(name);
                    String idx = name.split("_")[0];
                    if (verbose) LogInfo.logs(idx);
                    if (idx != null) patty120idx.add(idx);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (verbose) LogInfo.logs(patty120idx.size());
        BufferedReader br = new BufferedReader(new FileReader(pattyFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/patty120.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            String idx = line.split("\t")[0];
            if (idx != null && patty120idx.contains(idx)) {
                bw.write(line + "\n");
            }
        }
        br.close();
        bw.close();
    }

    public static ArrayList<HashSet<String>> pattyData = null;
    public static HashSet<String> stopSet = null;
    public static HashSet<Pair<Integer, Integer>> retPair = null;

    public static void work() throws IOException {
        /*
        Process patty file, select most occur 3 keywords
         */
        pattyFile = dataFile + "/patty/patty120.txt";
        BufferedReader br = new BufferedReader(new FileReader(pattyFile));
        String line = br.readLine();
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String idx = spt[0];
            String pattern = spt[1];
            String[] relations = pattern.split(";$");
            HashMap<String, Integer> occurence = new HashMap<>();
            for (int i=0; i<relations.length; i++) {
                String[] words = relations[i].split(" ");
                for (int j=0; j<words.length; j++) {
                    if (words[j].startsWith("[") || stopSet.contains(words[j]))
                        continue;
                    if (!occurence.containsKey(words[j])) occurence.put(words[j], 1);
                    else {
                        int tmp = occurence.get(words[j]) + 1;
                        occurence.put(words[j], tmp);
                    }
                }
            }
            ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(occurence);
            int i=0;
            HashSet<String> keywords = new HashSet<>();
            while (i<3 && i<sorted.size()) keywords.add(sorted.get(i).getKey());
            pattyData.add(keywords);
            if (LogUpgrader.showLine(cnt, 10)) LogInfo.logs(keywords);
            cnt ++;
        }
        LogInfo.logs("Total %d PATTY relations.", cnt);
        br.close();

        /*
        Process ppdb file
          */
        br = new BufferedReader(new FileReader(ppdbFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile + "/patty/matchRet.txt"));
        cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 10000);
            String[] spt = line.split("\\|\\|\\|");
            String left[] = spt[1].split(" ");
            String right[] = spt[2].split(" ");
            HashSet<String> leftWords = new HashSet<>();
            for (String word: left)
                if (!stopSet.contains(word)) leftWords.add(word);
            HashSet<String> rightWords = new HashSet<>();
            for (String word: right)
                if (!stopSet.contains(word)) rightWords.add(word);
            ArrayList<String> leftMatch = fuzzyMatch(leftWords, pattyData);
            ArrayList<String> rightMatch = fuzzyMatch(rightWords, pattyData);

            if (leftMatch == null || rightMatch == null) continue;
            for (String lmatch: leftMatch)
                for (String rmatch: rightMatch) {
                    //if (!lmatch.equals(rightMatch)) bw.write(lmatch + "\t###\t" + rmatch + "\n");
                    int lidx = Integer.parseInt(lmatch.split("\t")[0]);
                    int ridx = Integer.parseInt(rmatch.split("\t")[1]);
                    if (lidx == ridx) continue;
                    Pair<Integer, Integer> pair;
                    if (lidx < ridx) pair = new Pair<>(lidx, ridx);
                    else pair = new Pair<>(ridx, lidx);
                    retPair.add(pair);
                }
        }
        for (Pair<Integer, Integer> pair : retPair) {
            bw.write(pair.getFirst() + "\t" + pair.getSecond() + "\n");
        }
        br.close();
        bw.close();
    }

    public static ArrayList<String> fuzzyMatch(HashSet<String> words, ArrayList<HashSet<String>> patty) {
        ArrayList<String> ret = new ArrayList<>();
        int idx = 0;
        for (HashSet<String> set: patty) {
            int cnt = 0;
            for (String str: set)
                if (words.contains(str)) cnt ++;
            if (cnt > 0) {
                String str = String.valueOf(idx) + "\t" + set.toString();
                ret.add(str);
            }
            idx ++;
        }
        if (ret.size()>0) return ret;
        else return null;
    }

    public static void main(String[] args) throws Exception {
        //extract120();
        stopSet = StopWordLoader.getStopSet(stopWFile);
        work();
    }

}
