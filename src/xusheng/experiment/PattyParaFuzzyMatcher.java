package xusheng.experiment;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.misc.StopWordLoader;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 1/17/2016.
 */
public class PattyParaFuzzyMatcher implements Runnable {

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

    public static String[] taskList = new String[640000];
    public static HashMap<Integer, HashSet<String>> pattyData = new HashMap<>();
    public static HashSet<String> stopSet = null;
    public static HashSet<Pair<Integer, Integer>> retPair = new HashSet<>();
    public static BufferedWriter bw;
    public static int curr = -1, end = -1;

    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            if (curr % 1000 == 0) LogInfo.logs("Current Idx: %d [%s]", curr, new Date().toString());
            return ret;
        }
        return -1;
    }

    public static synchronized void writeRet(Pair<Integer, Integer> pair) throws Exception {
        if (retPair.contains(pair)) return;
        bw.write(pair.getFirst() + "\t" + pair.getSecond() + "\n");
        retPair.add(pair);
    }

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                String[] spt = taskList[idx].split("\t");

                String left[] = spt[0].split(" ");
                String right[] = spt[1].split(" ");
                HashSet<String> leftWords = new HashSet<>();
                for (String word: left)
                    if (!word.equals("") && !stopSet.contains(word)) leftWords.add(word);
                HashSet<String> rightWords = new HashSet<>();
                for (String word: right)
                    if (!word.equals("") && !stopSet.contains(word)) rightWords.add(word);

                // if two phrases's keywords are similar, just skip it
                if (similar(leftWords, rightWords)) continue;

                HashSet<Integer> leftMatch = fuzzyMatch(leftWords, pattyData);
                HashSet<Integer> rightMatch = fuzzyMatch(rightWords, pattyData);

                if (leftMatch == null || rightMatch == null) continue;
                for (int lidx: leftMatch)
                    for (int ridx: rightMatch) {
                        //if (!lmatch.equals(rightMatch)) bw.write(lmatch + "\t###\t" + rmatch + "\n");
                        // if two relation's EP has no intersection, just pass it
                        if (lidx == ridx || !hasIntersectEP(lidx, ridx)) continue;
                        Pair<Integer, Integer> pair;
                        if (lidx < ridx) pair = new Pair<>(lidx, ridx);
                        else pair = new Pair<>(ridx, lidx);
                        writeRet(pair);
                    }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void work(String _120) throws Exception {
        /*
        Process patty file, select most occur 3 keywords
         */
        if (_120.equals("120")) pattyFile = dataFile + "/patty/patty120.txt";
        BufferedReader br = new BufferedReader(new FileReader(pattyFile));
        if (_120.equals("120"))
            bw = new BufferedWriter(new FileWriter(dataFile + "/patty/keywords-120.txt"));
        else
            bw = new BufferedWriter(new FileWriter(dataFile + "/patty/keywords.txt"));
        String line = br.readLine();
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int idx = Integer.parseInt(spt[0]);
            String pattern = spt[1];
            String[] relations = pattern.split(";\\$");
            //for (String str: relations) LogInfo.logs(str);
            HashMap<String, Integer> occurence = new HashMap<>();
            for (int i=0; i<relations.length; i++) {
                String[] words = relations[i].split(" ");
                for (int j=0; j<words.length; j++) {
                    if (words[j].startsWith("[") || stopSet.contains(words[j]) || words[j].equals(""))
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
            //System.out.print(idx + "\t");
            while (i<3 && i<sorted.size()) {
                //System.out.print("\t" + sorted.get(i).getKey() + "\t" + sorted.get(i).getValue());
                keywords.add(sorted.get(i).getKey());
                i++;
            }
            //System.out.print("\n");
            pattyData.put(idx, keywords);
            bw.write(String.valueOf(idx));
            for (String str: keywords) bw.write("\t" + str);
            bw.write("\n");
            if (LogUpgrader.showLine(cnt, 10000)) LogInfo.logs(keywords);
            cnt ++;
        }
        LogInfo.logs("Total %d PATTY relations.", cnt);
        br.close();
        bw.close();

        /*
        Process ppdb file
        first stage: use all of the data;
        second stage: only use those keywords are different
          */
        br = new BufferedReader(new FileReader(ppdbFile));
        cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt++;
            //LogUpgrader.showLine(cnt, 10000);
            String[] spt = line.split("\\|\\|\\|");
            String newline = spt[1] + "\t" + spt[2];
            taskList[cnt] = newline;
        }
        br.close();
        LogInfo.logs("ppdb file read into taskList.");

        curr = 1;
        end = cnt + 1;
        if (_120.equals("120"))
            bw = new BufferedWriter(new FileWriter(dataFile + "/patty/matchRet-120.txt"));
        else
            bw = new BufferedWriter(new FileWriter(dataFile + "/patty/matchRet.txt"));
        LogInfo.begin_track("Begin fuzzy match...");
        int threads = 8;
        PattyParaFuzzyMatcher workThread = new PattyParaFuzzyMatcher();
        MultiThread multiThread = new MultiThread(threads, workThread);
        LogInfo.logs("%d threads are running...", threads);
        multiThread.runMultiThread();
        bw.close();
        LogInfo.end_track();
    }

    public static HashSet<Integer> fuzzyMatch(HashSet<String> words, HashMap<Integer, HashSet<String>> patty) {
        HashSet<Integer> ret = new HashSet<>();
        for (Map.Entry<Integer, HashSet<String>> entry: patty.entrySet()) {
            int cnt = 0;
            int idx = entry.getKey();
            HashSet<String> set = entry.getValue();
            for (String str: entry.getValue())
                if (words.contains(str)) cnt ++;
            if ((float) cnt/set.size() >= 0.6 && (float) cnt/words.size() >= 0.66) {
                //String str = String.valueOf(idx) + "\t" + set.toString();
                ret.add(idx);
            }
        }
        if (ret.size()>0) return ret;
        else return null;
    }

    public static boolean similar(HashSet<String> setA, HashSet<String> setB) {
        int cnt = 0;
        for (String strA: setA)
            for (String strB: setB) {
                if (strA.equals(strB)) cnt ++;
            }
        if ((float) cnt / setA.size() > 0.6 && (float) cnt / setB.size() > 0.6)
            return true;
        else return false;
    }

    public static boolean hasIntersectEP(int idxA, int idxB) {
        HashSet<String> setA = instances.get(idxA);
        HashSet<String> setB = instances.get(idxB);
        if (idxA == 22528 && idxB == 67314) {
            LogInfo.logs("22528" + setA.toString());
            LogInfo.logs("67314" + setB.toString());
        }
        if (setA.size() < 5 || setB.size() < 5) return false;
        int cnt = 0;
        for (String str: setA)
            if (setB.contains(str))  cnt ++;
        if (cnt > 1) return true;
        else return false;
    }

    public static HashMap<Integer, HashSet<String>> instances = new HashMap<>();
    public static void readInstance(String thresh) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(dataFile + "/patty/wikipedia-instances.txt"));
        String line;
        int cnt = 0, num = 0;
        int threshold = Integer.parseInt(thresh);
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 3000000);
            String[] spt = line.split("\t");
            int idx = Integer.parseInt(spt[0]);
            if (!instances.containsKey(idx))
                instances.put(idx, new HashSet<>());
            instances.get(idx).add(spt[1] + "\t" + spt[2]);
        }
        br.close();
        for (Map.Entry<Integer, HashSet<String>> entry: instances.entrySet()) {
            if (entry.getValue().size()>threshold) num ++;
        }
        LogInfo.logs("Instances read. size: %d", instances.size());
        LogInfo.logs("Size of > %d: %d", threshold, num);
    }

    public static void main(String[] args) throws Exception {
        //extract120();
        stopSet = StopWordLoader.getStopSet(stopWFile);
        readInstance(args[1]);
        work(args[0]);
    }

}
