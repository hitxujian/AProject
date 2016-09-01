package xusheng.kg.baike.link;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        readTasks();
        curr = 0; end = 0;
        int numOfThreads = 32;
        CandiGenerator workThread = new CandiGenerator();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    public static void findCandidates(int idx) {
        String task = taskList.get(idx);
        String[] spt = task.split("\t");
        String target = spt[2];

    }

    public static List<String> taskList = null;
    public static void readTasks() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/KB_unlinked.tsv"));
        String line;
        taskList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            taskList.add(line);
        }
        LogInfo.logs("[info] %d Tasks loaded. [%s]", taskList.size(), new Date().toString());
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
