package xusheng.kg.baidubaike;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xusheng on 7/1/2016.
 * Each function aims to change some file format
 * Refer to the specific notes
 */
public class FormatChanger {

    public static void RecoverInfoTriple() throws IOException {
        BkEntityIdxReader.initializeFromIdx2Name();
        BufferedReader br = new BufferedReader(new FileReader("/home/xusheng/starry/baidubaike/infobox.triple"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/starry/baidubaike/infobox.triple.raw.clean"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String leftName = null;
            String rightName = null;
            try {
                leftName = BkEntityIdxReader.getName(Integer.parseInt(spt[0]));
                rightName = BkEntityIdxReader.getName(Integer.parseInt(spt[2]));
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
            if (leftName != null && rightName != null)
                bw.write(leftName + "\t" + spt[1] + "\t" + rightName + "\n");
            /*else if (leftName == null && rightName == null)
                bw.write(line + "\n");
            else if (leftName != null)
                bw.write(leftName + "\t" + spt[1] + "\t" + spt[2] + "\n");
            else
                bw.write(spt[0] + "\t" + spt[1] + "\t" + rightName + "\n");*/
        }
        br.close();
        bw.close();
    }

    // generate node_dict.tsv & edge_dict.tsv
    public static void generateDictFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/home/xusheng/starry/baidubaike/entity.index"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/pra/examples/graphs/baike/kb_svo" +
                "/node_dict.tsv"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            bw.write(spt[1] + "\t" + spt[0] + "\n");
        }
        br.close();
        bw.close();

        Set<String> edges = new HashSet<>();
        File f = new File("/home/xusheng/starry/baidubaike/infobox.triple");
        br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        bw = new BufferedWriter(new FileWriter("/home/xusheng/pra/examples/graphs/baike/kb_svo" +
                "/edge_dict.tsv"));
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            edges.add(spt[1]);
        }
        br.close();
        int cnt = 0;
        for (String edge: edges) {
            cnt ++;
            bw.write(cnt + "\t" + edge + "\n");
        }
        bw.close();
        LogInfo.logs("Total number of eades: %d", edges.size());
    }

    public static void main(String[] args) throws IOException {
        //RecoverInfoTriple();
        generateDictFile();
    }
}
