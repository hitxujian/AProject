package xusheng.experiment;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;

import java.io.*;
import java.util.*;

/**
 * @author Xusheng 9/8/2015
 *
 */

public class FileProcessor {

    public static void main(String[] args) throws Exception {
        String CMD = args[0];
        if (CMD.equals("Entity")) extractEntity(args[1], args[2], args[3]);
        if (CMD.equals("TYPE")) extractType(args[4], args[5]);
    }

    public static void extractEntity(String inFile, String newFile, String entFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw_n = new BufferedWriter(new FileWriter(newFile));
        BufferedWriter bw_e = new BufferedWriter(new FileWriter(entFile));
        HashSet<String> entSet = new HashSet<>();
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
            if (line.startsWith("###")) {
                bw_n.write(line + "\n");
                continue;
            }
            String[] spt = line.split("\t");
            String[] entity = spt[0].split(":");
            String ent = entity[entity.length-1].replace('_', ' ');
            if (! entSet.contains(ent)) {
                bw_e.write(ent + "\n");
                entSet.add(ent);
            }
            String[] relation = spt[1].split(":");
            String rel = relation[relation.length-1];
            String[] value = spt[2].split(":");
            String val = value[value.length-1].replace('_', ' ');
            bw_n.write(ent + "\t" + rel + "\t" + val + "\n");
        }
        LogInfo.logs("All processed.");
        br.close();
        bw_e.close();
        bw_n.close();
    }

    public static void extractType(String inFile, String typeFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(typeFile));
        Set<String> typeSet = new HashSet<>();
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
            if (line.startsWith("###")) {
                bw.write(line + "\n");
                continue;
            }
            String[] spt = line.split("\t")[2].split(":");
            String type = spt[spt.length-1];
            if (! typeSet.contains(type)) {
                bw.write(type + "\n");
                typeSet.add(type);
            }
        }
        LogInfo.logs("All processed.");
        br.close();
        bw.close();
    }
}
