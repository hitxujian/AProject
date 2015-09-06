package xusheng.wikipedia.EP;

import fig.basic.LogInfo;
import xusheng.freebase.EntityIndex;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/6/13.
 */
public class PostProcessor {


    public static void changeForm(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            LogUpgrader.showLine(num, 50000);
            String[] spt = line.split("\t");
            if (spt.length < 5) continue;
            String subj = EntityTitle.getEntity(spt[1]),
                    obj = EntityTitle.getEntity(spt[4]);
            if (subj != null && obj != null) {
                String rel = spt[2].substring(0, spt[2].length()-1);
                String[] tmp = rel.split(" ");
                if (tmp.length < 6)
                    bw.write(spt[0] + "\t" + rel + "\t" + spt[3] + "\t" + subj + "\t" + obj + "\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("work done!");
    }

    public static void groupRel(String inFile_1, String inFile_2, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile_1));
        HashMap<String, ArrayList<Integer>> groups = new HashMap<>();
        HashMap<String, Integer> groupCnt = new HashMap<>();
        String line = "";
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt++;
            if (line.length() > 1) {
                if (!groups.containsKey(line)) {
                    groups.put(line, new ArrayList<Integer>());
                    groupCnt.put(line, 0);
                }
                groups.get(line).add(cnt);
                int tmp = groupCnt.get(line) + 1;
                groupCnt.put(line, tmp);
            }
        }
        br.close();
        ArrayList<Map.Entry<String, Integer>> sorted = new MapHelper<>().sort(groupCnt);
        ArrayList<String> tuples = new ArrayList<>();
        tuples.add(" ");
        br = new BufferedReader(new FileReader(inFile_2));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        while ((line = br.readLine()) != null) tuples.add(line);
        for (Map.Entry<String, Integer> entry: sorted) {
            bw.write("###\t" + entry.getKey() + "\t" + entry.getValue() + ":\n");
            ArrayList<Integer> list = groups.get(entry.getKey());
            for (Integer idx: list) bw.write(tuples.get(idx) + "\n");
        }
        br.close();
        bw.close();
    }

    public static void ChangeToIndex(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("###")) bw.write(line + "\n");
            String[] spt = line.split("\t");
            String subj = EntityIndex.getIdx(spt[3]);
            String obj = EntityIndex.getIdx(spt[4]);
            if (subj != null && obj != null)
                bw.write(subj + "\t" + obj + "\n");
            cnt ++;
            if (cnt % 100 == 0) LogUpgrader.showLine(cnt, 100);
        }
        br.close();
        bw.close();
    }

    public static void main(String[] args) throws Exception {
        //EntityTitle.initialize(args[0]);
        //changeForm(args[1], args[2]);

        // group relation-tuples by increasing order
        // groupRel(args[3], args[4], args[5]);

        // change mid to new-index
        EntityIndex.initialize(args[7]);
        ChangeToIndex(args[5], args[6]);
    }
}
