package xusheng.kg.baike.relation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 7/18/2016.
 *
 */
public class RawVecSummarizer {
    public static String rootFp = "/home/xusheng/starry/baidubaike";

    public static void main(String[] args) throws Exception {
        summary();
    }

    public static Map<String, Integer> charSet = new HashMap<>();
    public static void summary() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/raw_vectors"));
        String line;
        int chIdx = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int relIdx = Integer.parseInt(spt[0]);

            String rawVec = spt[1];
            for (char ch: rawVec.toCharArray()) {
                if (!charSet.containsKey(ch)) {
                    chIdx ++;
                    charSet.put(String.valueOf(ch), chIdx);
                }

            }
        }
    }
}
