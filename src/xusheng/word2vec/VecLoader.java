package xusheng.word2vec;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by angrymidiao on 11/10/15.
 */
public class VecLoader {

    public static String gooleNewsDir = "/home/xusheng/word2vec/GoogleNews-vectors-negative300.txt";
    public static HashMap<String, ArrayList<Double>> vectors = null;

    public static void load() throws Exception {
        if (vectors != null) return;
        BufferedReader br = new BufferedReader(new FileReader(gooleNewsDir));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 500000);
            String[] spt = line.split(" ");
            ArrayList<Double> vec = new ArrayList<>();
            for (int i=1; i<spt.length; i++) vec.add(Double.parseDouble(spt[i]));
            vectors.put(spt[0], vec);
        }
        br.close();
        LogInfo.logs("Google News Vectors Loaded.");
    }

    public static void main(String[] args) throws Exception {

    }
}
