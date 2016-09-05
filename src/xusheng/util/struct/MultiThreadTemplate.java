package xusheng.util.struct;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by Xusheng on 9/5/2016.
 * A template for multi-thread programming
 */

public class MultiThreadTemplate implements Runnable{
    public static String rootFp = "";
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

    }

    public static BufferedReader br = null;
    public static void multiThreadWork() throws Exception{
        br = new BufferedReader(new FileReader(rootFp + "/infobox/KB_"));
        curr = 0; end = 0;
        int numOfThreads = 32;
        // todo: re-write the work thread
        MultiThreadTemplate workThread = new MultiThreadTemplate();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }
}