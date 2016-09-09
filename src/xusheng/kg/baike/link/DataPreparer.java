package xusheng.kg.baike.link;

import java.io.*;

import fig.basic.LogInfo;
import xusheng.misc.IndexNameReader;

/**
 * Created by Xusheng on 8/30/2016.
 * Generate proper data format for link completion task
 */

public class DataPreparer {

    public static String rootFp = "/home/xusheng/starry/hudongbaike";

    public static IndexNameReader inr = null;
    public static void main(String[] args) throws Exception {
        inr = new IndexNameReader(rootFp + "/infobox/entIdx.tsv");
        inr.initializeFromName2Idx("NI");
        getData();
    }

    public static void getData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/infobox/triples.tsv"));
        BufferedWriter bwL = new BufferedWriter(new FileWriter(rootFp + "/infobox/KB_linked.tsv"));
        BufferedWriter bwU = new BufferedWriter(new FileWriter(rootFp + "/infobox/KB_unlinked.tsv"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            try {
                if (spt[2].startsWith("http")) {
                    int subj = inr.getIdx(spt[0]);
                    int obj = inr.getIdx(spt[2]);
                    if (subj != -1 && obj != -1)
                        bwL.write(String.format("%d\t%s\t%d\n", subj, spt[1], obj));
                } else {
                    int subj = inr.getIdx(spt[0]);
                    if (subj != -1)
                        bwU.write(String.format("%d\t%s\t%s\n", subj, spt[1], spt[2]));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        br.close();
        bwL.close();
        bwU.close();
        LogInfo.logs("[info] Data prepared.");
    }
    
}
