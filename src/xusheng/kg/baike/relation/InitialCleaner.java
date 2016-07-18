package xusheng.kg.baike.relation;

import fig.basic.LogInfo;
import xusheng.kg.baike.BkRelIdxReader;
import xusheng.misc.IndexNameReader;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public static void generateEdgeDict() throws IOException {
        File f = new File(relFp + "/infobox.url");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        f = new File(relFp + "/edge_dict.tsv");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        String line;
        int idx = 0;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rel = spt[1];
            if (rel.equals("")) rel = "EMPTY";
            if (!set.contains(rel)) {
                idx ++;
                set.add(rel);
                bw.write(idx + "\t" + rel + "\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("edge_dict.tsv is generated. Size: %d", set.size());
    }

    // remove all non-chinese symbols
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
            if (rel.equals("EMPTY"))
                newRel = rel;
            else for (char c: rel.toCharArray()) {
                if (isChinese(c)) newRel += c;
                if (newRel.equals("")) newRel = "EMPTY";
            }
            bw.write(idx + "\t" + newRel + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("%s/edge_dict.tsv.removal written.", relFp);
    }

    // group relations with same surface form, --> get a new index map and transform map
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

    // get infobox.text.v1 from infobox.text
    public static void transformInfoboxText() throws IOException {
        IndexNameReader inr_0 = new IndexNameReader(relFp + "/edge_dict.tsv");
        IndexNameReader inr_x = new IndexNameReader(relFp + "/edge_dict.tsv.v0Tv1");
        inr_0.initializeFromName2Idx();
        inr_x.initializeFromIdx2Name();
        BufferedReader br = new BufferedReader(new FileReader(relFp + "/infobox.text"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(relFp + "/infobox.text.v1"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (spt.length < 3) continue;
            String subj = getChinese(spt[0]);
            String obj = getChinese(spt[2]);
            if (subj.equals("") || obj.equals("")) continue;
            if (spt[1].equals("")) spt[1] = "EMPTY";
            Integer idx = inr_0.getIdx(spt[1]);
            if (idx == null) LogInfo.logs(spt[1]);
            String name = inr_x.getName(idx);
            if (name == null) LogInfo.logs(idx + "\t" + spt[1]);
            bw.write(subj + "\t" + name
                        + "\t" + obj + "\n");
        }
        bw.close();
        LogInfo.logs("infobox.text.v1 written.");
    }

    public static String getChinese(String str) {
        String ret = "";
        for (char ch: str.toCharArray()) {
            if (isChinese(ch)) ret += ch;
        }
        return ret;
    }

    public static void main(String[] args) throws IOException {
        generateEdgeDict();
        removeMarks();
        group();
        transformInfoboxText();
    }
}
