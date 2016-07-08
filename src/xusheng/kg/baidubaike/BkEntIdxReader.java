package xusheng.kg.baidubaike;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Luo Xusheng on 4/21/16.
 * Usage: Read BaiduBaike Article Index file
 * Note that index is from 1 to N and Name is Href style.
 */

public class BkEntIdxReader {
    public static String path = "/home/xusheng/pra/examples/graphs/baike/kb_svo/node_dict.tsv";
    public static Map<String, Integer> name2Idx = null;
    public static Map<Integer, String> idx2Name = null;

    public static Integer getIdx(String name) {
        if (name2Idx.containsKey(name)) return name2Idx.get(name);
        else return null;
    }

    public static String getName(int idx) {
        if (idx2Name.containsKey(idx)) return idx2Name.get(idx);
        else return null;
    }

    public static void initializeForBoth() throws IOException {
        if (name2Idx != null && idx2Name != null) return;
        name2Idx = new HashMap<>();
        idx2Name = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            String[] spt = line.split("\t");
            name2Idx.put(spt[1], Integer.parseInt(spt[0]));
            idx2Name.put(Integer.parseInt(spt[0]), spt[1]);
        }
        br.close();
        LogInfo.logs("BaiduBaike Entity-Idx(Both Sides) Read. Size: %d", cnt);
    }

    public static void initializeFromName2Idx() throws IOException {
        if (name2Idx != null) return;
        name2Idx = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            String[] spt = line.split("\t");
            name2Idx.put(spt[1], Integer.parseInt(spt[0]));
        }
        br.close();
        LogInfo.logs("BaiduBaike Entity-Idx(Name to Idx) Read. Size: %d", cnt);
    }

    public static void initializeFromIdx2Name() throws IOException {
        if (idx2Name != null) return;
        idx2Name = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            String[] spt = line.split("\t");
            idx2Name.put(Integer.parseInt(spt[0]), spt[1]);
        }
        br.close();
        LogInfo.logs("BaiduBaike Entity-Idx(Idx to Name) Read. Size: %d", cnt);
    }

    public static void main(String[] args) throws IOException {

    }
}
