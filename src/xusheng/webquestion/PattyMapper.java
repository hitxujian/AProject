package xusheng.webquestion;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.nlp.Lemmatizer;
import xusheng.util.nlp.StopWordLoader;
import xusheng.util.struct.MapHelper;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 5/17/2016.
 * To map the WebQuestion relations to the Patty Synsets
 */

public class PattyMapper implements Runnable{
    public static String pattyFp = "/home/data/PATTY/patty-dataset-freebase" +
            "/remove-type-signature";
    public static String pattySuppFp = pattyFp + "/pattern-support-dist.txt";
    public static String pattySrcFp = pattyFp + "/wikipedia-patterns.txt";
    public static String pattyKeyWFp = "/home/xusheng/AProject/data/patty/keywords_v2.txt";
    public static String webqFp = "/home/xusheng/WebQ/questions.lemma";
    public static String stopWFile = "/home/xusheng/AProject/data/misc/stop.simple";

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                LogInfo.logs("[%d] Working for Ques. No.%d... [%s]", idx, idx, new Date().toString());
                int ret = map(idx);
                if (ret != -1) add(ret);
                writeRes(idx + "\t" + ret + "\n");
                if (ret != -1)
                    LogInfo.logs("[" + idx + "]" + "\t" + webqMap.get(idx) + "|||"
                        + ret + "\t" + pattyMap.get(ret).toString() + "\t" + new Date().toString());
                else
                    LogInfo.logs("[" + idx + "]" + "\t" + webqMap.get(idx) + "|||"
                            + ret + "\t" + "NULL" + "\t" + new Date().toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int curr = -1, end = -1;
    public static synchronized int getCurr() {
        if (curr <= end) {
            int ret = curr;
            curr++;
            //if (curr % 10 == 0) LogInfo.logs("Current Idx: %d [%s]", curr, new Date().toString());
            return ret;
        }
        return -1;
    }

    public static synchronized void writeRes(String ret) throws IOException{
        bw.write(ret);
        bw.flush();
    }

    public static Set<Integer> retSet = new HashSet<>();
    public static synchronized void add(int idx) {
        retSet.add(idx);
    }

    public static BufferedWriter bw = null;
    public static Set<String> stopSet = null;
    public static void multiThreadWork() throws Exception {
        Lemmatizer.initPipeline();
        stopSet = StopWordLoader.getStopSet(stopWFile);
        readWebQ();
        readPattyKeyWords();
        readPattySupport();
        bw = new BufferedWriter(new FileWriter("/home/xusheng/WebQ/webqPattyMap.txt"));
        curr = 1; end = webqMap.size();
        LogInfo.logs("Begin to Map Webquestion relations to Patty synsets " +
                "from %d to %d...", curr, end);
        int numOfThreads = 8;
        PattyMapper workThread = new PattyMapper();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.logs("Total different patty synsets: %d", retSet.size());
        bw.close();
        LogInfo.end_track();
    }

    /*
     Read WebQuestions
     */
    public static Map<Integer, Set<String>> webqMap = new HashMap<>();
    public static void readWebQ() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(webqFp));
        String line; int idx = 0;
        while ((line = br.readLine()) != null) {
            /*if (line.trim().startsWith("Lemma")) {
                Set<String> set = new HashSet<>();
                String[] spt = line.split(", ");
                set.add(spt[0].split("\\[")[1]);
                for (int i=1; i<spt.length-1; i++) set.add(spt[i]);
                idx ++;
                webqMap.put(idx, set);
            }*/
            if (line.trim().startsWith("Extracting")) {
                idx ++;
                line = br.readLine();
                while (! line.trim().startsWith("Extracting")) {
                    if (line.trim().startsWith("<")) {
                        String relation = line.trim().split(", ")[1];
                        String[] spt = relation.split(" ");
                        String tmp = spt[0];
                        for (int i=1; i<spt.length; i++) tmp += (" " + spt[i]);
                        spt = Lemmatizer.lemmatize(tmp).split(" ");
                        Set<String> set = new HashSet<>();
                        for (int i=0; i<spt.length; i++) set.add(spt[i]);
                        webqMap.put(idx, set);
                    }
                    line = br.readLine();
                }
            }
        }
        br.close();
        LogInfo.logs("webquestions read. size: %d", webqMap.size());

    }

    /*
     Read PattyKeyWords todo: need to reconsider keywords filtering
     */
    public static Map<Integer, Set<String>> pattyMap = new HashMap<>();
    public static void readPattyKeyWords() throws Exception {
        File file = new File(pattyKeyWFp);
        if (!file.exists()) extractPattyKeyWords();
        BufferedReader br = new BufferedReader(new FileReader(pattyKeyWFp));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (spt.length == 1) continue;
            int idx = Integer.parseInt(spt[0]);
            pattyMap.put(idx, new HashSet<>());
            for (int i=1; i<spt.length; i++) pattyMap.get(idx).add(spt[i]);
        }
        br.close();
        LogInfo.logs("patty key words read. size: %d", pattyMap.size());
    }

    /*
     Extract key words fro Patty Synsets
     */
    public static void extractPattyKeyWords() throws Exception {
        LogInfo.logs("Start to extract keywords for patty...");
        BufferedReader br = new BufferedReader(new FileReader(pattySrcFp));
        BufferedWriter bw = new BufferedWriter(new FileWriter(pattyKeyWFp));
        String line;
        int cnt = 0;
        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int idx = Integer.parseInt(spt[0]);
            String pattern = spt[1];
            String[] relations = pattern.split(";\\$");
            HashMap<String, Integer> occurrence = new HashMap<>();
            for (int i=0; i<relations.length; i++) {
                String relation = Lemmatizer.lemmatize(relations[i]);
                String[] words = relation.split(" ");
                for (int j=0; j<words.length; j++) {
                    if (words[j].startsWith("[[") || stopSet.contains(words[j])
                            || words[j].equals("") || words[j].startsWith("-"))
                        continue;
                    if (!occurrence.containsKey(words[j]))
                        occurrence.put(words[j], 1);
                    else {
                        int tmp = occurrence.get(words[j]) + 1;
                        occurrence.put(words[j], tmp);
                    }
                }
            }
            ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(occurrence);
            int i=0;
            HashSet<String> keywords = new HashSet<>();
            while (i<8 && i<sorted.size()) {
                keywords.add(sorted.get(i).getKey());
                i++;
            }
            bw.write(String.valueOf(idx));
            for (String str: keywords) bw.write("\t" + str);
            bw.write("\n");
            cnt ++;
            if (LogUpgrader.showLine(cnt, 10000)) LogInfo.logs(keywords);
        }
    }

    /*
     Read Patty Support file
     */
    public static Map<Integer, Integer> pattySuppMap = new HashMap<>();
    public static void readPattySupport() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(pattySuppFp));
        String line; boolean flag = false;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\\|")[0].split("==>");
            if (flag) {
                flag = false;
                LogInfo.logs(spt[0].trim() + "\t" + spt[1].trim());
            }
            pattySuppMap.put(Integer.parseInt(spt[1].trim()),
                    Integer.parseInt(spt[0].trim()));
        }
        br.close();
        LogInfo.logs("patty support file read. size: %d", pattySuppMap.size());
    }

    public static double getScore(Set<String> setA, Set<String> setB) {
        int cnt = 0;
        for (String str: setA) {
            if (setB.contains(str)) cnt ++;
        }
        return (double) cnt / setA.size();
    }

    public static int map(int idx) {
        if (! webqMap.containsKey(idx))
            return -1;
        Set<String> qwordList = webqMap.get(idx);
        double maxScore = 0;
        Set<Integer> maxSet = new HashSet<>();
        for (Map.Entry<Integer, Set<String>> entry: pattyMap.entrySet()) {
            int pid = entry.getKey();
            double tmp = getScore(entry.getValue(), qwordList);
            if (tmp > maxScore) {
                maxSet.clear();
                maxSet.add(pid);
                maxScore = tmp;
            } else if (tmp == maxScore)
                maxSet.add(pid);
        }
        if (maxScore == 0) return -1;
        int maxSupp = 0, retIdx = -1;
        for (int candidate: maxSet) {
            if (pattySuppMap.get(candidate) > maxSupp) {
                retIdx = candidate;
                maxSupp = pattySuppMap.get(candidate);
            }
        }
        if (maxSupp < threshold) return -1;
        return retIdx;
    }

    public static int threshold;
    public static void main(String[] args) throws Exception {
        threshold = Integer.parseInt(args[0]);
        multiThreadWork();
    }
}
