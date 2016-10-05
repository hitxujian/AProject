package xusheng.kg.wiki;

import fig.basic.LogInfo;

import java.io.*;

/**
 * Created by Xusheng on 05/10/2016.
 * Prepare data for link-added wikipedia splits word2vec training
 */

public class W2VDataPreparer {
    public static String rootFp = "/home/xusheng/wikipedia/extracted";

    public static void mergeData() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/wiki4train_link.txt"));
        for (char Ch ='A'; Ch<='F'; Ch++) {
            for (char ch = 'A'; ch <= 'Z'; ch++) {
                for (int i = 0; i <= 9; i++) {
                    String fp = String.format("%s/%s%s/wiki_0%d_links", rootFp, Ch, ch, i);
                    if (new File(fp).exists()) {
                        merge(fp, bw);
                    }
                }
                for (int i = 10; i <= 99; i++) {
                    String fp = String.format("%s/%s%s/wiki_%d_links", rootFp, Ch, ch, i);
                    if (new File(fp).exists()) {
                        merge(fp, bw);
                    }
                }
            }
        }
        bw.close();
    }

    public static void merge(String fp, BufferedWriter bw) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fp));
        String line;
        while ((line = br.readLine()) != null) {
            bw.write(line + "\n");
        }
        br.close();
        LogInfo.logs("[log] [%s] merged.", fp);
    }

    public static void main(String[] args) throws Exception {
        mergeData();
    }
}
