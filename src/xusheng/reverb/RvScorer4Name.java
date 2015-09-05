package xusheng.reverb;

import fig.basic.LogInfo;
import kangqi.util.LogHelper;
import kangqi.util.MultiThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 2015/5/1.
 *
 * This is a multi-Threads class aimed to calculate similarities between each relations,
 * read a file named "vectors.txt", and write a ret file.
 */

public class RvScorer4Name implements Runnable{

    public static boolean verbose = true;

    // calculate the score of two given relations
    public static double getScore(int idx1, int idx2) {
        HashMap<String, Double> _1left = vector.get(idx1).get(0);
        HashMap<String, Double> _1right = vector.get(idx1).get(1);
        HashMap<String, Double> _2left = vector.get(idx2).get(0);
        HashMap<String, Double> _2right = vector.get(idx2).get(1);
        double score = 0;
        for (Map.Entry<String, Double> entry: _2left.entrySet()) {
            if (_1left.containsKey(entry.getKey()))
                score += _1left.get(entry.getKey()) * entry.getValue();
        }
        for (Map.Entry<String, Double> entry: _2right.entrySet()) {
            if (_1right.containsKey(entry.getKey()))
                score += _1right.get(entry.getKey()) * entry.getValue();
        }
        return score;
    }


    // -------------------------------------------------------------------------------------------------- //

    public static ArrayList<ArrayList<HashMap<String, Double> > >
            vector = new ArrayList<>();
    public static ArrayList<ArrayList<String>> graph = new ArrayList<>();
    public static int curr = -1, end = -1;
    public static double threshold = 0.1;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                ArrayList<String> ret = new ArrayList<>();
                for (int i=idx+1; i<end; i++) {
                    double score = getScore(idx, i);
                    if (score > threshold) ret.add(String.valueOf(i) + "\t" + String.valueOf(score));
                }
                writeRes(idx, ret);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            LogHelper.showLine(curr, 1000);
            if (curr % 1000 == 0) LogInfo.logs("Current Idx: %d [%s]", curr, new Date().toString());
            return ret;
        }
        return -1;
    }

    public static synchronized void writeRes(int idx, ArrayList<String> ret) {
        graph.set(idx, ret);
    }

    // group all equal sets into synsets using vectors generated in RvVector
    public static void multiThreadWorking(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

        String line = ""; vector.add(null); // idx from 1
        LogInfo.logs("Start to read vectors...");
        while ((line=br.readLine()) != null) {
            HashMap<String, Double> left = new HashMap<>();
            String spt[] = line.split("\t");
            for (int i=1; i<spt.length; i++) {
                String word = spt[i].split(" ")[0];
                String score = spt[i].split(" ")[1];
                left.put(word, Double.valueOf(score));
            }
            line = br.readLine();
            HashMap<String, Double> right = new HashMap<>();
            spt = line.split("\t");
            for (int i=1; i<spt.length; i++) {
                String word = spt[i].split(" ")[0];
                String score = spt[i].split(" ")[1];
                right.put(word, Double.valueOf(score));
            }
            ArrayList<HashMap<String, Double>> tmp = new ArrayList<>();
            tmp.add(left);
            tmp.add(right);
            vector.add(tmp);
        }

        LogInfo.logs("Vectors read into memory!");

        curr = 1; end = vector.size();
        LogInfo.begin_track("Begin calculating similarities and grouping...");
        int threads = 8;
        RvScorer4Name workThread = new RvScorer4Name();
        MultiThread multi = new MultiThread(threads, workThread);
        LogInfo.begin_track("%d threads are running...");
        multi.runMultiThread();
        LogInfo.end_track();

        LogInfo.logs("Graph construction complete, start writing");
        int num = 0;
        for (ArrayList<String> arr: graph) {
            bw.write(RvEqualSet.getName(String.valueOf(num)));
            for (String str: arr) {
                String[] spt = str.split("\t");
                bw.write("\t" + RvEqualSet.getName(spt[0]) + " " + spt[1]);
            }
            num ++;
            bw.write("\n");
        }
        LogInfo.logs("Written Complete!");
    }

    public static void main(String[] args) throws Exception {
        RvEqualSet.initializeFor3M(args[2]);
        multiThreadWorking(args[0], args[1]);
    }
}
