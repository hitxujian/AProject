package xusheng.kg.baike.relation;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 7/18/2016.
 *
 */
public class RawVecSummarizer implements Runnable {
    public static String rootFp = "/home/xusheng/starry/baidubaike";

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1 || !vectors.containsKey(idx)) return;
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

    public static Set<Pair<Integer, Integer>> visited = new HashSet<>();
    public static synchronized void addLock(Pair<Integer, Integer> pair) {
        visited.add(pair);
    }

    public static void calcuSim(int idx) {
        List<String> vector = vectors.get(idx);
    }

    public static void multiThreadWork() throws Exception {
        readVectors();
        curr = 1;
        end = numOfRel;
        LogInfo.logs("Begin to calculate similarities...");
        int numberOfThreads = 32;
        RawVecSummarizer workThread = new RawVecSummarizer();
        MultiThread multi = new MultiThread(numberOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numberOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    // ------------ pre-processing ---------------
    public static Map<Integer, List<String>> vectors = new HashMap<>();
    public static void readVectors() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/real_vectors"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int idx = Integer.parseInt(spt[0]);
            vectors.put(idx, new ArrayList<>());
            for (int i=1; i<spt.length; i++)
                vectors.get(idx).add(spt[i]);
        }
        LogInfo.logs("Relation vectors loaded. Size: %d", vectors.size());
    }

    public static int numOfRel = -1;
    public static void main(String[] args) throws Exception {
        createTable();
        numOfRel = Integer.parseInt(args[0]);
        multiThreadWork();
    }

    // ------------ construct tables ---------------
    public static Map<String, Integer> charSet = new HashMap<>();
    public static void createTable() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/raw_vectors"));
        String line;
        int chIdx = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rawVec = spt[1];
            for (char ch: rawVec.toCharArray()) {
                if (!charSet.containsKey(ch)) {
                    charSet.put(String.valueOf(ch), chIdx);
                    chIdx ++;
                }
            }
        }
        LogInfo.logs("Char set created. Size: %d", charSet.size());

        br = new BufferedReader(new FileReader(rootFp + "/raw_vectors"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/real_vectors"));
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rawVec = spt[1];
            double[] realvec = new double[charSet.size()];
            int relIdx = Integer.parseInt(spt[0]);
            int total = 0;
            for (char ch: rawVec.toCharArray()) {
                int idx = charSet.get(ch);
                realvec[idx] ++;
                total ++;
            }
            bw.write(relIdx);
            for (int i=0; i<charSet.size(); i++) {
                if (realvec[i] != 0) {
                    double tmp = realvec[i] / total;
                    String ret = String.format("%.4f", tmp);
                    bw.write("\t" + i + " " + ret);
                }
            }
            bw.write("\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Real vectors generated.");
    }


}
