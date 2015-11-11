package xusheng.experiment;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Administrator on 2015/10/13.
 */
public class Sampler {

    public static String file = "/home/kangqi/workspace/UniformProject/dist.txt";
    public static String dir = "/home/xusheng";

    public static void main(String[] args) throws Exception {
        rename();
    }

    public static void rename() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(dir + "/rand120.tsv"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(dir + "/rand120-name.tsv"));
        String line = "";
        HashSet<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String idx = line.split("_")[0];
            set.add(idx);
        }
        br.close();
        br = new BufferedReader(new FileReader(file));
        while ((line = br.readLine()) != null) {
            String idx;
            if (line.startsWith(" "))
                idx = line.split(" +")[3];
            else
                idx = line.split(" +")[2];
            if (set.contains(idx)) {
                bw.write(idx + "_\n");
                bw.write(line + "\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("Job Done");
    }

    public static void deal() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(dir + "/rand120.tsv"));
        String line = "";
        HashSet<Integer> set = new HashSet<>();
        Random random = new Random();
        while (set.size()<120) set.add(1 + random.nextInt(1500));
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (set.contains(cnt)) {
                if (line.startsWith(" "))
                    bw.write(line.split(" +")[3] + "_" + line.split(" +")[3] + "\n");
                else
                    bw.write(line.split(" +")[2] + "_" + line.split(" +")[2] + "\n");
            }
            if (cnt >1500) break;
        }
        br.close();
        bw.close();
        LogInfo.logs("Job Done");
    }
}
