package xusheng.freebase.liteVersion;

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
import java.util.HashSet;
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

    public static void newIdx(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        LogInfo.logs("Changing Idx " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 1000000 == 0) LogUpgrader.showLine(cnt, 1000000);
            String[] spt = line.split("\t");
            bw.write(spt[1] + "\t" + cnt + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static String tarDirNew = "/home/xusheng/data_0911/lite-newIdx";
    public static String fbDir = "/home/data/freebase";
    public static void main(String[] args) throws Exception {
        //countPopularity();
        //Top5mIndices.initialize();
        /*filterProp(oriDir + "/prop-final-sorted.aaai", tarDir + "/prop-final-sorted.aaai");
        filterEntityIndex(oriDir + "/entity_index.aaai", tarDir + "/entity_index.aaai");
        filterEntityType(oriDir + "/entity_type.aaai", tarDir + "/entity_type.aaai");
        filterTypeEntity(oriDir + "/type_entity.aaai", tarDir + "/type_entity.aaai");
        filterProp(oriDir + "/prop.aaai", tarDir + "/prop.aaai");*/
        //newIdx(tarDir + "/entity_index.aaai", tarDirNew + "/idx-changer.txt");
        //Top5mIndices.initForIdxChange();
        /*IdxProp(tarDir + "/prop-final-sorted.aaai", tarDirNew + "/prop-final-sorted.aaai");
        IdxEntityIndex(tarDir + "/entity_index.aaai", tarDirNew + "/entity_index.aaai");
        IdxEntityType(tarDir + "/entity_type.aaai", tarDirNew + "/entity_type.aaai");
        IdxTypeEntity(tarDir + "/type_entity.aaai", tarDirNew + "/type_entity.aaai");
        IdxProp(tarDir + "/prop.aaai", tarDirNew + "/prop.aaai");
        IdxTop(tarDir + "/top5m.txt", tarDirNew + "/top5m.txt");*/
        //filterName(fbDir + "/freebase_idmatch", tarDirNew + "/entWithName.txt");
        //splitEntity(oriDir + "/entWithName.txt", oriDir + "/entity_index.aaai", oriDir + "/withName.txt", oriDir + "/withourName.txt");
        EntityIndex.initFromIdx2Mid(oriDir + "/entity_index.aaai");
        changeId2Mid(oriDir + "/top5m.idx", oriDir + "/top5m.mid");
    }

    public static void changeId2Mid(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            bw.write(EntityIndex.getMid(spt[0]) + "\t" + spt[1] + "\n");
        }
        bw.close();
        br.close();
    }

    public static void splitEntity(String inFile1, String inFile2, String outFile1, String outFile2) throws Exception {
        BufferedReader br1 = new BufferedReader(new FileReader(inFile1));
        BufferedReader br2 = new BufferedReader(new FileReader(inFile2));
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(outFile1));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(outFile2));
        HashSet<String> set = new HashSet<>();
        String line; int cnt = 0;
        while ((line = br1.readLine()) != null) {
            cnt++;
            if (cnt % 1000000 == 0) LogUpgrader.showLine(cnt, 1000000);
            set.add(line);
        }
        LogInfo.logs("Name read.");

        while ((line = br2.readLine()) != null) {
            String[] spt = line.split("\t");
            if (set.contains(spt[0])) bw1.write(spt[0] + "\n");
            else bw2.write(spt[0] + "\n");
        }

        br1.close();
        br2.close();
        bw1.close();
        bw2.close();
        LogInfo.logs("Job done.");
    }

    public static String getName(String str) {
        String[] spt = str.split("/");
        String raw = spt[spt.length-1];
        int len = raw.length();
        String ret = raw.substring(0, len - 1);
        return ret;
    }

    public static void filterName(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashSet<String> set = new HashSet<>();
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 1000000 == 0) LogUpgrader.showLine(cnt, 1000000);
            String[] spt = line.split("\t");
            String ent = getName(spt[0]);
            if (ent.startsWith("m.")) set.add(ent);
        }
        for (String name : set) bw.write(name + "\n");
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void IdxTop(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        LogInfo.logs("Indexing " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 1000000 == 0) LogUpgrader.showLine(cnt, 1000000);
            String[] spt = line.split("\t");
            bw.write(Top5mIndices.getNewIdx(spt[0]) + "\t" + spt[1] + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void IdxProp(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        LogInfo.logs("Indexing " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            bw.write(Top5mIndices.getNewIdx(spt[0]) + "\t" + spt[1] + "\t" + Top5mIndices.getNewIdx(spt[2]) + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void IdxEntityIndex(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        int cnt = 0;
        LogInfo.logs("Indexing " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            bw.write(spt[0] + "\t" + Top5mIndices.getNewIdx(spt[1]) + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void IdxEntityType(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        LogInfo.logs("Indexing " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            bw.write(Top5mIndices.getNewIdx(spt[0]));
            for (int i=1; i<spt.length; i++)
                bw.write("\t" + spt[i]);
            bw.write("\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void IdxTypeEntity(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        LogInfo.logs("Indexing " + outFile + "...");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 1000 == 0) LogUpgrader.showLine(cnt, 1000);
            String[] spt = line.split("\t");
            StringBuffer sb = new StringBuffer();
            for (int i=1; i<spt.length; i++) {
                sb.append("\t" + Top5mIndices.getNewIdx(spt[i]));
            }
            bw.write(spt[0] + sb + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }


}
