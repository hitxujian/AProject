package xusheng.kg.baike.link;

import fig.basic.LogInfo;
import xusheng.util.struct.MapHelper;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 8/29/2016.
 * Generate candidate entities to link for values in the infobox.
 * Method: fuzzy match.
 * Input: KB_linked.tsv & KB_unlinked.tsv
 * Output: KB_unlinked.candi.tsv
 */

public class CandiGenerator implements Runnable {
    public static String rootFp = "/home/xusheng/starry/hudongbaike";

    public static int curr = -1, end = -1;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                findCandidates(idx);
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

    public static BufferedWriter bw = null;
    public static void multiThreadWork() throws Exception{
        readTasks();
        readData();
        bw = new BufferedWriter(new FileWriter(rootFp + "/infobox/KB_unlinked.candi.tsv"));
        curr = 0; end = 0;
        int numOfThreads = 32;
        CandiGenerator workThread = new CandiGenerator();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        bw.close();
        LogInfo.end_track();
    }

    public static void findCandidates(int idx) throws IOException{
        String task = taskList.get(idx);
        String[] spt = task.split("\t");
        String target = spt[2];
        Set<Integer> candidates = new HashSet<>();
        // find candidates from entity-name map
        if (nameEntMap.containsKey(target))
            candidates.addAll(nameEntMap.get(target));
        // find candidates from prior map
        // need to re-consider
        if (aliasPriorMap.containsKey(target))
            candidates.addAll(sort(new HashMap<>(aliasPriorMap.get(target))));
        if (candidates.size() == 0)
            writeRet(task + "\tNULL\n");
        else {
            String ret = task;
            for (Integer candi: candidates)
                ret += ("\t" + candi);
            ret += "\n";
            writeRet(ret);
        }
    }

    public static List<Integer> sort(HashMap<Integer, Double> map) {
        ArrayList<Map.Entry<Integer, Double>> sorted = MapHelper.sort(map);
        List<Integer> ret = new ArrayList<>();
        for (int i=0; i<sorted.size(); i++)
            ret.add(sorted.get(i).getKey());
        return ret;
    }

    public static synchronized void writeRet(String ret) throws IOException {
        bw.write(ret);
    }

    public static Map<String, Map<Integer, Double>> aliasPriorMap = null;
    public static Map<String, List<Integer>> nameEntMap = null;
    public static void readData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/content/prior.tsv"));
        String line;
        aliasPriorMap = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (!aliasPriorMap.containsKey(spt[0]))
                aliasPriorMap.put(spt[0], new HashMap<>());
            int idx = Integer.parseInt(spt[1]);
            double prob = Double.parseDouble(spt[2]);
            aliasPriorMap.get(spt[0]).put(idx, prob);
        }
        LogInfo.logs("[info] Prior file loaded. Size: %d. [%s]", aliasPriorMap.size(), new Date().toString());
        // ---------------------------------------------------------------------------------------------------
        nameEntMap = new HashMap<>();
        br = new BufferedReader(new FileReader(rootFp + "/infobox/entName.tsv"));
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int entIdx = Integer.parseInt(spt[0]);
            for (int i = 1; i < spt.length; i++) {
                if (!nameEntMap.containsKey(spt[i]))
                    nameEntMap.put(spt[i], new ArrayList<>());
                nameEntMap.get(spt[i]).add(entIdx);
            }
        }
        LogInfo.logs("[info] Entity Name file loaded. Size: %d. [%s]", nameEntMap.size(), new Date().toString());
    }

    public static List<String> taskList = null;
    public static void readTasks() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/infobox/KB_unlinked.tsv"));
        String line;
        taskList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            taskList.add(line);
        }
        LogInfo.logs("[info] %d Tasks loaded. [%s]", taskList.size(), new Date().toString());
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
