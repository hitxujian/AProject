package xusheng.experiment;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by Administrator on 2015/10/13.
 */
public class OpenIEChecker {

    public static String wiseDir = "/home/xusheng/WiSeNet/WiSeNet-2.0";

    public static void main(String[] args) throws Exception {
         dealWithWisenet();
    }

    public static void dealWithWisenet() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(wiseDir + "/"));

    }
}
