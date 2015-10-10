package xusheng.freebase;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/5/19.
 */

public class EntityType {
    public static HashMap<String, ArrayList<String>> en2type = new HashMap<>();

    public static ArrayList<String> getTypes(String entity) {
        ArrayList<String> ret = new ArrayList<>();
        if (en2type.containsKey(entity)) return en2type.get(entity);
        else return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        int num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            LogUpgrader.showLine(num, 10000000);
            String[] spt = line.split("\t");
            if (!en2type.containsKey(spt[0]))
                en2type.put(spt[0], new ArrayList<>());
            for (int i=1; i<spt.length; i++)
                en2type.get(spt[0]).add(spt[i]);
        }
        br.close();
        LogInfo.logs("Freebase Entity Type read into memory!");
    }
}
