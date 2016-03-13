package xusheng.experiment;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng Luo on 3/13/2016.
 */
public class Yuchen {
    public static String filePath = "/home/yuchen/";
    public static Map<String, Double> map = new HashMap<>();

    public static void work(String name) throws IOException{
        LogInfo.logs("work for %s", name);
        BufferedReader br = new BufferedReader(new FileReader(filePath + name));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String key = spt[0] + "\t" + spt[1];
            double value = Double.parseDouble(spt[2]);
            if (!map.containsKey(key))
                map.put(key, value);
            else {
                double tmp = map.get(key);
                map.put(key, tmp + value);
            }
            cnt ++;
            LogUpgrader.showLine(cnt, 10000);
        }
    }

    public static void endJob() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/IDwithFreq.merge"));
        int cnt = 0;
        for (Map.Entry<String, Double> entry: map.entrySet()) {
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            cnt ++;
            LogUpgrader.showLine(cnt, 10000);
        }
    }

    public static void main(String[] args) throws IOException {
        work("IDwithFreq.txt");
        work("IDwithFreq2.txt");
        work("IDwithFreq3.txt");
        endJob();
    }
}
