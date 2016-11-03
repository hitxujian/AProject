package xusheng.util.struct;

import fig.basic.LogInfo;
import fig.basic.Pair;

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

    // need to rewrite this func when facing different file format
    public static void work(int idx) {
        Pair<Integer, Integer> task = taskList.get(idx);
        int fst = task.getFirst();
        int snd = task.getSecond();
        Set<String> fstSet = null, sndSet = null;
        if (idxSetMap.containsKey(fst))
            fstSet = idxSetMap.get(fst);
        else fstSet = getSet(fst);
        if (idxSetMap.containsKey(snd))
            sndSet = idxSetMap.get(snd);
        else sndSet = getSet(snd);
        add2Ret(idxNameMap.get(fst) + "\t" + idxNameMap.get(snd) + "\t" + getJaccard(fstSet, sndSet) + "\n");
    }

    public static Set<String> getSet(int idx) {
        String line = dataList.get(idx);
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

    public static List<String> dataList = null;
    public static List<Pair<Integer, Integer>> taskList = null;
    public static void readTasks() throws IOException {
        String file = rootFp + "/" + fileName + ".txt";
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        dataList = new ArrayList<>();
        idxNameMap = new HashMap<>();
        idxSetMap = new HashMap<>();
        while ((line = br.readLine()) != null) {
            dataList.add(line);
        }
        br.close();
        LogInfo.logs("[log] Data from %s loaded. Size: %d.", file, dataList.size());

        taskList = new ArrayList<>();
        for (int i=0; i<dataList.size(); i++)
            for (int j=i+1; j<dataList.size(); j++)
                taskList.add(new Pair<>(i, j));
        LogInfo.logs("[log] Task from loaded. Size: %d.", taskList.size());
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
        multiThreadWork();
        writeRet();
    }
}
