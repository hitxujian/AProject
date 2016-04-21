package xusheng.kg.starry.t2t;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by Luo Xusheng on 4/21/16.
 * Usage: Generate Baike candidates for triples in zh-freebase
 */

public class CandGenerator implements Runnable {
    public static String root = "/home/xusheng/starry";
    public static int curr = -1, end = -1;
    public static BufferedWriter bw = null;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                fuzzyMatch();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr++;
            return ret;
        }
        return -1;
    }

    public static void fuzzyMatch() {

    }

    public static void multiThreadWork() throws Exception {
        bw = new BufferedWriter(new FileWriter(root + "/candidates.triple"));
        curr = 1;
        end = 300;
        LogInfo.logs("Begin to Construct Article Idx and Extract Infobox...");
        int numOfThreads = 8;
        CandGenerator workThread = new CandGenerator();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
