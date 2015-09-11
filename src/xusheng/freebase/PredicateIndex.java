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
    public static void scan(String inFile_1, String outFile_1, String outFile_2, String entFile) throws Exception {
        HashMap<String, Integer> preSet = new HashMap<>();
        HashMap<String, Integer> entSet = new HashMap<>();
        BufferedReader br_1 = new BufferedReader(new FileReader(inFile_1));

        BufferedWriter bw_p = new BufferedWriter(new FileWriter(outFile_1));
        BufferedWriter bw_f = new BufferedWriter(new FileWriter(outFile_2));
        BufferedWriter bw_e = new BufferedWriter(new FileWriter(entFile));

        String line = "";
        int cnt = 0, pcnt = 0, ecnt = 0;
        while ((line = br_1.readLine()) !=null) {
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);

            String[] spt = line.split("\t");
            String ent1 = getName(spt[0]);
            String ent2 = getName(spt[2]);
            String pred = getName(spt[1]);

            if (! ent1.startsWith("m.") || ! ent2.startsWith("m.")) continue;

            if (! entSet.containsKey(ent1)) {
                ecnt ++;
                entSet.put(ent1, ecnt);
                bw_e.write(ent1 + "\t" + ecnt + "\n");
            }
            if (! entSet.containsKey(ent2)) {
                ecnt ++;
                entSet.put(ent2, ecnt);
                bw_e.write(ent2 + "\t" + ecnt + "\n");
            }

            if (!pred.startsWith("freebase") && !pred.startsWith("base") && !pred.startsWith("common")
                    && !pred.startsWith("type") && !pred.startsWith("user") && !pred.startsWith("key")) {
                if (!preSet.containsKey(pred)) {
                    pcnt++;
                    preSet.put(pred, pcnt);
                    bw_p.write(pred + "\t" + pcnt + "\n");
                }
                bw_f.write(entSet.get(ent1) + "\t" + preSet.get(pred) + "\t" + entSet.get(ent2) + "\n");
            }
        }
        br_1.close();
        bw_p.close();
        bw_f.close();
        bw_e.close();
        LogInfo.logs("Predicates size: %d", preSet.size());
        LogInfo.logs("Entity-Idx size: %d", entSet.size());
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
        //EntityIndex.initFromMid2Idx(args[0]);
        scan(args[1], args[2], args[3], args[0]);
    }
}

