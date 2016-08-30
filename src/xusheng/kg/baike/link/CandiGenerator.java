package xusheng.kg.baike.link;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

/**
 * Created by Xusheng on 8/29/2016.
 * Generate candidate entities to link for values in the infobox.
 * Method: fuzzy match.
 * Input: KB_linked.tsv & KB_unlinked.tsv
 * Output: KB_unlinked.candi.tsv
 */

public class CandiGenerator implements Runnable {
    public static String rootFp = "/home/xusheng/starry/hudongbaike/infobox";

    public static int curr = -1, end = -1;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                findCandidates(idx);
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

    public static void multiThreadWork() throws Exception{
        curr = 0; end = 0;
        int numOfThreads = 32;
        CandiGenerator workThread = new CandiGenerator();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    public static void findCandidates(int idx) {

    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
