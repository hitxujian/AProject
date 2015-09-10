package xusheng.freebase;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Provide transformation between index and mid of Freebase Entity
 * @author Xusheng
 * @version 1.0
 */
public class EntityIndex {
    public static HashMap<String, String> midToIdx = new HashMap<>();
    public static HashMap<String, String> idxToMid = new HashMap<>();
    public static boolean verbose = true;

    public static String getMid(String idx) {
        if (idx.equals("-10"))
            return "datetime";
        if (idxToMid.containsKey(idx))
            return idxToMid.get(idx);
        else
            return null;
    }

    public static String getIdx(String mid) {
        if (midToIdx.containsKey(mid))
            return midToIdx.get(mid);
        else
            return null;
    }

    public static void initialize_old(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = ""; int idx = 0;
        if (verbose) LogInfo.begin_track("Start to read Old Fb Entity-Index");
        while ((line=br.readLine()) != null) {
            idx ++;
            if (idx % 100000 == 0) LogInfo.logs("%d lines read into memory...", idx);
            String[] spt = line.split("\t");
            midToIdx.put(spt[1], spt[0]);
            idxToMid.put(spt[0], spt[1]);
        }
        br.close();
        if (verbose) LogInfo.end_track();
        if (verbose) LogInfo.logs("Freebase Old Entity-Index read into memory!");
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = ""; int idx = 0;
        if (verbose) LogInfo.begin_track("Start to read Fb Entity-Index");
        while ((line=br.readLine()) != null) {
            idx ++;
            if (idx % 100000 == 0) LogInfo.logs("%d lines read into memory...", idx);
            String[] spt = line.split("\t");
            midToIdx.put(spt[0], spt[1]);
            idxToMid.put(spt[1], spt[0]);
        }
        br.close();
        if (verbose) LogInfo.end_track();
        if (verbose) LogInfo.logs("Freebase Entity-Index read into memory!");
    }

    public static void initFromMid2Idx(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = ""; int idx = 0;
        if (verbose) LogInfo.begin_track("Start to read Fb Entity-Index");
        while ((line=br.readLine()) != null) {
            idx ++;
            if (idx % 100000 == 0) LogInfo.logs("%d lines read into memory...", idx);
            String[] spt = line.split("\t");
            midToIdx.put(spt[0], spt[1]);
        }
        br.close();
        if (verbose) LogInfo.end_track();
        if (verbose) LogInfo.logs("Freebase Entity-Index(Mid to Idx) read into memory!");
    }

    public static void initFromIdx2Mid(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = ""; int idx = 0;
        if (verbose) LogInfo.begin_track("Start to read Fb Entity-Index");
        while ((line=br.readLine()) != null) {
            idx ++;
            if (idx % 100000 == 0) LogInfo.logs("%d lines read into memory...", idx);
            String[] spt = line.split("\t");
            idxToMid.put(spt[1], spt[0]);
        }
        br.close();
        if (verbose) LogInfo.end_track();
        if (verbose) LogInfo.logs("Freebase Entity-Index(Idx to Mid) read into memory!");
    }

    public static String getName(String str) {
        String[] spt = str.split("/");
        String raw = spt[spt.length-1];
        int len = raw.length();
        String ret = raw.substring(0, len - 1);
        return ret;
    }

    // 8.23 whole new scan for freebase entities
    public static void scan(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashSet<String> entset = new HashSet<>();
        String line = "";
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 100000 == 0) LogUpgrader.showLine(cnt, 100000);
            String[] spt = line.split("\t");
            String ent1 = getName(spt[0]);
            //String ent2 = getName(spt[2]);
            if (ent1.startsWith("m."))
                if (!entset.contains(ent1)) {
                    bw.write(ent1 + "\n");
                    entset.add(ent1);
                }
        }
        LogInfo.logs("Total size: %d", entset.size());
        br.close();
        bw.close();
    }

    public static void transformToMid(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 100000 == 0) LogUpgrader.showLine(cnt, 100000);
            String[] spt = line.split("\t");
            bw.write(spt[0]);
            for (int i=1; i<spt.length; i++) {
                String[] sptt = spt[i].split(" ");
                bw.write("\t" + getMid(sptt[0]) + " " + getMid(sptt[1]));
            }
            bw.write("\n");
        }
        br.close();
        bw.close();
    }

    public static void main(String[] args) throws Exception {
        scan(args[0], args[1]);
        //initialize_old(args[2]);
        //transformToMid(args[3], args[4]);
    }
}

