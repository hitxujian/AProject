package xusheng.freebase.liteVersion;

import java.util.HashMap;

/**
 * Created by angrymidiao on 10/7/15.
 */
public class Filter {

    public static HashMap<String, Integer> cntList = new HashMap<>();

    public static void countPopularity(String inFile, String outFile) {

    }

    public static void main(String[] args) throws Exception {
        countPopularity(args[0], args[1]);
    }
}
