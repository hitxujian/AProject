package xusheng.kg.fbzh;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xusheng Luo on 3/23/2016.
 */

public class ZhFbWorker {
    public static String root = "/home/xusheng/zh-freebase";
    public static String fbzhFp = root + "/fb-mid-zh";

    public static void makeEIList() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fbzhFp));
        Set<String> set = new HashSet<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String mid = spt[0].split("ns/")[1].split(">")[0];
            String name = spt[2].split("\"")[1];
            if (!set.contains(mid)) {
                set.add(mid);
                LogInfo.logs("%s\t%s", mid, name);
            }
        }
        LogInfo.logs(set.size());
        br.close();
    }

    /*
    TODO
    extract entity pairs from zh-fb
    ? question: should we ignore those nodes without chinese name but
    does connect two entity with chinese names ?
      */
    public static void extractPredEP() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(root + "/"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            
        }
    }

    /*
    TODO
    link zh predicates to fb predicates
    ! maybe move this function to another class...
     */
    public static void link4zhPred() {

    }

    public static void main(String[] args) throws IOException {
        makeEIList();
    }
}
