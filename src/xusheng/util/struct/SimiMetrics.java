package xusheng.util.struct;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.util.log.LogUpgrader;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 03/11/2016.
 * Get similarity of two terms using different metrics (e.g. Jaccard)
 * Input is a file with multiple lines while each line represents one term
 * and its corresponding descriptions.
 */

public class SimiMetrics implements Runnable {
    public static String rootFp = "/home/yuchen/wn/similarity";

    public static int curr = -1, end = -1;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                work(idx);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            return ret;
        }
        return -1;
    }

    public static Map<Integer, String> idxNameMap = null;
    public static synchronized void add2IdxNameMap(int idx, String name) {
        if (!idxNameMap.containsKey(idx))
            idxNameMap.put(idx, name);
    }

    public static Map<Integer, Set<String>> idxSetMap = null;
    public static synchronized void add2IdxSetMap(int idx, Set<String> set) {
        if (!idxSetMap.containsKey(idx))
            idxSetMap.put(idx, set);
    }

    public static List<String> retList = new ArrayList<>();
    public static synchronized void add2Ret(String ret) {
        retList.add(ret);
    }

    public static BufferedWriter bw = null;
    public static synchronized void write2Ret(String ret) throws IOException {
        bw.write(ret);
    }

    public static int cnt = 0;
    public static synchronized void checkProgress() {
        cnt ++;
        if (cnt % 1000 == 0)
            LogInfo.logs("[log] %d / %d done. [%s]", cnt, taskList.size(), new Date().toString());
    }

    // need to rewrite this func when facing different file format
    public static void work(int fst) throws IOException {
        //LogInfo.logs("[T%s] Start to work for Task %d / %d. [%s]", Thread.currentThread().getName(), fst, taskList.size(), new Date().toString());
        Set<String> fstSet = null, sndSet = null;
        if (idxSetMap.containsKey(fst))
            fstSet = idxSetMap.get(fst);
        else fstSet = getSet(fst);

        for (int snd=fst+1; snd<taskList.size(); snd ++) {
            if (idxSetMap.containsKey(snd))
                sndSet = idxSetMap.get(snd);
            else sndSet = getSet(snd);
            double score = getJaccard(fstSet, sndSet);
            if (score > 0)
                write2Ret(idxNameMap.get(fst) + "\t" + idxNameMap.get(snd) + "\t" + String.format("%.4f", score) + "\n");
        }
        checkProgress();
        //LogInfo.logs("[T%s] Finish working for Task %d / %d. [%s]", Thread.currentThread().getName(), fst, taskList.size(), new Date().toString());
    }

    public static Set<String> getSet(int idx) {
        String line = taskList.get(idx);
        String[] spt = line.split("\t");
        String name = spt[0];
        add2IdxNameMap(idx, name);
        Set<String> decs = new HashSet<>();
        for (int i=1; i<spt.length; i++)
            if (spt[i].startsWith("SID")) decs.add(spt[i]);
        add2IdxSetMap(idx, decs);
        return decs;
    }

    public static double getJaccard(Set<String> setA, Set<String> setB) {
        int intersection = 0;
        for (String str: setA)
            if (setB.contains(str))
                intersection ++;
        return (double) intersection / (setA.size() + setB.size() - intersection);
    }

    public static void multiThreadWork() throws Exception {
        curr = 0;
        end = taskList.size();
        int numOfThreads = 32;
        SimiMetrics workThread = new SimiMetrics();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("[log] %d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    public static List<String> taskList = null;
    public static void readTasks() throws IOException {
        String file = rootFp + "/" + fileName + ".txt";
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        taskList = new ArrayList<>();
        idxNameMap = new HashMap<>();
        idxSetMap = new HashMap<>();
        while ((line = br.readLine()) != null) {
            taskList.add(line);
        }
        br.close();
        LogInfo.logs("[log] Data from %s loaded. Size: %d.", file, taskList.size());
    }

    public static void writeRet() throws IOException {
        String file = "/home/xusheng/yuchen/ret_" + fileName +  ".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (String line: retList)
            bw.write(line);
        bw.close();
        LogInfo.logs("[log] Result written into %s.", file);
    }

    public static String fileName = "circleVerb";
    public static void main(String[] args) throws Exception {
        if (args.length != 0)
            fileName = args[0];
        readTasks();
        String file = "/home/xusheng/yuchen/ret_" + fileName +  ".txt";
        bw = new BufferedWriter(new FileWriter(file));
        multiThreadWork();
        bw.close();
        LogInfo.logs("[log] Result written into %s.", file);
        //writeRet();
    }
}
