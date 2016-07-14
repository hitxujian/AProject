package xusheng.kg.baike.relation;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 7/13/2016.
 * Do the initial cleaning of baike relations,
 * which means simply cluster those relations with similar surface forms.
 * Not difficult but extremely complicated!
 */

public class InitialCleaner {
    public static String relFp = "/home/xusheng/starry/baidubaike";

    public static boolean isChinese(char c) {
        return c >= 0x4E00 &&  c <= 0x9FA5;
    }

    public static boolean isChinese(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c)) return true;
        }
        return false;
    }

    public static void removeMarks() throws IOException {
        File f = new File(relFp + "/edge_dict.tsv");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        f = new File(relFp + "/edge_dict.tsv.removal");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        String line;
        while ((line = br.readLine()) != null) {
            String idx = line.split("\t")[0];
            String rel = line.split("\t")[1];
            String newRel = "";
            for (char c: rel.toCharArray()) {
                if (isChinese(c)) newRel += c;
                if (newRel.equals("")) newRel = "EMPTY";
                bw.write(idx + "\t" + newRel + "\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("%s/edge_dict.tsv.removal written.", relFp);
    }

    public static void group() throws IOException {
        File f = new File(relFp + "/edge_dict.tsv.removal");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        int idx = 0;
        Map<String, String> newMap = new HashMap<>(), transformer = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (!newMap.containsKey(spt[1])) {
                idx ++;
                newMap.put(spt[1], String.valueOf(idx));
            }
            transformer.put(spt[0], newMap.get(spt[1]));
        }
        br.close();
        f = new File(relFp + "/edge_dict.tsv.v1");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        for (Map.Entry<String, String> entry: newMap.entrySet())
            bw.write(entry.getValue() + "\t" + entry.getKey() + "\n");
        bw.close();
        LogInfo.logs("%s/edge_dict.tsv.v1 written. size : %d", relFp, newMap.size());
        f = new File(relFp + "/edge_dict.tsv.v0Tv1");
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        for (Map.Entry<String, String> entry: transformer.entrySet())
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        bw.close();
        LogInfo.logs("%s/edge_dict.tsv.v0Tv1 written. size : %d", relFp, transformer.size());
    }

    public static void main(String[] args) throws IOException {
        removeMarks();
        group();
    }
}
