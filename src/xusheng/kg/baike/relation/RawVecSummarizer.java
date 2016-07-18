package xusheng.kg.baike.relation;

import fig.basic.LogInfo;

import java.io.*;
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
            String rawVec = spt[1];
            for (char ch: rawVec.toCharArray()) {
                if (!charSet.containsKey(ch)) {
                    charSet.put(String.valueOf(ch), chIdx);
                    chIdx ++;
                }
            }
        }
        LogInfo.logs("Char set created. Size: %d", charSet.size());

        br = new BufferedReader(new FileReader(rootFp + "/raw_vectors"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/real_vectors"));
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rawVec = spt[1];
            double[] realvec = new double[charSet.size()];
            int relIdx = Integer.parseInt(spt[0]);
            int total = 0;
            for (char ch: rawVec.toCharArray()) {
                int idx = charSet.get(ch);
                realvec[idx] ++;
                total ++;
            }
            bw.write(relIdx);
            for (int i=0; i<charSet.size(); i++) {
                realvec[i] /= total;
                bw.write(" " + realvec[i]);
            }
            bw.write("\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Real vectors generated.");
    }
}
