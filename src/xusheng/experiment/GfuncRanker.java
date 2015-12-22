package xusheng.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by angrymidiao on 12/22/2015.
 */
public class GfuncRanker {

    public static String schemaFile = "/home/kangqi/workspace/UniformProject/resources" +
            "/paraphrase/emnlp2015/PATTY120_Matt-Fb2m_med_gGD_s20_len3_fb1_sh0_aT0_c150" +
            "_c21.2_aD1_SF1_SL1_cov0.10_pH10_dt1.0_sz30000_aI1/362_362/schema";

    public static void work() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(schemaFile));
        String line;
        while ((line = br.readLine()) != null) {

        }
    }

    public static void main(String[] args) {

    }
}
