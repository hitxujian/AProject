package xusheng.freebase;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Administrator on 2015/5/2.
 * process fb.txt and generate left and right files
 */
public class FbTuples {

    // hashmap: mid-pairs ---> predicate id
    public static HashMap<String, HashSet<String>> tuples = new HashMap<>();
    // hashmap: idx ---> idx#pidx pairs
    public static HashMap<String, HashSet<String>> ltuples = new HashMap<>(), rtuples = new HashMap<>();
    public static boolean verbose = true;

    // return predicate id according mid pair
    public static HashSet<String> related(String mid1, String mid2) {
        if (mid1 == null || mid2 == null)
            return null;
        String str = mid1 + "&" + mid2;
        if (tuples.containsKey(str))
            return tuples.get(str);
        return null;
    }

    public static void processFor2Hubs (String inFile, String loutFile, String routFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(loutFile));
        String line = ""; int num = 0, useless = 0;
        if (verbose) LogInfo.begin_track("Start to process fb.txt");
        while ((line=br.readLine()) != null) {
            num ++;
            if (num % 100000 ==0) LogInfo.logs("%d lines processed...", num);

            String[] spt = line.split("\t");
            String lidx = EntityIndex.getIdx(spt[0]);
            String ridx = EntityIndex.getIdx(spt[2]);
            String pidx = PredicateIndex.getIdx(spt[1]);

            if (lidx == null || ridx == null || pidx == null) {
                useless ++;
                continue;
            }

            if (!ltuples.containsKey(lidx))
                ltuples.put(lidx, new HashSet<String>());
            String rpair = ridx + "#" + pidx;
            ltuples.get(lidx).add(rpair);

            if (!rtuples.containsKey(ridx))
                rtuples.put(ridx, new HashSet<String>());
            String lpair = lidx + "#" + pidx;
            rtuples.get(ridx).add(lpair);
        }
        LogInfo.logs("%d lines are not used", useless);
        LogInfo.end_track();

        LogInfo.logs("Start to write left sides...");
        br.close();
        for (Map.Entry<String, HashSet<String>> entry: ltuples.entrySet()) {
            bw.write(entry.getKey() + "\t");
            for (String str: entry.getValue()) {
                bw.write(str + " ");
            }
            bw.write("\n");
        }
        bw.close();
        LogInfo.logs("Start to write right sides...");
        bw = new BufferedWriter(new FileWriter(routFile));
        for (Map.Entry<String, HashSet<String>> entry: rtuples.entrySet()) {
            bw.write(entry.getKey() + "\t");
            for (String str: entry.getValue()) {
                bw.write(str + " ");
            }
            bw.write("\n");
        }
        LogInfo.logs("Write complete!");
        bw.close();
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        int num = 0;
        if (verbose) LogInfo.begin_track("Start to read FbTuples");
        while ((line=br.readLine()) != null) {
            num ++;
            if (num < 150000000) continue;
            if (num % 100000 == 0) LogInfo.logs("%d lines read into memory...", num);
            //if (num % 150000000 == 0)
              //  break;
            String[] spt = line.split("\t");
            String str = spt[0] + "&" + spt[2];
            String idx = PredicateIndex.getIdx(spt[1]);
            if (tuples.containsKey(str)) {
                tuples.get(str).add(idx);
            } else {
                HashSet<String> tmp = new HashSet<>();
                tmp.add(idx);
                tuples.put(str, tmp);
            }
        }
        br.close();
        if (verbose) LogInfo.end_track();
        if (verbose) LogInfo.logs("Freebase tuples read into memory! size: %d", tuples.size());
    }

    public static void main(String[] args) throws Exception {
        PredicateIndex.initialize(args[3]);
        EntityIndex.initialize(args[4]);
        processFor2Hubs(args[0], args[1], args[2]);
    }

}
