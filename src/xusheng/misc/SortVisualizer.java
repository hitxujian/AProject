package xusheng.misc;

import fig.basic.LogInfo;
import xusheng.util.struct.MapHelper;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Xusheng on 7/29/16.
 * Input: idxA idxB\tNumber
 * Output: Sort by number and change idx to name
 */

public class SortVisualizer {

    public static void sortAndShowRelName(String inFp, String outFp, String idxFp) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inFp));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFp));
        String line;
        HashMap<String, Double> map = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            map.put(spt[0], Double.parseDouble(spt[1]));
        }
        List<Map.Entry<String, Double>> sorted = MapHelper.sort(map);
        IndexNameReader inr = new IndexNameReader(idxFp);
        inr.initializeFromIdx2Name();
        for (int i=0; i<sorted.size(); i++) {
            String[] spt = sorted.get(i).getKey().split(" ");
            bw.write(inr.getName(Integer.parseInt(spt[0])) + " " +
                    inr.getName(Integer.parseInt(spt[1])) + "\t" +
                    sorted.get(i).getValue() + "\n");
        }
        bw.close();
        LogInfo.logs("Sort & Visualization %s done.", outFp);
    }

    public static void sortRet(String rootFp) throws IOException {
        SortVisualizer.sortAndShowRelName(rootFp + "/rel_cooccurR.txt",
                rootFp + "/rel_cooccurR.txt.vis", rootFp + "/edge_dict.tsv.v1");
        SortVisualizer.sortAndShowRelName(rootFp + "/rel_cooccurN.txt",
                rootFp + "/rel_cooccurN.txt.vis", rootFp + "/edge_dict.tsv.v1");
    }

    public static void main(String[] args) throws IOException {
        sortRet("/home/xusheng/starry/baidubaike");
    }
}
