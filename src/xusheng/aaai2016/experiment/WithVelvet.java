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
        //process(args[0], args[2], args[3]);
        /*for (int i = 40; i < 100; i++) {
            int j = i + 52;
            choose52_829_40(args[0], args[1], args[2], i, j);
        }*/
        get50_102(args[0], args[1]);
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
        ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(count, true);
        LogInfo.logs("Sorted size: %d", sorted.size());
        for (int i=0; i<sorted.size(); i++) {
            String rel = sorted.get(i).getKey();
            HashSet<Belief> set = contents.get(rel);
            pw.format("###\t%s\t%d\t:\n", rel, set.size());
            for (Belief belief: set) pw.write(belief.toString() + "\n");
        }
        pw.close();
    }

    public static void choose52_829_40(String inFile, String typeFile, String outFile, int st, int ed) throws Exception {
        LogInfo.logs(st + "\t" + ed + "\n");
        BufferedReader br_t = new BufferedReader(new FileReader(typeFile));
        String line;
        HashMap<String, HashSet<String>> typeMap = new HashMap<>();
        while ((line = br_t.readLine()) != null) {
            String[] spt = line.split("\t");
            String type = spt[0].replaceAll("_+", " ");
            if (! typeMap.containsKey(type)) typeMap.put(type, new HashSet<>());
            typeMap.get(type).add(spt[1]);
        }
        br_t.close();
        //LogInfo.logs("Entity Type size: %d", typeMap.size());

        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashSet<String> entSet = new HashSet<>(), typeSet = new HashSet<>();
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("###")) {
                //LogInfo.logs(line + "@@@@@" + cnt);
                cnt++;
                if (cnt < st) continue;
                if (cnt > ed) break;
                //LogInfo.logs(line + "@@@@@" + cnt);
                bw.write(line + "\n");
                continue;
            }
            if (cnt < st) continue;
            bw.write(line + "\n");
            String[] spt = line.split("\t");
            entSet.add(spt[0]);
            entSet.add(spt[1]);
            HashSet<String> types = null;
            if (typeMap.containsKey(spt[0])) {
                types = typeMap.get(spt[0]);
                for (String type : types) typeSet.add(type);
            }
            if (typeMap.containsKey(spt[1])) {
                types = typeMap.get(spt[1]);
                for (String type : types) typeSet.add(type);
            }
        }
        bw.close();
        LogInfo.logs("Entity Size: %d\n", entSet.size());
        LogInfo.logs("Type Coverage: %d\n", typeSet.size());
    }

    public static void get50_102(String inFile, String path) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = null;
        int cnt = 0, num = 0;
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("###")) {
                cnt ++;
                if (cnt < 50) continue;
                if (cnt > 102) break;
                String absPath = String.format(path + "/%d", num);
                num ++;
                File dir = new File(absPath);
                try {
                    dir.mkdirs();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                bw = new BufferedWriter(new FileWriter(absPath + "/info.txt"));
                bw.write(line.split("\t")[1]);
                bw.close();
                bw = new BufferedWriter(new FileWriter(absPath + "/entity_pairs.txt"));
                continue;
            }
            if (cnt < 50) continue;
            bw.write(line + "\n");
        }
        bw.close();
    }
}
