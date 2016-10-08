package xusheng.kg.wiki;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.*;
import java.util.HashSet;

/**
 * Created by Xusheng on 10/8/2016.
 */
public class NNDataPreparer {
    public static String rootFp = "/home/xusheng";

    public static void getTrainingData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp +
                "dbpedia/infobox_properties_en.ttl"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp +
                "nn/training.tsv"));

    }

    public static void getTestingData() throws IOException {

    }

    public static void getCleanData() throws IOException {
        File f = new File(rootFp + "/dbpedia/infobox_properties_en.ttl");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        f = new File(rootFp + "/nn/wiki_info_linked.tsv");
        BufferedWriter bwl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        f = new File(rootFp + "/nn/wiki_info_unlinked.tsv");
        BufferedWriter bwu = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));

        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 1000000);
            try {
                String[] spt = line.split(" ");
                String subj = spt[0].split("resource/")[1].split(">")[0].replace("_", " ").toLowerCase();
                String relation = spt[1].split("property/")[1].split(">")[0].replace("_", " ").toLowerCase();
                String obj_s;
                if (spt[2].contains("dbpedia.org/resource")) {
                    obj_s = spt[2].split("resource/")[1].split(">")[0].replace("_", " ").toLowerCase();
                    bwl.write(String.format("%s\t%s\t%s\n", subj, relation, obj_s));
                }
                else{
                    obj_s = spt[2];
                    if (obj_s.equals("\""))
                        obj_s = spt[2] + spt[3];
                    bwu.write(String.format("%s\t%s\t%s\n", subj, relation, obj_s));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                LogInfo.logs("[error] %s", line);
            }
        }
        br.close();
        bwl.close();
        bwu.close();
    }

    public static void main(String[] args) throws IOException {
        getCleanData();
    }
}
