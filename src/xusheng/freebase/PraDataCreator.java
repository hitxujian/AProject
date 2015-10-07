package xusheng.freebase;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.*;

/**
 * Created by Xusheng on 2015/10/6.
 * Create input data for pra/../FreebaseKbFileCreator
 */
public class PraDataCreator {

    public static String getName(String str) {
        String[] spt = str.split("/");
        String raw = spt[spt.length-1];
        int len = raw.length();
        String ret = raw.substring(0, len - 1);
        return ret;
    }

    public static boolean wantedPred(String pred) {
        if (!pred.startsWith("freebase") && !pred.startsWith("base") && !pred.startsWith("common")
                && !pred.startsWith("type") && !pred.startsWith("user") && !pred.startsWith("key"))
            return true;
        else return false;
    }

    public static void scan(String inFile, String outFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        LogInfo.logs("Start to scan inverse, domain, range...");
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 1000000 == 0) LogUpgrader.showLine(cnt, 1000000);
            String[] spt = line.split("\t");
            String selector = getName(spt[1]);
            String subj = getName(spt[0]);
            String obj = getName(spt[2]);

            if (selector.equals("type.property.reverse_property")
                    && !subj.startsWith("m.") && !obj.startsWith("m.")
                    && wantedPred(subj) && wantedPred(obj)) {
                bw.write("inverse\t" + subj + "\t" + obj + "\n");
            } else if (selector.equals("type.property.expected_type")
                    && !subj.startsWith("m.") && !obj.startsWith("m.")
                    && wantedPred(subj)) {
                bw.write("range\t" + subj + "\t" + obj + "\n");
            } else if (selector.equals("type.property.schema")
                    && !subj.startsWith("m.") && !obj.startsWith("m.")
                    && wantedPred(subj)) {
                bw.write("domain\t" + subj + "\t" + obj + "\n");
            }
        }
        LogInfo.logs("Job done.");
        br.close();
        bw.close();
    }

    public static String entIdxFile  = "/home/xusheng/data_0911/entity_index.aaai",
                         predIdxFile = "/home/xusheng/data_0911/pred_index.aaai",
                         typeIdxFile = "/home/xusheng/data_0911/type_index.aaai";

    public static void changeForm(String inFile, String outFile, String typeFile) throws Exception {
        EntityIndex.initFromIdx2Mid(entIdxFile);
        PredicateIndex.initialize(predIdxFile);
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String subj = EntityIndex.getMid(spt[0]);
            String obj = EntityIndex.getMid(spt[2]);
            String rel = PredicateIndex.getPre(spt[1]);
            bw.write(subj + "\t" + rel + "\t" + obj + "\n");
        }
        br.close();
        TypeIndex.initialize(typeIdxFile);
        br = new BufferedReader(new FileReader(typeFile));
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String ent = EntityIndex.getMid(spt[0]);
            for (int i=1; i<spt.length; i++)
                bw.write(ent + "\ttype.object.type\t" + TypeIndex.getType(spt[i]) + "\n");
        }

    }

    public static void main(String[] args) throws Exception {
        //scan(args[0], args[1]);
        changeForm(args[2], args[3], args[4]);
    }
}
