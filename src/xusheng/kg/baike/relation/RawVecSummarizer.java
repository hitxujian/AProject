package xusheng.kg.baike.relation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Xusheng on 7/18/2016.
 *
 */
public class RawVecSummarizer {
    public static String rootFp = "/home/xusheng/starry/baidubaike";

    public static void main(String[] args) throws Exception {
        summary();
    }

    public static void summary() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/raw_vectors"));
    }
}
