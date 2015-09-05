package xusheng.freebase;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/5/2.
 */
public class PredicateIndex {

    public static HashMap<String, String> preToIdx = new HashMap<>();
    public static HashMap<String, String> idxToPre = new HashMap<>();
    public static boolean verbose = true;

    public static String getPre(String idx) {
        if (idxToPre.containsKey(idx))
            return idxToPre.get(idx);
        else
            return null;
    }

    public static String getIdx(String pre) {
        if (preToIdx.containsKey(pre))
            return preToIdx.get(pre);
        else
            return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            preToIdx.put(spt[1], spt[0]);
            idxToPre.put(spt[0], spt[1]);
        }
        br.close();
        if (verbose) LogInfo.logs("Freebase Predicate-Index read into memory!");
    }

    public static String getName(String str) {
        String[] spt = str.split("/");
        String raw = spt[spt.length-1];
        int len = raw.length();
        String ret = raw.substring(0, len - 1);
        return ret;
    }

    // 8.23 whole new scan from the original freebase data
    public static void scan(String inFile_1, String inFile_2, String outFile_1, String outFile_2) throws Exception {
        BufferedReader br_2 = new BufferedReader(new FileReader(inFile_2));
        HashMap<String, Integer> preset = new HashMap<>();
        String line = "";
        while ((line = br_2.readLine()) != null) {
            String[] spt = line.split("\t");
            preset.put(spt[0], Integer.parseInt(spt[1]));
        }
        br_2.close();
        LogInfo.logs("Part of predicate set read into menmory! size: %d", preset.size());

        BufferedReader br_1 = new BufferedReader(new FileReader(inFile_1));
        BufferedWriter bw_1 = new BufferedWriter(new FileWriter(outFile_1));
        BufferedWriter bw_2 = new BufferedWriter(new FileWriter(outFile_2));


        line = "";
        int cnt = 0, pcnt = 2498;//pcnt = 0;
        while ((line = br_1.readLine()) !=null) {
            cnt ++;
            if (cnt < 1020000000) continue;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            String ent1 = getName(spt[0]);
            int index;
            if (EntityIndex.getIdx(ent1) != null) index = Integer.parseInt(EntityIndex.getIdx(ent1));
            else continue;
            if (index < 20395306) continue;
            String ent2 = getName(spt[2]);
            String pred = getName(spt[1]);
            if (EntityIndex.getIdx(ent1) != null && EntityIndex.getIdx(ent2) != null)
                if (!pred.startsWith("freebase") && !pred.startsWith("base") && !pred.startsWith("common")
                        && !pred.startsWith("type") && !pred.startsWith("user") && !pred.startsWith("key")) {
                    if (!preset.containsKey(pred)) {
                        pcnt++;
                        preset.put(pred, pcnt);
                        bw_1.write(pred + "\t" + pcnt + "\n");
                    }
                    bw_2.write(EntityIndex.getIdx(ent1) + "\t" + preset.get(pred) + "\t" + EntityIndex.getIdx(ent2) + "\n");
                }
        }
        br_1.close();
        LogInfo.logs("Predicates size: %d", preset.size());
        bw_1.close();
        bw_2.close();
    }

    public static void construct(String infile, String outfile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(infile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        HashMap<String, Integer> index = new HashMap<>();
        String line = ""; int num = 0, lnum = 0;
        while ((line=br.readLine()) != null) {
            lnum ++;
            if (lnum % 100000 == 0) LogInfo.logs("%d lines processed...", lnum);
            String[] spt = line.split("\t");
            if (!index.containsKey(spt[1])) {
                num++;
                index.put(spt[1], num);
                bw.write(num + "\t" + spt[1] + "\n");
            }
        }
        br.close();
    }

    public static void main(String[] args) throws Exception {
        //construct(args[0], args[1]);
        EntityIndex.initialize(args[0]);
        scan(args[1], args[4], args[2], args[3]);
    }
}

