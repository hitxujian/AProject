package xusheng.kg.baike.relation.baseline;

import fig.basic.LogInfo;
import xusheng.misc.IndexNameReader;
import xusheng.misc.SortVisualizer;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 7/28/2016.
 * Input is the idx-raw words form vectors
 * Output is the idx-idx relation similarity scores
 * Using the relation itself co-occurrence method
 */


public class CoOccurrence implements Runnable{
    public static String rootFp = "/home/xusheng/starry/baidubaike";

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                if (!vectors.containsKey(idx)) continue;
                calcuSim(idx);
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

    public static synchronized void writeR(String ret) throws IOException {
        bwR.write(ret);
    }

    public static synchronized void writeN(String ret) throws IOException {
        bwN.write(ret);
    }

    public static void calcuSim(int idx) throws IOException{
        LogInfo.logs("[log] Working for relation No.%d. [%s]", idx, new Date().toString());
        String relNameA = inr.getName(idx);
        for (int i=idx+1; i<=numOfRel; i++) {
            if (!vectors.containsKey(i)) continue;
            int timesA = 0;
            String contentB = vectors.get(i);
            int lenB = contentB.length();
            while (contentB.contains(relNameA)) {
                timesA ++;
                contentB = contentB.substring(contentB.indexOf(relNameA) + relNameA.length(), contentB.length());
            }
            // other side
            int timesB = 0;
            String contentA = vectors.get(idx);
            int lenA = contentA.length();
            String relNameB = inr.getName(i);
            while (contentA.contains(relNameB)) {
                timesB ++;
                contentA = contentA.substring(contentA.indexOf(relNameB) + relNameB.length(), contentA.length());
            }
            double ratioA = (double)(timesA * relNameA.length()) / lenA;
            double ratioB = (double)(timesB * relNameB.length()) / lenB;
            writeN(idx + " " + i + "\t" + (timesA+timesB) + "\n");
            writeR(idx + " " + i + "\t" + (ratioA+ratioB) + "\n");
        }
        LogInfo.logs("[log] Done for relation No.%d. [%s]", idx, new Date().toString());
    }

    public static BufferedWriter bwR, bwN;
    public static IndexNameReader inr;
    public static void multiThreadWork() throws Exception {
        readVectors();
        curr = 1;
        end = numOfRel;
        inr = new IndexNameReader("/home/xusheng/starry/baidubaike/edge_dict.tsv.v1");
        inr.initializeFromIdx2Name();
        bwR = new BufferedWriter(new FileWriter(rootFp + "/rel_cooccurR.txt"));
        bwN = new BufferedWriter(new FileWriter(rootFp + "/rel_cooccurN.txt"));
        LogInfo.logs("Begin to calculate similarities...");
        int numberOfThreads = 32;
        CoOccurrence workThread = new CoOccurrence();
        MultiThread multi = new MultiThread(numberOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numberOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
        bwR.close();
        bwN.close();
        SortVisualizer.sortAndShowRelName(rootFp + "/rel_cooccurR.txt",
                rootFp + "/rel_cooccurR.txt.vis", rootFp + "/edge_dict.tsv.v1");
        SortVisualizer.sortAndShowRelName(rootFp + "/rel_cooccurN.txt",
                rootFp + "/rel_cooccurN.txt.vis", rootFp + "/edge_dict.tsv.v1");
    }


    public static Map<Integer, String> vectors = new HashMap<>();
    public static void readVectors() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/raw_vectors.txt.0"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int idx = Integer.parseInt(spt[0]);
            vectors.put(idx, spt[1]);
        }
        LogInfo.logs("Relation vectors loaded. Size: %d", vectors.size());
    }

    public static void sortRet() throws IOException {
        SortVisualizer.sortAndShowRelName(rootFp + "/rel_cooccurR.txt",
                rootFp + "/rel_cooccurR.txt.vis", rootFp + "/edge_dict.tsv.v1");
        SortVisualizer.sortAndShowRelName(rootFp + "/rel_cooccurN.txt",
                rootFp + "/rel_cooccurN.txt.vis", rootFp + "/edge_dict.tsv.v1");
    }

    public static int numOfRel = -1;
    public static void main(String[] args) throws Exception {
        numOfRel = Integer.parseInt(args[0]);
        if (args[1].equals("MULTI")) multiThreadWork();
        else sortRet();
    }
}
