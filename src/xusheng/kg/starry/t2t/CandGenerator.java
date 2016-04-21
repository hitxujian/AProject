package xusheng.kg.starry.t2t;

import fig.basic.LogInfo;
import xusheng.kg.baidubaike.BkEntityIdxReader;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Luo Xusheng on 4/21/16.
 * Usage: Generate Baike candidates for triples in zh-freebase
 */

public class CandGenerator implements Runnable {
    public static String root = "/home/xusheng/starry";
    public static String fbPath = root + "/freebase", bkPath = root + "/baidubaike";
    public static int curr = -1, end = -1;
    public static BufferedWriter bw = null;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                fuzzyMatch(idx);
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

    public static void fuzzyMatch(int idx) {
        String fbTriple = taskList[idx];

    }

    public static void multiThreadWork() throws Exception {
        readFbData();
        readBkData();
        bw = new BufferedWriter(new FileWriter(root + "/candidates.triple"));
        curr = 1;
        LogInfo.logs("Begin to Generate Candidates for zh-Freebase from " +
                "%d to %d...", curr, end);
        int numOfThreads = 8;
        CandGenerator workThread = new CandGenerator();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    //--------------------------------------------------------------------------------

    public static Map<String, String> mid2Name = new HashMap<>();
    public static String[] taskList = new String[600000];

    public static void readFbData() throws IOException {
        // read freebase mid => name map
        BufferedReader br = new BufferedReader(new FileReader(fbPath + "/entity-name.zh"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            mid2Name.put(spt[0], spt[1]);
        }
        br.close();
        LogInfo.logs("Freebase Entity-Name Read, Size: %d", mid2Name.size());
        // read freebase Chinese triples as taskList
        br = new BufferedReader(new FileReader(fbPath + "/fb-zh.mid.triple"));
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            taskList[cnt] = line;
        }
        end = cnt + 1;
        LogInfo.logs("Freebase Chinese Triples Read, Size: %d", cnt);
    }

    // -------------------------------------------------------------------------------

    public static Map<Integer, String> BkId2Name = new HashMap<>();

    public static void readBkData() throws IOException {
        BkEntityIdxReader.initializeFromName2Idx();
        BufferedReader br = new BufferedReader(new FileReader(bkPath + "/entity.name"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            BkId2Name.put(Integer.parseInt(spt[0]), spt[1]);
        }
        LogInfo.logs("BaiduBaike Index-Name Read, Size: %d", BkId2Name.size());
    }


    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
