package xusheng.kg.fbzh;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Xusheng Luo on 3/31/16.
 */

public class EntityHandler {

    private static String root = "/home/xusheng/zh-freebase";
    private static String fp = root + "/entity-zh.index";

    private static Map<String, String> mid2Name = null;
    private static List<String> entities = null;

    public static List<String> getEntityList() throws IOException {
        if (entities != null) {
            LogInfo.logs("%d Entities Read.", entities.size());
            return entities;
        }
        BufferedReader br = new BufferedReader(new FileReader(fp));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            entities.add(spt[0]);
        }
        LogInfo.logs("%d Entities(list) Read.", entities.size());
        br.close();
        return entities;
    }


    public static Map<String, String> getEntityMap() throws IOException {
        if (mid2Name != null) {
            return mid2Name;
        }
        BufferedReader br = new BufferedReader(new FileReader(fp));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            mid2Name.put(spt[0], spt[1]);
        }
        LogInfo.logs("%d Entities(map) Read.", mid2Name.size());
        br.close();
        return mid2Name;
    }

}
