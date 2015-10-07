package xusheng.freebase.liteVersion;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by angrymidiao on 10/7/15.
 */
public class Top5mIndices {

    public static String path = "/home/xusheng/data_0911/lite";
    public static HashSet<String> top5m = new HashSet<>();

    public static void initialize() throws Exception {
        LogInfo.logs("Start to read Top 5 million entities...");
        BufferedReader br = new BufferedReader(new FileReader(path + "/top5m.txt"));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            top5m.add(spt[0]);
        }
        br.close();
        LogInfo.logs("Top 5 million entities read.");
    }

    public static HashMap<String, String> old2new = new HashMap<>();

    public static String getNewIdx(String old) {
        if (old2new.containsKey(old)) return old2new.get(old);
        else {
            LogInfo.logs("Error: NULL for old idx: " + old);
            return null;
        }
    }

    public static String newPath = "/home/xusheng/data_0911/lite-newIdx"    ;
    public static void initForIdxChange() throws Exception {
        LogInfo.logs("Start to read entity changers...");
        BufferedReader br = new BufferedReader(new FileReader(newPath + "/idx-changer.txt"));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            old2new.put(spt[0], spt[1]);
        }
        br.close();
        LogInfo.logs("Idx changers read.");
    }
}
