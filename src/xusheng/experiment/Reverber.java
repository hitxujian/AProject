package xusheng.experiment;

import fig.basic.LogInfo;
import xusheng.freebase.EntityIndex;
import xusheng.freebase.EntityType;
import xusheng.util.log.LogUpgrader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/9/16.
 */
public class Reverber {

    public static void main(String[] args) throws Exception {
        //generateInput(args[0], args[1], args[2]);
        //changeInputFormat(args[3], args[4]);
        //EntityIndex.initFromMid2Idx(fbDir + "/entity_index.aaai");
        //selectSubjTypeConsistency();
        EntityType.initialize(fbDir + "/entity_type.aaai");
        work();
    }

    public static void work() throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(rvDir + "/rel-suppSubj.idx"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(rvDir + "/rel-suppSubj-type.idx"));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 1000 == 0) LogUpgrader.showLine(cnt, 1000);
            String[] spt = line.split("\t");
            bw.write("###\t" + spt[0] + "\n");
            for (int i=1; i<spt.length; i++) {
                ArrayList<String> types = EntityType.getTypes(spt[i]);
                if (types == null) continue;
                bw.write("@" + spt[i] + "\t" + types.get(0));
                for (int j=1; j<types.size(); j++) bw.write("\t" + types.get(j));
                bw.write("\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static String rvDir = "/home/xusheng/reverb";
    public static String fbDir = "/home/xusheng/data_0911";
    public static void selectSubjTypeConsistency() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(rvDir + "/3m.tsv"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(rvDir + "/rel-suppSubj.mid"));
        String line = ""; int cnt = 0;
        ArrayList<String> subjMid = new ArrayList<>();
        subjMid.add("");
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 1000000 == 0) LogUpgrader.showLine(cnt, 1000000);
            String[] spt = line.split("\t");
            subjMid.add(EntityIndex.getIdx("m." + spt[3]));
        }
        br.close();
        LogInfo.logs("subj read.");

        br = new BufferedReader(new FileReader(rvDir + "/3m-relation-supports.txt"));
        cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (spt.length <= 50) continue;
            cnt ++;
            bw.write(spt[0] + "\t");
            for (int i=1; i<spt.length; i++)
                bw.write("\t" + subjMid.get(Integer.parseInt(spt[i])));
            bw.write("\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job done, size: " + cnt);
    }

    public static void generateInput(String inFile, String outFile, String fbFile) throws Exception {
        EntityIndex.initFromMid2Idx(fbFile);
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (! line.startsWith("#")) continue;
            String[] spt = line.split("\t");
            bw.write("###\t" + spt[0].substring(1, spt[0].length()) + "\t:\n");
            for (int i=1; i<spt.length; i++) {
                String[] pair = spt[i].split(" ");
                String ent1 = EntityIndex.getIdx(pair[0]);
                String ent2 = EntityIndex.getIdx(pair[1]);
                if (ent1 != null && ent2 != null)
                    bw.write(ent1 + "\t" + ent2 + "\n");
            }
        }
        bw.close();
    }

    public static void changeInputFormat(String inFile, String path) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = null;
        String line, absPath = ""; int num = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("###")) {
                absPath = String.format("%s/%d", path, num);
                File f = new File(absPath);
                f.mkdirs();
                LogInfo.logs("mkdir: %s", absPath);
                bw = new BufferedWriter(new FileWriter(absPath + "/info.txt"));
                bw.write(line.split("\t")[1] + "\n");
                bw.close();
                num ++;
                bw = new BufferedWriter(new FileWriter(absPath + "/entity_pair.txt"));
                continue;
            }
            bw.write(line + "\n");
            bw.flush();
        }
        bw.close();
    }
}
