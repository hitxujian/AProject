package xusheng.kg.fbzh;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Xusheng Luo on 3/23/2016.
 */

public class ZhFbWorker {
    public static String root = "/home/xusheng/zh-freebase";
    public static String fbzhFp = root + "/mid-name.raw";

    public static void makeEIList() throws IOException {
        File f = new File(fbzhFp);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
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

    public static String getName(String str) {
        String[] spt = str.split("/");
        String raw = spt[spt.length-1];
        int len = raw.length();
        String ret = raw.substring(0, len - 1);
        return ret;
    }

    /*
    TODO
    extract entity pairs from zh-fb
    ? question: should we ignore those nodes without chinese name but
    does connect two entity with chinese names ?
      */
    public static Map<String, String> zhEntMap = null;
    public static void extractPredEP() throws IOException {
        zhEntMap = EntityHandler.getEntityMap();
        File f = new File("/home/data/freebase/freebase-rdf-latest");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String idl = getName(spt[0]);
            String idr = getName(spt[2]);
            String pred = getName(spt[1]);
            if (idl.startsWith("m.") && idr.startsWith("m.")) {
                if (zhEntMap.containsKey(idl) && zhEntMap.containsKey(idr)) {
                    if (!pred.startsWith("freebase") && !pred.startsWith("base") && !pred.startsWith("common")
                            && !pred.startsWith("type") && !pred.startsWith("user") && !pred.startsWith("key"))
                        LogInfo.logs("%s\t%s\t%s", idl, pred, idr);
                }
            }
        }
        br.close();
    }

    /*
    TODO
    link zh predicates to fb predicates
    ! maybe move this function to another class...
     */
    public static void link4zhPred() {

    }

    public static void main(String[] args) throws IOException {
        //makeEIList();
        extractPredEP();
    }
}
