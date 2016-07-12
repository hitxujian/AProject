package xusheng.kg.starry.t2t;

import fig.basic.LogInfo;
import xusheng.kg.baike.BkEntIdxReader;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luo Xusheng on 4/21/16.
 * Usage: Generate Baike candidates for triples in zh-freebase
 */

public class CandGenerator implements Runnable {
    public static String root = "/home/xusheng/starry";
    public static String fbPath = root + "/freebase", bkPath = root + "/baike";
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

    public static boolean isChinese(char c) {
        return c >= 0x4E00 &&  c <= 0x9FA5;
    }

    public static boolean isChinese(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c)) return true;
        }
        return false;
    }

    public static boolean match(String a, String b) {
        Set<String> setA = new HashSet<>();
        Set<String> setB = new HashSet<>();
        for (int i=0; i<a.length(); i++) {
            if (a.charAt(i) == '(') break;
            if (a.charAt(i) != ' ')
                setA.add(String.valueOf(a.charAt(i)));
        }
        //LogInfo.logs(setA.toString());

        for (int i=0; i<b.length(); i++)
            if (b.charAt(i) != ' ')
                setB.add(String.valueOf(b.charAt(i)));
        //LogInfo.logs(setB.toString());

        double intersect = 0.0;
        for (String ch : setA)
            if (setB.contains(ch)) intersect += 1;
        double perA = intersect / setA.size();
        double perB = intersect / setB.size();
        if (perA >= 0.5 && perB >= 0.5 && (perA + perB) >= 1.3)
            return true;
        else
            return false;
    }

    public static void fuzzyMatch(int idx) {
        String fbTriple = taskList[idx];
        String[] spt = fbTriple.split("\t");
        if (!isChinese(spt[0]) || !isChinese(spt[2]))
            return;
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
        BkEntIdxReader.initializeFromName2Idx();
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
