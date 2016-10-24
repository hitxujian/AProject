package xusheng.kg.wiki;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Xusheng on 04/10/2016.
 * Read Wikipedia Anchor Text info from DBpedia source.
 */

public class AnchorTextReader {
    public static String dataFp = "/home/xusheng/dbpedia/anchor_text_en.ttl";

    public static Map<String, Set<String>> ReadDataFromEnt2Name() throws IOException {
        Map<String, Set<String>> anchorMap = new HashMap<>();
        LogInfo.begin_track("Start to read %s from entity to name.", dataFp);
        BufferedReader br = new BufferedReader(new FileReader(dataFp));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("<http"))
                continue;
            try {
                String[] spt = line.split(" ");
                // entity not contains "_";
                String entity = spt[0].split("resource/")[1].split(">")[0].replace("_", " ").toLowerCase();
                // name contains no "_" (replaced by " ")
                String name = line.split("\"")[1].trim().toLowerCase();
                if (!anchorMap.containsKey(entity))
                    anchorMap.put(entity, new HashSet<>());
                anchorMap.get(entity).add(name);
                if (cnt < 10)
                    LogInfo.logs(entity + "\t" + name);
            } catch (Exception ex) {
                ex.printStackTrace();
                LogInfo.logs("[error] %s", line);
            }
            cnt ++;
            LogUpgrader.showLine(cnt, 1000000);
        }
        br.close();
        LogInfo.end_track();
        return anchorMap;
    }

    public static Map<String, Set<String>> ReadDataFromName2Ent() throws IOException {
        Map<String, Set<String>> anchorMap = new HashMap<>();
        LogInfo.begin_track("Start to read %s from name to entity.", dataFp);
        BufferedReader br = new BufferedReader(new FileReader(dataFp));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("<http"))
                continue;
            try {
                String[] spt = line.split(" ");
                // entity not contains "_";
                String entity = spt[0].split("resource/")[1].split(">")[0].replace("_", " ").toLowerCase();
                // name contains no "_" (replaced by " ")
                String name = line.split("\"")[1].trim().toLowerCase();
                if (!anchorMap.containsKey(name))
                    anchorMap.put(name, new HashSet<>());
                anchorMap.get(name).add(entity);
                if (cnt < 10)
                    LogInfo.logs(entity + "\t" + name);
            } catch (Exception ex) {
                ex.printStackTrace();
                LogInfo.logs("[error] %s", line);
            }
            cnt ++;
            LogUpgrader.showLine(cnt, 1000000);
        }
        br.close();
        LogInfo.end_track();
        return anchorMap;
    }

    public static Map<String, Set<String>> ReadDataFromName2Ent(String cmd) throws IOException {
        Map<String, Set<String>> anchorMap = new HashMap<>();
        dataFp = "/home/xusheng/nn/data/wikipedia/prior.txt";
        LogInfo.begin_track("Start to read %s from name to entity.", dataFp);
        BufferedReader br = new BufferedReader(new FileReader(dataFp));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (spt[0].equals(" ")) continue;
            if (!anchorMap.containsKey(spt[0]))
                anchorMap.put(spt[0], new HashSet<>());
            anchorMap.get(spt[0]).add(spt[1]);
            cnt ++;
            LogUpgrader.showLine(cnt, 1000000);
        }
        br.close();
        LogInfo.end_track();
        return anchorMap;
    }
}
