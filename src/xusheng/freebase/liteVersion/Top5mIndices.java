package xusheng.freebase.liteVersion;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

/**
 * Created by angrymidiao on 10/7/15.
 */
public class Top5mIndices {

    public static String path = "/home/xusheng/data_0911/lite/top5m.txt";
    public static HashSet<String> top5m = new HashSet<>();

    public static void initialize() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            top5m.add(spt[0]);
        }
        br.close();
        LogInfo.logs("Top 5 million entities read.");
    }
}
