package xusheng.experiment;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 12/22/2015.
 */

public class GfuncRanker {

    public static String schemaFile = "/home/kangqi/workspace/UniformProject/resources" +
            "/paraphrase/emnlp2015/PATTY120_Matt-Fb2m_med_gGD_s20_len3_fb1_sh0_aT0_c150" +
            "_c21.2_aD1_SF1_SL1_cov0.10_pH10_dt1.0_sz30000_aI1/362_362/schema";
    public static int E = 86054363;
    public static HashMap<String, Double> map = null;
    public static boolean verbose;

    public static void work() throws IOException{
        map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(schemaFile));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            double fscore = Double.parseDouble(line.split("S\\) = ")[1].split(" ")[0]);
            int cover = Integer.parseInt(line.split("EP ")[1].split(" ")[0]);
            int numOfEp = Integer.parseInt(line.split("of ")[1].split(" ")[0]);
            if (verbose) LogInfo.logs(fscore + " " + cover + " " + numOfEp);
            double gscore = fscore + 2 * Math.log(E) + numOfEp - cover;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(line + "\n");
            while ((line = br.readLine()).length() != 0) {
                stringBuffer.append(line + "\n");
            }
            map.put(stringBuffer.toString(), gscore);
            cnt ++;
            if (verbose) LogUpgrader.showLine(cnt, 20);
        }

        int rank = 0;
        ArrayList<Map.Entry<String, Double>> sorted = MapHelper.sort(map, true);
        for (Map.Entry<String, Double> entry : sorted) {
            rank ++;
            LogInfo.logs("#" + rank + " g-func: " + entry.getValue() +
                    "\n" + entry.getKey() + "\n");
        }
        LogInfo.logs("Job done.");
    }

    public static void main(String[] args) throws IOException{
        if (args[0].equals("0")) verbose = false;
        else verbose = true;
        work();
    }
}
