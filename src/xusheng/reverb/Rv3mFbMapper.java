package xusheng.reverb;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by Administrator on 2015/5/10.
 * Map 3m reverb instances to freebase predicate through 1 or 2 hubs.
 */
public class Rv3mFbMapper implements  Runnable{

    public static ArrayList<RvTuple> RvTuples = new ArrayList<>();
    public static HashMap<String, String> leftSet = new HashMap<>(), rightSet = new HashMap<>();
    public static BufferedWriter bw;
    public static boolean verbose = true;

    public static void read3MLinking(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        RvTuples.add(null); // idx start from 1
        while ((line=br.readLine()) != null) {
            RvTuple rt = new RvTuple(line);
            RvTuples.add(rt);
        }
        br.close();
        if (verbose) LogInfo.logs("ReVerb simple linking read into memory!");
    }

    /*public static void map1HubToFb(String outFile) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        for (Map.Entry<String, RvTuple> entry: RvTuples.entrySet()) {
            String lmid = EntityIndex.getMid(entry.getValue().arg1);
            String rmid = EntityIndex.getMid(entry.getValue().arg2);
            HashSet<String> fbMatchs = FbTuples.related(lmid, rmid);
            if (fbMatchs != null) {
                bw.write(entry.getKey());
                for (String str: fbMatchs)
                    bw.write("\t" + str);
                bw.write("\n");
            }
        }
        bw.close();
    }*/

    public static HashSet<String> get2HubMatch(String ll, String rr) {
        String[] left = ll.split(" "), right = rr.split(" ");
        HashMap<String, ArrayList<String>> lset = new HashMap<>(), rset = new HashMap<>();
        for (String str: left) {
            String[] spt = str.split("#");
            if (!lset.containsKey(spt[0])) lset.put(spt[0], new ArrayList<String>());
            lset.get(spt[0]).add(spt[1]);
        }
        for (String str: right) {
            String [] spt = str.split("#");
            if (!rset.containsKey(spt[0])) rset.put(spt[0], new ArrayList<String>());
            rset.get(spt[0]).add(spt[1]);
        }
        HashSet<String> ret = new HashSet<>();
        boolean flag = false;
        for (Map.Entry<String, ArrayList<String>> entry: lset.entrySet()) {
            if (rset.containsKey(entry.getKey())) {
                for (String lpre: entry.getValue())
                    for (String rpre: rset.get(entry.getKey())) {
                        ret.add(lpre + "#" + rpre);
                        flag = true;
                    }
            }
        }
        if (flag) return ret;
        else return null;
    }

    //-----------------------------------------------------------------------------------------//

    public static int curr = -1, end = -1;

    public void run() {
        while (true) {
            try {
                int cur = getCurr();
                if (cur == -1) return;
                RvTuple tuple = RvTuples.get(cur);
                if (leftSet.containsKey(tuple.arg1) && rightSet.containsKey(tuple.arg2)) {
                    String left = leftSet.get(tuple.arg1);
                    String right = rightSet.get(tuple.arg2);
                    HashSet<String> match = get2HubMatch(left, right);
                    if (match != null) {
                        StringBuffer ret = new StringBuffer();
                        ret.append(tuple.idx);
                        for (String str : match)
                            ret.append("\t" + str);
                        writeRes(ret);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public  static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            if (curr % 10000 == 0) LogInfo.logs("Current Idx: %d [%s]", curr, new Date().toString());
            return ret;
        }
        return -1;
    }

    public static synchronized void writeRes(StringBuffer line) throws Exception {
        bw.write(line.toString() + "\n");
    }

    public static void map2HubToFb(String lFile, String rFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(lFile));
        LogInfo.begin_track("Start to read left and right entities relations...");
        String line = ""; int num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            if (num % 100000 == 0) LogInfo.logs("left %d lines processed...", num);
            String[] spt = line.split("\t");
            leftSet.put(spt[0], spt[1].trim());
        }
        br.close();
        LogInfo.logs("Left part read into memory!");
        br = new BufferedReader(new FileReader(rFile));
        num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            if (num % 100000 == 0) LogInfo.logs("right %d lines processed...", num);
            String[] spt = line.split("\t");
            rightSet.put(spt[0], spt[1].trim());
        }
        br.close();
        LogInfo.logs("right part read into memory!");
        LogInfo.end_track();

        LogInfo.begin_track("Start to map 2 Hubs...");
        bw = new BufferedWriter(new FileWriter(outFile));
        curr = 1; end = RvTuples.size();
        int threads = 8;
        Rv3mFbMapper workThread = new Rv3mFbMapper();
        MultiThread multiThread = new MultiThread(threads, workThread);
        LogInfo.logs("%d threads are running...", threads);
        multiThread.runMultiThread();
        bw.close();
        LogInfo.end_track();
    }

    public static void getTogether(String file1, String file2, String file3, String file4) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file1));
        HashMap<String, HashSet<String>> set = new HashMap<>();
        String line = "";
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            String idx = spt[0];
            if (!set.containsKey(idx)) set.put(idx, new HashSet<String>());
            for (int i=1; i<spt.length; i++)
                set.get(idx).add(spt[i]);
        }
        br.close();
        br = new BufferedReader(new FileReader(file2));
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            String idx = spt[0];
            if (!set.containsKey(idx)) set.put(idx, new HashSet<String>());
            for (int i=1; i<spt.length; i++)
                set.get(idx).add(spt[i]);
        }
        br.close();
        br = new BufferedReader(new FileReader(file3));
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            String idx = spt[0];
            if (!set.containsKey(idx)) set.put(idx, new HashSet<String>());
            for (int i=1; i<spt.length; i++)
                set.get(idx).add(spt[i]);
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file4));
        for (Map.Entry<String, HashSet<String>> entry: set.entrySet()) {
            bw.write(entry.getKey());
            for (String str: entry.getValue())
                bw.write("\t" + str);
            bw.write("\n");
        }
        bw.close();
    }

    public static void main(String[] args) throws Exception {
        read3MLinking(args[0]);
        /*EntityIndex.initialize(args[2]);
        PredicateIndex.initialize(args[3]);
        FbTuples.initialize(args[1]);
        map1HubToFb(args[4]);*/
        map2HubToFb(args[9], args[10], args[11]);
        //getTogether(args[5], args[6], args[7], args[8]);
    }
}
