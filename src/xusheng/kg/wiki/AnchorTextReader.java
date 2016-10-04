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

    public static Map<String, Set<String>> ReadData() throws IOException {
        Map<String, Set<String>> anchorMap = new HashMap<>();
        LogInfo.begin_track("Start to read %s", dataFp);
        BufferedReader br = new BufferedReader(new FileReader(dataFp));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split(" ");
            // entity still contains "_";
            String entity = spt[0].split("resource/")[1].split(">")[0].replace("_", " ");
            // name contains no "_" (replaced by " ")
            String name = spt[2].split("\"")[1];
            if (!anchorMap.containsKey(entity))
                anchorMap.put(entity, new HashSet<>());
            anchorMap.get(entity).add(name);
            cnt ++;
            if (cnt < 10)
                LogInfo.logs(entity + "\t" + name);
            LogUpgrader.showLine(cnt, 1000000);
        }
        br.close();
        LogInfo.end_track();
        return anchorMap;
    }
}