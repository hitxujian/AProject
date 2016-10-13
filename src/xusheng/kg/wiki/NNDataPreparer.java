package xusheng.kg.wiki;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.FixLenRankList;
import xusheng.word2vec.VecLoader;
import xusheng.word2vec.WordEmbedder;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 10/8/2016.
 * Prepare training/testing data for NN
 * Input: wiki_infobox data
 */

public class NNDataPreparer {
    public static String rootFp = "/home/xusheng";
    public static Map<String, String> vectors = null;
    public static Map<String, Set<String>> anchorTextMap = null;

    public static void getFullPositiveData() throws IOException {
        LogInfo.begin_track("Begin to get full positive data.");
        // load word2vec
        vectors = VecLoader.load(rootFp + "/word2vec/vec/wiki_link_" + String.valueOf(lenOfw2v) +".txt");
        // load clean wiki infobox data
        BufferedReader br = new BufferedReader(new FileReader(rootFp +
                "/nn/wiki_info_linked.tsv"));

        BufferedWriter bwp = new BufferedWriter(new FileWriter(rootFp +
                "/nn/positive_full.tsv"));

        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            try {
                String[] spt = line.split("\t");
                // 4 elements
                String markedSubj = addMark(spt[0]);
                String markedObj = addMark(spt[2]);
                String rel_s[] = spt[1].split(" ");
                String obj_s[] = spt[2].split(" ");
                // check if w2v contains these 4 elements
                // check rel_s
                boolean flag = true;
                for (String str : rel_s) {
                    if (!vectors.containsKey(str)) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) continue;
                // check obj_s
                flag = true;
                for (String str : obj_s)
                    if (!vectors.containsKey(str)) {
                        flag = false;
                        break;
                    }
                if (!flag) continue;
                // check subj_l & obj_l
                if (!vectors.containsKey(markedObj) || !vectors.containsKey(markedSubj))
                    continue;
                // get the final vectors
                String newLine = "";

                newLine += (vectors.get(markedSubj) + "\t" + average(rel_s) + "\t" + average(obj_s) + "\t" +
                        (vectors.get(markedObj)));

                // format: triple\t\t\vec
                bwp.write(line + "\t\t" + newLine + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                LogInfo.logs("[error] %s", line);
            }
        }
        br.close();
        bwp.close();
        LogInfo.end_track();
    }

    public static void getTrainTestData(int numOfTrain, int numOfTest) throws IOException {
        LogInfo.begin_track("Start to get training & testing data.");

        // load full positive data
        List<String> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(rootFp +
                "/nn/positive_full.tsv"));
        String line;
        while ((line = br.readLine()) != null)
            data.add(line);
        LogInfo.logs("[log] %s loaded. Size: %d.", rootFp + "/nn/positive_full.tsv", data.size());

        // load anchor text data
        if (anchorTextMap == null)
            anchorTextMap = AnchorTextReader.ReadDataFromName2Ent();
        // load word2vec
        if (vectors == null)
            vectors = VecLoader.load(rootFp + "/word2vec/vec/wiki_link_" + String.valueOf(lenOfw2v) +".txt");

        BufferedWriter bwn = new BufferedWriter(new FileWriter(rootFp +
                "/nn/training_" + String.valueOf(numOfTrain) + ".tsv"));
        BufferedWriter bwt = new BufferedWriter(new FileWriter(rootFp +
                "/nn/testing_" + String.valueOf(numOfTest) + ".tsv"));

        int cnt = 0;
        LogInfo.logs("[log] sampling training data.");
        Random rand = new Random();
        Set<Integer> set = new HashSet<>();
        cnt = 0;
        // pos:neg = 1:9, that's why /10!
        while (cnt < numOfTrain/10) {
            int num = rand.nextInt(data.size());
            if (!set.contains(num)) {
                set.add(num);
                cnt ++;
                String[] spt = data.get(num).split("\t\t")[1].split("\t");
                String PosVec = spt[0] + " " + spt[1] + " " + spt[2] + " " + spt[3];
                bwn.write(PosVec + " 1");
                // generate negative data
                String obj_s =data.get(num).split("\t\t")[0].split("\t")[2];
                Set<String> negObjs = getNegObj(obj_s);
                for (String negObj: negObjs)
                    bwn.write(spt[0] + " " + spt[1] + " " + spt[2]  + " " + negObj + " 0");
            }
        }
        LogInfo.logs("[log] training data generated.");
        cnt = 0;
        while (cnt < numOfTest/10) {
            int num = rand.nextInt(data.size());
            if (!set.contains(num)) {
                set.add(num);
                cnt ++;
                String[] spt = data.get(num).split("\t\t")[1].split("\t");
                String PosVec = spt[0] + " " + spt[1] + " " + spt[2] + " " + spt[3];
                bwt.write(PosVec + " 1");
                // generate negative data
                String obj_s =data.get(num).split("\t\t")[0].split("\t")[2];
                Set<String> negObjs = getNegObj(obj_s);
                for (String negObj: negObjs)
                    bwt.write(spt[0] + " " + spt[1] + " " + spt[2]  + " " + negObj + " 0");
            }
        }
        LogInfo.logs("[log] testing data generated.");

        bwn.close();
        bwt.close();
        LogInfo.end_track();
    }

    public static Set<String> getNegObj(String obj_s) {
        FixLenRankList<Set<String>, Double> rankList = new FixLenRankList<>(30);
        for (Map.Entry<String, Set<String>> entry: anchorTextMap.entrySet()) {
            double score = getSimilarity(entry.getKey(), obj_s);
            rankList.insert(new Pair<>(entry.getValue(), score));
        }
        List<Set<String>> retList = rankList.getList();
        Set<String> ret = new HashSet<>();
        boolean flag = true;
        for (int i=0; i<retList.size(); i++) {
            Set<String> set = retList.get(i);
            for (String str: set) {
                if (!str.equals(obj_s) && vectors.containsKey(addMark(str)))
                    ret.add(vectors.get(addMark(str)));
                if (ret.size() == 10) {
                    flag = false;
                    break;
                }
            }
            if (!flag) break;
        }
        return ret;
    }

    public static double getSimilarity(String str1, String str2) {
        String[] spt1 = str1.split(" ");
        String[] spt2 = str2.split(" ");
        Set<String> set1 = new HashSet<>(spt1);
        Set<String> set2 = new HashSet<>(spt2);
        int intersect = 0;
        int union = 0;
        for (String str: set1)
            if (set2.contains(str))
                intersect ++;
        union = set1.size() + set2.size() - intersect;
        double score = (double) intersect / union;
        return score;
    }

    public static String average(String[] spt) {
        double[] vec = new double[lenOfw2v];
        for (String str: spt) {
            String[] sptt = vectors.get(str).split(" ");
            for (int i=0; i<sptt.length; i++)
                vec[i] += Double.parseDouble(sptt[i]);
        }
        for (int i=0; i<vec.length; i++)
            vec[i] /= spt.length;
        String ret = String.format("%.6f", vec[0]);
        for (int i=1; i<vec.length; i++)
            ret += " " + String.format("%.6f", vec[i]);
        return ret;
    }

    public static String addMark(String entity) {
        String tmp = entity.replace(" ", "");
        String mark = String.valueOf(tmp.charAt(0));
        for (int i=1; i<tmp.length(); i++)
            mark += ("_" + tmp.charAt(i));
        return String.format("[[%s]]", mark);
    }


    public static void getCleanData() throws IOException {
        LogInfo.begin_track("Begin to get clean wiki infobox data.");
        File f = new File(rootFp + "/dbpedia/infobox_properties_en.ttl");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        f = new File(rootFp + "/nn/wiki_info_linked.tsv");
        BufferedWriter bwl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        f = new File(rootFp + "/nn/wiki_info_unlinked.tsv");
        BufferedWriter bwu = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));

        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 1000000);
            try {
                String[] spt = line.split(" ");
                String subj = spt[0].split("resource/")[1].split(">")[0].replace("_", " ").toLowerCase();
                String rawRelation = spt[1].split("property/")[1].split(">")[0].replace("_", " ");
                String relation = "";
                for (int i=0; i<rawRelation.length(); i++) {
                    if (rawRelation.charAt(i) <= 'Z')
                        relation += " " + String.valueOf(rawRelation.charAt(i)).toLowerCase();
                    else relation += String.valueOf(rawRelation.charAt(i));
                }
                String obj_s;
                if (spt[2].contains("dbpedia.org/resource")) {
                    obj_s = spt[2].split("resource/")[1].split(">")[0].replace("_", " ").toLowerCase();
                    bwl.write(String.format("%s\t%s\t%s\n", subj, relation, obj_s));
                }
                else{
                    obj_s = spt[2];
                    if (obj_s.equals("\""))
                        obj_s = spt[2] + spt[3];
                    bwu.write(String.format("%s\t%s\t%s\n", subj, relation, obj_s));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                LogInfo.logs("[error] %s", line);
            }
        }
        br.close();
        bwl.close();
        bwu.close();
        LogInfo.end_track();
    }



    public static int lenOfw2v = 50;
    public static void main(String[] args) throws IOException {
        lenOfw2v = Integer.parseInt(args[0]);
        //getCleanData();
        //getFullPositiveData();
        getTrainTestData(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
}
