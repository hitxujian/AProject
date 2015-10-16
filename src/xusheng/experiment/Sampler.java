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

    public static String file = "/home/kangqi/workspace/UniformProject/dist.tsv";
    public static String dir = "/home/xusheng";

    public static void main(String[] args) throws Exception {
         deal();
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
                bw.write(line.split(" ")[5] + "_\n");
                bw.write(line.split(" ")[5] + "\t");
                bw.write(line.split(";")[1] + "\t" + line.split(";")[2] + "\t"
                        + line.split(";")[3] + "\n");
            }
            if (cnt >1500) break;
        }
        br.close();
        bw.close();
        LogInfo.logs("Job Done");
    }
}
