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

    public static void main(String[] args) throws IOException {
        scan(args[0], args[1]);
    }
}
