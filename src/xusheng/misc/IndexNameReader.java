package xusheng.misc;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 7/18/2016.
 * General Index-Name form Reader
 */

public class IndexNameReader {

    private String path = "/home/xusheng/starry/baidubaike/edge_dict.tsv";
    private Map<String, Integer> name2Idx;
    private Map<Integer, String> idx2Name;

    public IndexNameReader(String fp) {
        path = fp;
        name2Idx = null;
        idx2Name = null;
    }

    public int getIdx(String name) {
        if (name2Idx == null) LogInfo.logs("Error! Pls initialize before using!");
        if (name2Idx.containsKey(name)) return name2Idx.get(name);
        else return -1;
    }

    public String getName(int idx) {
        if (idx2Name == null) LogInfo.logs("Error! Pls initialize before using!");
        if (idx2Name.containsKey(idx)) return idx2Name.get(idx);
        else return null;
    }

    public void initializeForBoth() throws IOException {
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
        LogInfo.logs("%s Relation-Idx(Both Sides) Read. Size: %d", path, cnt);
    }

    public void initializeFromName2Idx() throws IOException {
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
        LogInfo.logs("%s Relation-Idx(Name to Idx) Read. Size: %d", path, cnt);
    }

    public void initializeFromName2Idx(String dir) throws IOException {
        if (name2Idx != null) return;
        name2Idx = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            String[] spt = line.split("\t");
            if (dir.equals("IN") || dir.equals("Idx-Name"))
                name2Idx.put(spt[1], Integer.parseInt(spt[0]));
            else if (dir.equals("NI") || dir.equals("Name-Idx"))
                name2Idx.put(spt[0], Integer.parseInt(spt[1]));
        }
        br.close();
        LogInfo.logs("%s Relation-Idx(Name to Idx) Read. Size: %d", path, cnt);
    }

    public void initializeFromIdx2Name() throws IOException {
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
        LogInfo.logs("%s Relation-Idx(Idx to Name) Read. Size: %d", path, cnt);
    }

    public void initializeFromIdx2Name(String dir) throws IOException {
        if (idx2Name != null) return;
        idx2Name = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            String[] spt = line.split("\t");
            if (dir.equals("IN") || dir.equals("Idx-Name"))
                idx2Name.put(Integer.parseInt(spt[0]), spt[1]);
            else if (dir.equals("NI") || dir.equals("Name-Idx"))
                idx2Name.put(Integer.parseInt(spt[1]), spt[0]);
        }
        br.close();
        LogInfo.logs("%s Relation-Idx(Idx to Name) Read. Size: %d", path, cnt);
    }
}
