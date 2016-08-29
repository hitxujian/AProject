package xusheng.kg.baike;

import java.io.*;

/**
 * Created by Xusheng on 7/1/2016.
 * Usage: Generate training & testing data on baike
 */

public class SplitDataSampler {
    public static String fp = "/home/xusheng/pra/examples/graphs/baike/kb_svo/graph_chi";
    public static String splitFp = "/home/xusheng/pra/examples/splits/baike_split_with_negatives";

    public static void getPositiveData(String relation) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fp + "/edges.tsv"));
        BufferedWriter bwTr = new BufferedWriter(new FileWriter(splitFp + "/职业/training.tsv"));
        BufferedWriter bwTe = new BufferedWriter(new FileWriter(splitFp + "/职业/testing.tsv"));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (cnt > 150) break;
            String[] spt = line.split("\t");
            if (spt[1].equals(relation)) {
                cnt++;
                if (cnt <= 100) bwTr.write(BkEntIdxReader.getName(Integer.parseInt(spt[0])) + "\t"
                        + BkEntIdxReader.getName(Integer.parseInt(spt[2])) + "\t1\n");
                else bwTe.write(BkEntIdxReader.getName(Integer.parseInt(spt[0])) + "\t"
                        + BkEntIdxReader.getName(Integer.parseInt(spt[2])) + "\t1\n");
            }
        }
        br.close();
        bwTr.close();
        bwTe.close();
    }

    public static void getNegativeData(String relation) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fp + "/edges.tsv"));
        BufferedWriter bwTr = new BufferedWriter(new FileWriter(splitFp + "/职业/training.tsv", true));
        BufferedWriter bwTe = new BufferedWriter(new FileWriter(splitFp + "/职业/testing.tsv", true));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (cnt > 750) break;
            String[] spt = line.split("\t");
            if (spt[1].equals(relation)) {
                cnt ++;
                if (cnt <= 500) bwTr.write(BkEntIdxReader.getName(Integer.parseInt(spt[0])) + "\t"
                        + BkEntIdxReader.getName(Integer.parseInt(spt[2])) + "\t-1\n");
                else bwTe.write(BkEntIdxReader.getName(Integer.parseInt(spt[0])) + "\t"
                        + BkEntIdxReader.getName(Integer.parseInt(spt[2])) + "\t-1\n");
            }
        }
        br.close();
        bwTr.close();
        bwTe.close();
    }

    public static void main(String[] args) throws IOException {
        BkEntIdxReader.initializeFromIdx2Name("baidu");
        BkRelIdxReader.initializeFromIdx2Name("baidu");
        getPositiveData("201256");
        getNegativeData("57559");
    }
}
