package xusheng.aaai2016.experiment;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/9/15.
 */
public class ELPreparer {

    public static void main(String[] args) throws Exception {
        //changeFormat(args[0], args[1]);
        genrate2Files(args[1], args[2], args[3], args[4]);
    }

    public static String removeUnderline(String entity) {
        return entity.replaceAll("_+", " ");
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

    public static void genrate2Files(String inFile, String linkingFile, String nameFile, String idxFile) throws Exception {
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
