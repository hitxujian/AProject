package xusheng.kg.wiki;

import fig.basic.LogInfo;

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
        BufferedReader br = new BufferedReader(new FileReader(rootFp +
                "dbpedia/infobox_properties_en.ttl"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp +
                "nn/wiki_info.tsv"));
        String line;
        while ((line = br.readLine()) != null) {
            try {
                String[] spt = line.split(" ");
                String subj = spt[0].split("resource/")[1].split(">")[0].replace("_", " ").toLowerCase();
                String obj_s = line.split("\"")[1].trim().toLowerCase();
                String relation = spt[1].split("property/")[1].split(">")[0].replace("_", " ").toLowerCase();
                bw.write(String.format("%s\t%s\t%s\n", subj, relation, obj_s));
            } catch (Exception ex) {
                ex.printStackTrace();
                LogInfo.logs("[error] %s", line);
            }
        }
        br.close();
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        getCleanData();
    }
}
