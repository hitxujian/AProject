package xusheng.freebase.liteVersion;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 10/7/15.
 */
public class Filter {

    public static HashMap<String, Integer> cntList = new HashMap<>();
    public static String oriDir = "/home/xusheng/data_0911";
    public static String tarDir = "/home/xusheng/data_0911/lite";

    public static void countPopularity() throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(oriDir + "/prop.aaai"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(tarDir + "/top5m.txt"));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000000 == 0) LogUpgrader.showLine(cnt, 10000000);
            String[] spt = line.split("\t");
            if (!cntList.containsKey(spt[0])) cntList.put(spt[0], 1);
            else {
                int tmp = cntList.get(spt[0]) + 1;
                cntList.put(spt[0], tmp);
            }
            if (!cntList.containsKey(spt[2])) cntList.put(spt[2], 1);
            else {
                int tmp = cntList.get(spt[2]) + 1;
                cntList.put(spt[2], tmp);
            }
        }
        br.close();
        LogInfo.logs("Finish counting, start to sort and write...");
        ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(cntList);
        cnt = 0;
        for (Map.Entry<String, Integer> entry : sorted) {
            cnt ++;
            if (cnt > 5000000) break;
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        bw.close();
        LogInfo.logs("Job done.");
    }

    public static void main(String[] args) throws Exception {
        countPopularity();
    }
}
