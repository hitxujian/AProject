package xusheng.kg.baike.link;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Xusheng on 9/5/2016.
 * Construct related entity graph using bfs (within 5 steps)
 * Run a page rank to select the best entity
 * Input: KB_linked.tsv & KB_unlinked.candi.tsv
 * Output: KB_full.tsv
 */

public class BasicLinker {
    public static String rootFp = "/home/xusheng/starry/hudongbaike";
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

    public static void work(int idx) {
        String task = taskList.get(idx);
        String[] spt = task.split("\t");
        if (spt[3].equals("NULL"))
            return;
        int st = Integer.parseInt(spt[0]);
        List<Integer> eds = new ArrayList<>();
        for (int i=3; i<spt.length; i++)
            eds.add(Integer.parseInt(spt[i]));
        bfs(st, eds);
    }

    // bfs within 5 steps
    public static void bfs(int st, List<Integer> eds) {

    }

    // read linked part of graph
    // linked-list, no need of edge name at current stage
    public static Map<Integer, List<Integer>> kb = null;
    public static void readLinked() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/KB_linked.tsv"));
        String line;
        kb = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int subj = Integer.parseInt(spt[0]);
            int obj = Integer.parseInt(spt[2]);
            if (!kb.containsKey(subj))
                kb.put(subj, new ArrayList<>());
            kb.get(subj).add(obj);
        }
        br.close();
        LogInfo.logs("[info] KB loaded. Size: %d.", kb.size());
    }

    public static List<String> taskList = null;
    public static void readUnLinked() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/KB_unlinked.tsv"));
        String line;
        taskList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            taskList.add(line);
        }
        br.close();
        LogInfo.logs("[info] Tasks loaded. Size: %d.", taskList.size());
    }

    public static BufferedWriter bw = null;
    public static void multiThreadWork() throws Exception{
        readLinked();
        readUnLinked();
        curr = 0; end = 0;
        int numOfThreads = 32;
        CandiGenerator workThread = new CandiGenerator();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
