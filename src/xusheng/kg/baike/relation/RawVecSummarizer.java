package xusheng.kg.baike.relation;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.misc.IndexNameReader;
import xusheng.util.struct.MapHelper;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.nio.Buffer;
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

    public static synchronized void writeRet(String ret) throws IOException{
        bw.write(ret);
    }

    public static void calcuSim(int idx) throws IOException{
        LogInfo.logs("[log] Working for relation No.%d. [%s]", idx, new Date().toString());
        List<String> vectorA = vectors.get(idx);
        Map<Integer, Double> posA = getPos(vectorA);
        // calculate similarities between relations whose idx are bigger
        for (int i=idx+1; i<=numOfRel; i++) {
            if (!vectors.containsKey(i)) continue;
            List<String> vectorB = vectors.get(i);
            Map<Integer, Double> posB = getPos(vectorB);
            double sum = 0;
            for (Map.Entry<Integer, Double> entry : posA.entrySet())
                if (posB.containsKey(entry.getKey()))
                    sum += entry.getValue() * posB.get(entry.getKey());
            writeRet(String.format("%d %d\t%.4f\n", idx, i, sum));
        }
        LogInfo.logs("[log] Done for relation No.%d. [%s]", idx, new Date().toString());
    }

    public static Map<Integer, Double> getPos(List<String> vec) {
        Map<Integer, Double> ret = new HashMap<>();
        for (String str: vec) {
            String[] spt = str.split(" ");
            ret.put(Integer.parseInt(spt[0]), Double.parseDouble(spt[1]));
        }
        return ret;
    }

    public static BufferedWriter bw;
    public static void multiThreadWork() throws Exception {
        readVectors();
        curr = 1;
        end = numOfRel;
        bw = new BufferedWriter(new FileWriter(rootFp + "/rel_simi.txt"));
        LogInfo.logs("Begin to calculate similarities...");
        int numberOfThreads = 32;
        RawVecSummarizer workThread = new RawVecSummarizer();
        MultiThread multi = new MultiThread(numberOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numberOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
        bw.close();
    }

    // ------------ pre-processing ---------------
    public static Map<Integer, List<String>> vectors = new HashMap<>();
    public static void readVectors() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/real_vectors.txt"));
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
        // step 1.
        //getTogether();
        //createTable();
        // step 2.
        numOfRel = Integer.parseInt(args[0]);
        //multiThreadWork();
        // step 3.
        sortAndShowRelName();
    }

    // ------------ construct tables ---------------
    public static void getTogether() throws IOException {
        Map<String, StringBuffer> map = new HashMap<>();
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/raw_vectors.txt.0"));
        for (int i=1; i<=3; i++) {
            BufferedReader br = new BufferedReader(new FileReader(rootFp + "/raw_vectors.txt." + i));
            String line;
            while ((line = br.readLine()) != null) {
                String[] spt = line.split("\t");
                if (spt.length < 2) {
                    LogInfo.logs(line);
                    continue;
                }
                if (!map.containsKey(spt[0]))
                    map.put(spt[0], new StringBuffer());
                map.get(spt[0]).append(spt[1]);
            }
            br.close();
        }
        for (Map.Entry<String, StringBuffer> entry: map.entrySet())
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        bw.close();
        LogInfo.logs("raw_vectors.txt.0 is written. Size: %d.", map.size());
    }

    public static Map<String, Integer> charSet = new HashMap<>();
    public static void createTable() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/raw_vectors.txt.0"));
        String line;
        int chIdx = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rawVec = spt[1];
            for (char ch: rawVec.toCharArray()) {
                if (!charSet.containsKey(String.valueOf(ch))) {
                    charSet.put(String.valueOf(ch), chIdx);
                    chIdx ++;
                }
            }
        }
        LogInfo.logs("Char set created. Size: %d", charSet.size());

        br = new BufferedReader(new FileReader(rootFp + "/raw_vectors.txt.0"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/real_vectors.txt"));
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rawVec = spt[1];
            double[] realvec = new double[charSet.size()];
            int relIdx = Integer.parseInt(spt[0]);
            int total = 0;
            for (char ch: rawVec.toCharArray()) {
                int idx = charSet.get(String.valueOf(ch));
                realvec[idx] ++;
                total ++;
            }
            bw.write(String.valueOf(relIdx));
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

    // ------------ post processing ----------------
    public static void sortAndShowRelName() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/rel_simi.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/rel_simi.visual"));
        String line;
        HashMap<String, Double> map = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            map.put(spt[0], Double.parseDouble(spt[1]));
        }
        List<Map.Entry<String, Double>> sorted = MapHelper.sort(map);
        IndexNameReader inr = new IndexNameReader(rootFp + "/edge_dict.tsv.v1");
        inr.initializeFromIdx2Name();
        for (int i=0; i<sorted.size(); i++) {
            String[] spt = sorted.get(i).getKey().split(" ");
            bw.write(inr.getName(Integer.parseInt(spt[0])) + " " +
                    inr.getName(Integer.parseInt(spt[1])) + "\t" +
                    sorted.get(i).getValue() + "\n");
        }
        bw.close();
        LogInfo.logs("Visualization for rel_simi.txt done.");
    }
}
