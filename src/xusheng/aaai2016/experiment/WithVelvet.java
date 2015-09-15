package xusheng.aaai2016.experiment;

import fig.basic.LogInfo;
import xusheng.nell.Belief;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Administrator on 2015/9/5.
 */
public class WithVelvet {

    public static void main(String[] args) throws Exception {
        //countRel(args[0], args[1]);
        process(args[0], args[2], args[3]);
    }

    public static void countRel(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashSet<String> set = new HashSet<>();
        String line = "";
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rel = spt[2];
            if (! set.contains(rel)) {
                bw.write(rel + "\n");
                set.add(rel);
            }
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
        }
        br.close();
        bw.close();
        LogInfo.logs("Total rel for %s: %d", inFile, set.size());
    }

    /**process the belief file,
     * output a result file consisting 52 smallest instances relations
     */
    public static void process(String inFile, String outFile, String typeFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw_o = new BufferedWriter(new FileWriter(outFile));
        PrintWriter pw = new PrintWriter(bw_o);
        BufferedWriter bw_t = new BufferedWriter(new FileWriter(typeFile));
        HashMap<String, HashSet<Belief>> contents = new HashMap<>();
        HashMap<String, Integer> count = new HashMap<>();
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            Belief belief = new Belief(line);
            if (!belief.isConcept) continue;
            if (belief.isType) {
                bw_t.write(belief.toString() + "\n");
                continue;
            }
            if (! contents.containsKey(belief.relation)) contents.put(belief.relation, new HashSet<>());
            contents.get(belief.relation).add(belief);
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
        }
        LogInfo.logs("Total Size: %d", contents.size());
        br.close();
        bw_t.close();
        for (Map.Entry<String, HashSet<Belief>> entry : contents.entrySet())
            count.put(entry.getKey(), entry.getValue().size());
        ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(count);
        LogInfo.logs("Sorted size: %d", sorted.size());
        for (int i=0; i<sorted.size(); i++) {
            String rel = sorted.get(i).getKey();
            HashSet<Belief> set = contents.get(rel);
            pw.format("###\t%s\t%d\t:\n", rel, set.size());
            for (Belief belief: set) pw.write(belief.toString() + "\n");
        }
        pw.close();
    }
}
