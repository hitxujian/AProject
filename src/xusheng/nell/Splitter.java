package xusheng.nell;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Created by Xusheng on 2015/9/10.
 */
public class Splitter {


    public static String getName(String arg) {
        String[] spt = arg.split(":");
        return spt[spt.length-1];
    }

    public static void splitNell(String inFile, String entFile, String relFile,
                                 String typeFile, String ETFile, String propFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw_e = new BufferedWriter(new FileWriter(entFile));
        BufferedWriter bw_r = new BufferedWriter(new FileWriter(relFile));
        BufferedWriter bw_t = new BufferedWriter(new FileWriter(typeFile));
        BufferedWriter bw_et = new BufferedWriter(new FileWriter(ETFile));
        BufferedWriter bw_p = new BufferedWriter(new FileWriter(propFile));

        HashMap<String, Integer> entSet = new HashMap<>();
        HashMap<String, Integer> relSet = new HashMap<>();
        HashMap<String, Integer> typeSet = new HashMap<>();
        int eidx = 0, ridx = 0, tidx = 0;

        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 100000 == 0) LogUpgrader.showLine(cnt, 100000);
            String[] spt = line.split("\t");
            if (!spt[0].startsWith("concept") || !spt[2].startsWith("concept")) continue;
            String arg1 = getName(spt[0]);
            String arg2 = getName(spt[2]);
            String rel = getName(spt[1]);

            if (! entSet.containsKey(arg1)) {
                eidx ++;
                entSet.put(arg1, eidx);
                bw_e.write(arg1 + "\t" + eidx + "\n");
            }
            if (rel.equals("generalizations")) {
                if (! typeSet.containsKey(arg2)) {
                    tidx ++;
                    typeSet.put(arg2, tidx);
                    bw_t.write(arg2 + "\t" + tidx + "\n");
                }
                bw_et.write(entSet.get(arg1) + "\t" + typeSet.get(arg2) + "\n");
                continue;
            }
            if (! entSet.containsKey(arg2)) {
                eidx ++;
                entSet.put(arg2, eidx);
                bw_e.write(arg2 + "\t" + eidx + "\n");
            }
            if (! relSet.containsKey(rel)) {
                ridx ++;
                relSet.put(rel, ridx);
                bw_r.write(rel + "\t" + ridx + "\n");
            }
            bw_p.write(entSet.get(arg1) + "\t" + relSet.get(rel) + "\t" + entSet.get(arg2) + "\n");
        }
        br.close();
        bw_e.close();
        bw_et.close();
        bw_p.close();
        bw_r.close();
        bw_t.close();
        LogInfo.logs("Job done.");
    }

    public static void main(String[] args) throws Exception {
        splitNell(args[0], args[1], args[2], args[3], args[4], args[5]);
    }
}
