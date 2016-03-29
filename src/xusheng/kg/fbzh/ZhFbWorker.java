package xusheng.kg.fbzh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Xusheng Luo on 3/23/2016.
 */

public class ZhFbWorker {
    public static String root = "/home/xusheng";
    public static String fbzhFp = root + "/freebase-zh";
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fbzhFp));
        String line;
        while ((line = br.readLine()) != null) {

        }
    }
}
