package xusheng.freebase.liteVersion;

import fig.basic.LogInfo;
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
 * Created by Xusheng on 10/7/15.
 */
public class Filter {

    public static HashMap<String, Integer> cntList = new HashMap<>();
    public static String oriDir = "/home/xusheng/data_0911";
    public static String tarDir = "/home/xusheng/data_0911/lite";

    public static void countPopularity() throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(oriDir + "/prop.aaai"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(tarDir + "/top5m.txt"));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            if (!cntList.containsKey(spt[0])) cntList.put(spt[0], 1);
            else {
                int tmp = cntList.get(spt[0]) + 1;
                cntList.put(spt[0], tmp);
            }
            if (!cntList.containsKey(spt[2])) cntList.put(spt[2], 1);
            else {
                int tmp = cntList.get(spt[2]) + 1;
                cntList.put(spt[2], tmp);
            }
        }
        br.close();
        LogInfo.logs("Finish counting, start to sort and write...");
        ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(cntList);
        cnt = 0;
        for (Map.Entry<String, Integer> entry : sorted) {
            cnt ++;
            if (cnt > 5000000) break;
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void filterProp(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        LogInfo.logs("Filtering " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            if (Top5mIndices.top5m.contains(spt[0]) && Top5mIndices.top5m.contains(spt[2]))
                bw.write(line + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void filterEntityIndex(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        int cnt = 0;
        LogInfo.logs("Filtering " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            if (Top5mIndices.top5m.contains(spt[1]))
                bw.write(line + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void filterEntityType(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        LogInfo.logs("Filtering " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            if (Top5mIndices.top5m.contains(spt[0]))
                bw.write(line + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void filterTypeEntity(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        LogInfo.logs("Filtering " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 1000 == 0) LogUpgrader.showLine(cnt, 1000);
            String[] spt = line.split("\t");
            StringBuffer sb = new StringBuffer();
            for (int i=1; i<spt.length; i++) {
                if (Top5mIndices.top5m.contains(spt[i])) sb.append("\t" + spt[i]);
            }
            if (sb.length() != 0) bw.write(spt[0] + sb + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void main(String[] args) throws Exception {
        //countPopularity();
        Top5mIndices.initialize();
        /*filterProp(oriDir + "/prop-final-sorted.aaai", tarDir + "/prop-final-sorted.aaai");
        filterEntityIndex(oriDir + "/entity_index.aaai", tarDir + "/entity_index.aaai");
        filterEntityType(oriDir + "/entity_type.aaai", tarDir + "/entity_type.aaai");
        filterTypeEntity(oriDir + "/type_entity.aaai", tarDir + "/type_entity.aaai");*/
        filterProp(oriDir + "/prop.aaai", tarDir + "/prop.aaai");
    }
}
