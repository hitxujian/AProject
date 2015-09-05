package xusheng.wikipedia.EP;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/6/11.
 */
public class TitleIndex {

    public static HashMap<String, String> idxToTitle = new HashMap<>(), titleToIdx = new HashMap<>();

    public static String getTitle(String idx) {
        if (idxToTitle.containsKey(idx))
            return idxToTitle.get(idx);
        else return null;
    }

    public static String getIdx(String title) {
        if (titleToIdx.containsKey(title))
            return titleToIdx.get(title);
        else return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = ""; int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            LogUpgrader.showLine(num, 500000);
            String[] spt = line.split("\t");
            idxToTitle.put(spt[0], spt[1]);
            titleToIdx.put(spt[1], spt[0]);
        }
        br.close();
        LogInfo.logs("Wikipedia Title Index read into memory! size: %d", idxToTitle.size());
    }

}
