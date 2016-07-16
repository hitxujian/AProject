package xusheng.kg.baike.relation;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Xusheng on 7/13/2016.
 * Cluster cleaned-up relations in semantic level
 * by using embedding vectors.
 */
public class SemanticGrouper implements Runnable{

    public static String rootFp = "/home/xusheng/starry/baidubaike";
    public static String relFp = rootFp + "/infobox.text";


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

    public static int curr = -1, end = -1;
    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            return ret;
        }
        return -1;
    }

    public static void multiThreadWork() throws Exception {
        readRelEpMap();
        curr = 1; end = 20;
        LogInfo.logs("Begin to construct vector rep. of relations...");
        int numOfThreads = 8;
        SemanticGrouper workThread = new SemanticGrouper();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    public static void work(int idx) throws Exception {
        int ed = idx * 10000;
        int st = ed - 10000 + 1;
        Map<String, String> map = new HashMap<>();

    }

    public static int numOfRel = 0;
    public static String[] rels = null;
    public static void readRelation() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "edge_dict.tsv.v1"));
        String line;
        rels = new String[numOfRel];
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            rels[Integer.parseInt(spt[0])] = spt[1];
        }
        br.close();
    }

    public static List<String>[] relTasks = null;
    public static void readRelEpMap() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "infobox.text.v1"));
        String line;
        relTasks = new List[numOfRel];
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            relTasks[Integer.parseInt(spt[1])].add(spt[0] + "\t" + spt[2]);
        }
        br.close();
        LogInfo.logs("Relation-Entity Pairs Map Loaded.");
    }

    public static void main(String[] args) throws Exception {
        numOfRel = Integer.parseInt(args[0]);
        multiThreadWork();
    }


}
