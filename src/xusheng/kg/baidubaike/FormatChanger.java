package xusheng.kg.baidubaike;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Xusheng on 7/1/2016.
 * Each function aims to change some file format
 * Refer to the specific notes
 */
public class FormatChanger {

    // recover the index-replaced files with original names or strings
    public static void RecoverInfoTriple() throws IOException {
        BkEntIdxReader.initializeFromIdx2Name();
        BufferedReader br = new BufferedReader(new FileReader("/home/xusheng/starry/baidubaike/infobox.triple"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/starry/baidubaike/infobox.triple.raw.clean"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String leftName = null;
            String rightName = null;
            try {
                leftName = BkEntIdxReader.getName(Integer.parseInt(spt[0]));
                rightName = BkEntIdxReader.getName(Integer.parseInt(spt[2]));
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
        f = new File("/home/xusheng/pra/examples/graphs/baike/kb_svo/edge_dict.tsv");
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
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

    // generate edge.tsv
    public static String svoFile = "/home/xusheng/pra/examples/graphs/baike/kb_svo";
    public static void generateEdgeFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(svoFile + "/node_dict.tsv"));
        String line;
        Map<String, String> nodeMap = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            nodeMap.put(spt[1], spt[0]);
        }
        br.close();
        br = new BufferedReader(new FileReader(svoFile + "/edge_dict.tsv"));
        Map<String, String> edgeMap = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            edgeMap.put(spt[1], spt[0]);
        }
        br.close();
        br = new BufferedReader(new FileReader("/home/xusheng/pra/examples/relation_metadata/baike/labeled_edges.tsv"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(svoFile + "/graph_chi/edges.tsv"));
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (nodeMap.containsKey(spt[0]) && nodeMap.containsKey(spt[2]) && edgeMap.containsKey(spt[1])) {
                bw.write(nodeMap.get(spt[0]) + "\t" + edgeMap.get(spt[1]) + "\t" + nodeMap.get(spt[2]) + "\n");
            }
        }
        br.close();
        bw.close();
    }

    // change node_dict.tsv's format
    public static void reOrder() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(svoFile + "/node_dict.tsv"));
        String[] nodes = new String[3004119];
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            nodes[Integer.parseInt(spt[0])] = spt[1];
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(svoFile + "/node_dict.tsv.new"));
        for (int i=1; i<=3004118; i++) bw.write(i + "\t" + nodes[i] + "\n");
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        //RecoverInfoTriple();
        //generateDictFile();
        //generateEdgeFile();
        reOrder();
    }
}
