package xusheng.kg.baidubaike;

import java.io.*;

/**
 * Created by Xusheng on 7/1/2016.
 * Usage: Generate training & testing data on baike
 */

public class SplitDataSampler {
    public static String fp = "/home/xusheng/starry/baidubaike";

    public static void getPositiveData(String relation) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fp + "/infobox.triple"));
        BufferedWriter bwTr = new BufferedWriter(new FileWriter(fp + "/training.tsv"));
        BufferedWriter bwTe = new BufferedWriter(new FileWriter(fp + "/testing.tsv"));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt > 150) break;
            String[] spt = line.split("\t");
            if (spt[1].equals(relation))
                if (cnt <= 100) bwTr.write(spt[0] + "\t" + spt[2] + "\t1\n");
                else bwTe.write(spt[0] + "\t" + spt[2] + "\t1\n");
        }
        br.close();
        bwTr.close();
        bwTe.close();
    }

    public static void getNegativeData(String relation) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fp + "/infobox.triple"));
        BufferedWriter bwTr = new BufferedWriter(new FileWriter(fp + "/training.tsv", true));
        BufferedWriter bwTe = new BufferedWriter(new FileWriter(fp + "/testing.tsv", true));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt > 750) break;
            String[] spt = line.split("\t");
            if (spt[1].equals(relation))
                if (cnt <= 500) bwTr.write(spt[0] + "\t" + spt[2] + "\t1\n");
                else bwTe.write(spt[0] + "\t" + spt[2] + "\t1\n");
        }
        br.close();
        bwTr.close();
        bwTe.close();
    }

    public static void main(String[] args) throws IOException {
        getPositiveData("女儿");
        getNegativeData("儿子");
    }
}
