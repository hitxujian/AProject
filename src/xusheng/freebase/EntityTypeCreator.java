package xusheng.freebase;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Xusheng
 * @author 9/10/2015
 */
public class EntityTypeCreator {
    public static HashMap<String, ArrayList<String>> typeToIdx = new HashMap<>();
    public static HashMap<String, ArrayList<String>> idxToType = new HashMap<>();
    public static boolean verbose = true;

    public static ArrayList<String> getType(String idx) {
        if (idxToType.containsKey(idx))
            return idxToType.get(idx);
        else
            return null;
    }

    public static ArrayList<String> getIdx(String type) {
        if (typeToIdx.containsKey(type))
            return typeToIdx.get(type);
        else
            return null;
    }

    public static String getName(String str) {
        String[] spt = str.split("/");
        String raw = spt[spt.length-1];
        int len = raw.length();
        String ret = raw.substring(0, len - 1);
        return ret;
    }

    public static void scan(String inFile, String outFile_1, String outFile_2) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw_t = new BufferedWriter(new FileWriter(outFile_1));
        BufferedWriter bw_et = new BufferedWriter(new FileWriter(outFile_2));
        HashMap<String, Integer> typeSet = new HashMap<>();
        String line = ""; int cnt = 0, typeIdx = 0;
        String preEnt = "";
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String entity = getName(spt[0]);
            if (! entity.startsWith("m.")) continue;
            String type = getName(spt[2]);
            if (type.startsWith("freebase") || type.startsWith("base") || type.startsWith("common")
                    || type.startsWith("user") || type.startsWith("key")) continue;
            if (! typeSet.containsKey(type)) {
                typeIdx ++;
                bw_t.write(type + "\t" + typeIdx + "\n");
                typeSet.put(type, typeIdx);
            }
            if (entity.equals(preEnt)) bw_et.write("\t" + typeSet.get(type));
            else {
                bw_et.write("\n" + entity + "\t" + typeSet.get(type));
                preEnt = entity;
            }
            cnt ++;
            if (cnt % 100000 == 0) LogUpgrader.showLine(cnt ,100000);
        }
        br.close();
        bw_t.close();
        bw_et.close();
        LogInfo.logs("Freebase Type-Index constructed.");
        LogInfo.logs("Freebase Entity-Type Info constructed.");
    }

    public static void ChangeFromMid2Idx(String inFile, String outFile) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (EntityIndex.getIdx(spt[0]) == null) continue;
            bw.write(EntityIndex.getIdx(spt[0]));
            for (int i=1; i<spt.length; i++) bw.write("\t" + spt[i]);
            bw.write("\n");
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void generateTEfile(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashMap<String, StringBuffer> map = new HashMap<>();
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            for (int i=1; i<spt.length; i++) {
                if (! map.containsKey(spt[i])) map.put(spt[i], new StringBuffer());
                map.get(spt[i]).append("\t" + spt[0]);
            }
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
        }
        br.close();
        for (Map.Entry<String, StringBuffer> entry: map.entrySet()) {
            bw.write(entry.getKey() + entry.getValue() + "\n");
        }
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void initializeTE(String file_1, String file_2) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file_1));
        String line = "";
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            ArrayList<String> tmp = new ArrayList<>();
            for (int i=1; i<spt.length; i++) tmp.add(spt[i]);
            typeToIdx.put(spt[0], tmp);
        }
        br.close();
        br = new BufferedReader(new FileReader(file_2));
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            ArrayList<String> tmp = new ArrayList<>();
            for (int i=1; i<spt.length; i++) tmp.add(spt[i]);
            idxToType.put(spt[0], tmp);
        }
        br.close();
        if (verbose) LogInfo.logs("Freebase Type-Entity read into memory!");
    }

    public static void main(String[] args) throws Exception {
        //initialize(args[0], args[1]);
        //scan(args[0], args[1], args[2]);
        EntityIndex.initFromMid2Idx(args[5]);
        ChangeFromMid2Idx(args[2], args[3]);
        EntityIndex.clear();
        generateTEfile(args[3], args[4]);
    }
}
