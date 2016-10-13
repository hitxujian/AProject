package xusheng.word2vec;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by angrymidiao on 11/10/15.
 */
public class VecLoader {

    public static String googleNewsDir = "/home/xusheng/word2vec/GoogleNews-vectors-negative300.txt";
    public static HashMap<String, ArrayList<Double>> vectors = null;

    public static void load() throws IOException {
        if (vectors != null) return;
        vectors = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(googleNewsDir));
        String line = br.readLine();
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 500000);
            String[] spt = line.split(" ");
            ArrayList<Double> vec = new ArrayList<>();
            for (int i=1; i<spt.length; i++) vec.add(Double.parseDouble(spt[i]));
            //LogInfo.logs(spt[0] + "\t" + vec.size());
            vectors.put(spt[0], vec);
        }
        br.close();
        LogInfo.logs("Google News Vectors Loaded. Size: %d", vectors.size());
    }


    public static Map<String, String> load(String fp) throws IOException {
        LogInfo.logs("[log] Begin to load %s.", fp);
        Map<String, String> vecs = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(fp));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 500000);
            String[] spt = line.split(" ");
            String vec = spt[1];
            for (int i=2; i<spt.length; i++) vec += (" " + spt[i]);
            //LogInfo.logs(spt[0] + "\t" + vec.size());
            vecs.put(spt[0], vec);
        }
        br.close();
        LogInfo.logs("[log] %s Loaded. Size: %d", fp, vecs.size());
        return vecs;
    }

    public static void main(String[] args) throws Exception {

    }
}
