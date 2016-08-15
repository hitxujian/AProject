package xusheng.kg.baike.relation.model;

import fig.basic.LogInfo;
import xusheng.util.nlp.ChWordSegmentor;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.List;

/**
 * Created by Xusheng on 8/1/2016.
 * Perform Chinese Word Segmentation using Stanford
 */

public class WordSegmenter implements Runnable{

    public static String rootFp = "/home/xusheng/starry/baidubaike";

    public void run() {
        while (true) {
            int idx = getCurr();
            if (idx == -1) return;
            String task = taskList[idx];
            try{
                List<String> str = ChWordSegmentor.segment(task);
                if (str == null) {
                    LogInfo.logs("NULL for [%s]", task);
                    return;
                }
                String ret = "";
                for (int i=0; i<str.size()-1; i++)
                    ret += str.get(i) + " | ";
                ret += str.get(str.size()-1) + "\n";
                writeRet(idx + "\t" + ret);
            } catch (Exception ex) {
                LogInfo.logs("Exception in [%s]", task);
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

    public static synchronized void writeRet(String str) throws IOException {
        bw.write(str);
    }

    public static void initialize() throws Exception {
        ChWordSegmentor.initialize();
    }

    public static BufferedWriter bw;
    public static void MultiThreadWork() throws Exception {
        initialize();
        readInput_v1();
        curr = 1;
        end = len;
        bw = new BufferedWriter(new FileWriter(rootFp + "/edge_dict.tsv.v1.seg"));
        LogInfo.logs("Begin to segment...");
        int numOfThreads = 32;
        WordSegmenter workThread = new WordSegmenter();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    // read edge_dict.tsv file as input.
    public static String[] taskList = null;
    public static int len = 186263;
    public static void readInput_v1() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/edge_dict.tsv.v1"));
        String line;
        taskList = new String[len+1];
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            taskList[Integer.parseInt(spt[0])] = spt[1];
        }
        LogInfo.logs("edge_dict.tsv.v1 read. Size: %d", taskList.length);
        br.close();
    }

    public static void main(String[] args) throws Exception {
        len = Integer.parseInt(args[0]);
        MultiThreadWork();
    }
}
