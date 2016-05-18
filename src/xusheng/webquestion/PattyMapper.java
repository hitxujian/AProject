package xusheng.webquestion;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Xusheng on 5/17/2016.
 * To map the WebQuestion relations to the Patty Synsets
 */

public class PattyMapper implements Runnable{
    public static String pattyFp = "/home/data/PATTY/patty-dataset-freebase/" +
            "remove-type-signature/Matt-Fb3m_med/pattern-support-dist.txt";
    public static String pattyKeyWFp = "/home/xusheng/AProject/data/patty/keywords_clean.txt";
    public static String webqFp = "/home/xusheng/WebQ/questions.v2";


    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                int ret = map(idx);
                writeRes(idx + "\t" + ret + "\n");
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
            return ret;
        }
        return -1;
    }

    public static synchronized void writeRes(String ret) throws IOException{
        bw.write(ret);
    }

    public static BufferedWriter bw = null;
    public static void multiThreadWork() throws Exception {
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
        bw.close();
        LogInfo.end_track();
    }

    public static Map<Integer, String> webqMap = new HashMap<>();
    public static void readWebQ() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(webqFp));
        String line;
        while ((line = br.readLine()) != null) {
            String idx = line.split("\t")[1];
            line = br.readLine();
            String question = line.split("\t")[1];
            webqMap.put(Integer.parseInt(idx), question);
        }
        br.close();
        LogInfo.logs("webquestions read. size: %d", webqMap.size());
    }

    public static Map<Integer, Set<String>> pattyMap = new HashMap<>();
    public static void readPattyKeyWords() throws IOException {
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

    public static Map<Integer, Integer> pattySuppMap = new HashMap<>();
    public static void readPattySupport() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(pattyFp));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split(" | ")[0].split(" ==>    ");
            pattySuppMap.put(Integer.parseInt(spt[1]), Integer.parseInt(spt[0]));
        }
        br.close();
        LogInfo.logs("patty support file read. size: %d", pattySuppMap.size());
    }

    public static double getScore(Set<String> setA, String[] listB) {
        Set<String> setB = new HashSet<>();
        for (String str: listB) setB.add(str);
        int cnt = 0;
        for (String str: setA) {
            if (setB.contains(str)) cnt ++;
        }
        return (double) cnt / setA.size();
    }

    public static int map(int idx) {
        String q = webqMap.get(idx);
        String question = q.substring(0, q.length() - 1);
        String[] wordList = question.split(" ");
        double maxScore = 0;
        Set<Integer> maxSet = new HashSet<>();
        for (Map.Entry<Integer, Set<String>> entry: pattyMap.entrySet()) {
            int pid = entry.getKey();
            double tmp = getScore(entry.getValue(), wordList);
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
        return retIdx;
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
