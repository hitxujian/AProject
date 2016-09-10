package xusheng.kg.baike.link;

import fig.basic.LogInfo;
import xusheng.kg.baike.link.structure.Graph;
import xusheng.util.struct.MultiThread;

import java.io.*;
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

    public static void work(int idx) throws IOException{
        String task = taskList.get(idx);
        String[] spt = task.split("\t");
        if (spt[3].equals("NULL")) {
            LogInfo.logs("[T%s|W%d] No candidates found.", Thread.currentThread().getName(), idx);
            return;
        }
        int st = Integer.parseInt(spt[0]);
        List<Integer> eds = new ArrayList<>();
        for (int i=3; i<spt.length; i++)
            eds.add(Integer.parseInt(spt[i]));
        Graph graph = bfs(st, eds);
        if (graph.numOfPath > 3) graph.printGraph();
        int top = graph.pageRank();
        String ret = "";
        if (top == -1)
            ret = task + "\t[[" + spt[3] + "]]\n";
        else
            ret = task + "\t[[" + top + "]]\n";
        addToRet(ret);
        LogInfo.logs("[T%s|W%d] Linked ", Thread.currentThread().getName(), idx);
    }

    // bfs within 4 steps
    public static Graph bfs(int st, List<Integer> eds) {
        Graph graph = new Graph(st);
        List<List<Integer>> lists = new ArrayList<>();
        lists.add(new ArrayList<>());
        lists.get(0).add(st);
        for (int step=0; step<maxLen; step++) { // maxlen steps
            int len = lists.size();
            for (int i=0; i<len; i++) {
                if (lists.get(i).size() < step + 1)
                    continue;
                List<Integer> list = lists.get(i);
                int last = list.get(list.size()-1);
                if (eds.contains(last))
                    graph.addPath(list);
                if (!kb.containsKey(last))
                    continue;
                for (int expand: kb.get(last)) {
                    List<Integer> newList = new ArrayList<>(list);
                    newList.add(expand);
                    lists.add(newList);
                }
            }
        }
        return graph;
    }


    public static synchronized void writeRet(String ret) throws IOException {
        bw.write(ret);
    }

    public static synchronized void addToRet(String ret) throws IOException {
        triRet.add(ret);
    }

    // read linked part of graph
    // linked-list, no need of edge name at current stage
    public static Map<Integer, List<Integer>> kb = null;
    public static void readLinked() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/infobox/KB_linked.tsv"));
        String line;
        kb = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int subj = Integer.parseInt(spt[0]);
            int obj = Integer.parseInt(spt[2]);
            // left->right direction
            if (!kb.containsKey(subj))
                kb.put(subj, new ArrayList<>());
            kb.get(subj).add(obj);
            // right->left direction
            if (!kb.containsKey(obj))
                kb.put(obj, new ArrayList<>());
            kb.get(obj).add(subj);
            /*
            // right->left direction (negative number!)
            if (!kb.containsKey(obj))
                kb.put(obj, new ArrayList<>());
            kb.get(obj).add(-subj);
            */
        }
        br.close();
        LogInfo.logs("[info] KB loaded. Size: %d.", kb.size());
    }

    public static List<String> taskList = null;
    public static void readUnLinked() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/infobox/KB_unlinked.candi.tsv"));
        String line;
        taskList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            taskList.add(line);
        }
        br.close();
        LogInfo.logs("[info] Tasks loaded. Size: %d.", taskList.size());
    }

    public static BufferedWriter bw = null;
    public static List<String> triRet = null;
    public static void multiThreadWork() throws Exception{
        readLinked();
        readUnLinked();
        triRet = new ArrayList<>();
        curr = 0; end = taskList.size();
        int numOfThreads = 20;
        CandiGenerator workThread = new CandiGenerator();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        bw = new BufferedWriter(new FileWriter(rootFp + "/infobox/KB_addLinks.tsv"));
        multi.runMultiThread();
        LogInfo.logs("[info] Now write linked triples into file.");
        for (String triple: triRet)
            bw.write(triple);
        bw.close();
        LogInfo.logs("[info] Linking mission completed.");
        LogInfo.end_track();
    }

    public static int maxLen = 4;
    public static void main(String[] args) throws Exception {
        if (args.length != 0)
            maxLen = Integer.parseInt(args[0]);
        multiThreadWork();
    }
}
