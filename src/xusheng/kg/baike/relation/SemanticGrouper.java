package xusheng.kg.baike.relation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Xusheng on 7/13/2016.
 * Cluster cleaned-up relations in semantic level
 * by using embedding vectors.
 */
public class SemanticGrouper implements Runnable{

    public static String rootFp = "/home/xusheng/starry/baidubaike";
    public static String relFp = rootFp + "/infobox.text";


    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int curr = -1, end = -1;
    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            return ret;
        }
        return -1;
    }


    public static void multiThreadWork() throws Exception {

    }

    public static void readRelation() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "edge_dict.tsv.v1"));
        String line;
        String[] rels = new String[210000];
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            rels[Integer.parseInt(spt[0])] = spt[1];

        }
    }

    public static void main(String[] args) throws IOException {

    }


}
