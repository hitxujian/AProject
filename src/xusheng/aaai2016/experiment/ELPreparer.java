package xusheng.aaai2016.experiment;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.freebase.EntityIndex;
import xusheng.nell.Belief;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;
import xusheng.util.struct.Triple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Administrator on 2015/9/15.
 */
public class ELPreparer {

    public static void main(String[] args) throws Exception {
        changeFormat2(args[0], args[1]);
        generateIdxFile(args[2], args[1], args[3]);
        //generate2Files(args[1], args[2], args[3], args[4]);
        fromIdx2Mid(args[3], args[4]);
    }

    public static String removeUnderline(String entity) {
        return entity.replaceAll("_+", " ");
    }

    public static void fromIdx2Mid(String inFile, String outFile) throws Exception {
        EntityIndex.initialize_old("/home/xusheng/old_data/entity_index.txt");
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("###")) {
                bw.write(line + "\n");
                continue;
            }
            String[] spt = line.split("\t");
            bw.write(EntityIndex.getMid(spt[0]) + "\t" + EntityIndex.getMid(spt[1]) + "\n");
        }
        br.close();
        bw.close();
    }

    public static void changeFormat(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line, relation = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
            if (line.startsWith("###")) {
                relation = line.split("\t")[1];
                continue;
            }
            String spt[] = line.split("\t");
            bw.write(removeUnderline(spt[0]) + "\t" + relation + "\t" + removeUnderline(spt[1]) + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job Done.");
    }

    public static void changeFormat2(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashMap<String, HashSet<Triple<String, String, Integer>>> contents = new HashMap<>();
        HashMap<String, Integer> count = new HashMap<>();
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            String[] spt = line.split("\t");
            String rel = spt[1];
            if (! contents.containsKey(rel)) contents.put(rel, new HashSet<>());
            contents.get(rel).add(new Triple<>(spt[0], spt[2], cnt));
        }
        br.close();
        LogInfo.logs("Total Size: %d", contents.size());

        for (Map.Entry<String, HashSet<Triple<String, String, Integer>>> entry : contents.entrySet())
            count.put(entry.getKey(), entry.getValue().size());
        ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(count, true);
        LogInfo.logs("Sorted size: %d", sorted.size());

        for (int i=0; i<sorted.size(); i++) {
            String rel = sorted.get(i).getKey();
            HashSet<Triple<String, String, Integer>> set = contents.get(rel);
            String format = String.format("###\t%s\t%d\t:\n", rel, set.size());
            bw.write(format);
            for (Triple<String, String, Integer> triple: set)
                bw.write(triple.getFirst() + "\t" + triple.getSecond() + "\t" + triple.getThird() + "\n");
        }
        bw.close();
        LogInfo.logs("Job Done.");
    }

    public static void generateIdxFile(String idxFile, String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(idxFile));
        HashMap<Integer, String> mp = new HashMap<>();
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
            String[] spt = line.split("\t");
            String ret = spt[0] + "\t" + spt[2];
            mp.put(cnt, ret);
        }
        br.close();

        br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String relation = ""; cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
            if (line.startsWith("###")) {
                bw.write(line + "\n");
                continue;
            }
            String spt[] = line.split("\t");
            int lineNum = Integer.parseInt(spt[2]);
            bw.write(mp.get(lineNum) + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job Done.");
    }

    public static void generate2Files(String inFile, String linkingFile, String nameFile, String idxFile) throws Exception {
        BufferedReader br_i = new BufferedReader(new FileReader(inFile));
        BufferedReader br_l = new BufferedReader(new FileReader(linkingFile));
        BufferedWriter bw_n = new BufferedWriter(new FileWriter(nameFile));
        BufferedWriter bw_i = new BufferedWriter(new FileWriter(idxFile));
        HashMap<Integer, Pair<String, String>> mp = new HashMap<>();
        String line = "";
        while ((line = br_l.readLine()) != null) {
            String[] spt = line.split("\t");
            String ent1 = spt[1].split(" ")[0];
            String ent2 = spt[2].split(" ")[0];
            int num = Integer.parseInt(spt[0]);
            Pair<String, String> pair = new Pair<>(ent1, ent2);
            mp.put(num, pair);
        }
        LogInfo.logs("Entity Linking File Read Done...");
        br_l.close();

        int cnt = 0;
        while ((line = br_i.readLine()) != null) {
            cnt ++;
            String[] spt = line.split("\t");
            String rel = spt[1];
            if (mp.containsKey(cnt)) {
                bw_n.write(line + "\n");
                bw_i.write(mp.get(cnt).getFirst() + "\t" + rel + "\t" + mp.get(cnt).getSecond() + "\n");
            }
        }
        br_i.close();
        bw_i.close();
        bw_n.close();
        LogInfo.logs("Job Done.");
    }
}
