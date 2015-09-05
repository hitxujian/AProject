package xusheng.reverb;

import fig.basic.LogInfo;
import xusheng.util.struct.MapHelper;
import xusheng.util.struct.MultiThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 2015/5/2.
 * @author Xusheng Luo
 * This is a multi-Threads class aimed to calculate similarities between each relations,
 * read a vector file, and write a ret file.
 */

public class RvScorer4Fb implements Runnable{

    public static boolean verbose = true;

    /** calculate the score of two given vectors */
    public static double getScore(int idx1, int idx2) {
        //String[] v1 = vector.get(idx1).split("\t");
        //String[] v2 = vector.get(idx2).split("\t");
        ArrayList<Double> vec1 = vector.get(idx1);
        ArrayList<Double> vec2 = vector.get(idx2);
        //for (int i=1; i<v1.length; i++) vec1.add(Double.parseDouble(v1[i]));
        //for (int i=1; i<v2.length; i++) vec2.add(Double.parseDouble(v2[i]));

        double ret = 0;
        int size = vec1.size() < vec2.size()? vec1.size(): vec2.size();
        for (int i=0; i<size; i++)
            ret += vec1.get(i) * vec2.get(i);
        return ret;
    }

    // -------------------------------------------------------------------------------------------------- //

    public static ArrayList<ArrayList<Double>>
            vector = new ArrayList<>();
    public static HashMap<Integer, HashMap<Integer, Double>> graph = new HashMap();
    public static ArrayList<String> idxMatch = new ArrayList<>();
    public static BufferedWriter bw;
    public static int curr = -1, end = -1;
    public static double threshold = 0.5;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                HashMap<Integer, Double> ret = new HashMap<>();
                for (int i=1; i<end; i++) {
                    double score = getScore(idx, i);
                    if (score > threshold) ret.put(i, score);
                }
                StringBuffer sb = new StringBuffer();
                ArrayList<Map.Entry<Integer, Double>> sorted = MapHelper.sort(ret);
                for (Map.Entry<Integer, Double> entry: sorted)
                    sb.append("\t" + RvEqualSet.getName(idxMatch.get(entry.getKey())) + " " + entry.getValue());
                sb.append("\n");
                writeRes(idx, sb);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            //LogHelper.showLine(curr, 100);
            if (curr % 100 == 0) LogInfo.logs("Current Idx: %d [%s]", curr, new Date().toString());
            return ret;
        }
        return -1;
    }

    public static synchronized void writeRes(int idx, StringBuffer ret) throws Exception{
        bw.write("#" + RvEqualSet.getName(idxMatch.get(idx)) + "#" + ret.toString());
    }

    /** group all equal sets into synsets using vectors generated in RvVector */
    public static void multiThreadWorking(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        bw = new BufferedWriter(new FileWriter(outFile));

        String line = ""; vector.add(null); idxMatch.add(""); // index from 1
        LogInfo.logs("Start to read vectors...");
        int num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            if (num % 1000 == 0) LogInfo.logs("%d lines processed...", num);
            String[] spt = line.split("\t");
            idxMatch.add(spt[0]);
            ArrayList<Double> vec = new ArrayList<>();
            for (int i=1; i<spt.length; i++) vec.add(Double.parseDouble(spt[i]));
            vector.add(vec);
        }

        LogInfo.logs("Vectors read into memory!");

        curr = 1; end = vector.size();
        LogInfo.begin_track("Begin calculating similarities and grouping...");
        int threads = 8;
        RvScorer4Fb workThread = new RvScorer4Fb();
        MultiThread multi = new MultiThread(threads, workThread);
        LogInfo.begin_track("%d threads are running...", threads);
        multi.runMultiThread();
        LogInfo.end_track();

        /*LogInfo.logs("Graph construction complete, start writing");
        //DecimalFormat df = new DecimalFormat(".###");
        for (int i=1; i<vector.size(); i++) {
            bw.write("#" + RvEqualSet.getName(idxMatch.get(i)) + "#");
            ArrayList<Map.Entry<Integer, Double>> sorted = MapHelper.sort(graph.get(i));
                    //new MapDoubleHelper<Integer>().sort(graph.get(i));
            for (Map.Entry<Integer, Double> entry: sorted)
                bw.write("\t" + RvEqualSet.getName(idxMatch.get(entry.getKey())) + " " + entry.getValue());
            bw.write("\n");
        }
        LogInfo.logs("Written Complete!");*/
    }

    public static void main(String[] args) throws Exception {
        RvEqualSet.initializeFor3M(args[2]);
        multiThreadWorking(args[0], args[1]);
    }
}
