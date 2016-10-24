package xusheng.kg.wiki;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.FixLenRankList;
import xusheng.word2vec.VecLoader;
import xusheng.word2vec.WordEmbedder;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                "/nn/data/wikipedia/wiki_info_linked.tsv"));

        BufferedWriter bwp = new BufferedWriter(new FileWriter(rootFp +
                "/nn/data/wikipedia/positive_full.tsv"));

        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            try {
                String[] spt = line.split("\t");
                // 4 elements
                String markedSubj = addMark(spt[0]);
                String markedObj = addMark(spt[3]);
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
                "/nn/data/positive_full.tsv"));
        String line;
        while ((line = br.readLine()) != null)
            data.add(line);
        LogInfo.logs("[log] %s loaded. Size: %d.", rootFp + "/nn/data/positive_full.tsv", data.size());

        // load anchor text data
        if (anchorTextMap == null)
            anchorTextMap = AnchorTextReader.ReadDataFromName2Ent();
        // load word2vec
        if (vectors == null)
            vectors = VecLoader.load(rootFp + "/word2vec/vec/wiki_link_" + String.valueOf(lenOfw2v) +".txt");

        BufferedWriter bwn = new BufferedWriter(new FileWriter(rootFp +
                "/nn/data/margin/training_" + String.valueOf(numOfTrain) + ".tsv"));
        BufferedWriter bwt = new BufferedWriter(new FileWriter(rootFp +
                "/nn/data/margin/testing_" + String.valueOf(numOfTest) + ".tsv.full"));

        int cnt = 0;
        LogInfo.logs("[log] sampling training/testing data.");
        Random rand = new Random();
        Set<Integer> set = new HashSet<>();

        cnt = 0;
        // pos:neg = 1:9, that's why /10!
        while (cnt < numOfTrain/10) {
            int num = rand.nextInt(data.size());
            if (!set.contains(num)) {
                LogInfo.logs("[log] get positive sample [%s].", data.get(num).split("\t\t")[0]);
                set.add(num);
                cnt ++;
                String[] spt = data.get(num).split("\t\t")[1].split("\t");
                String PosVec = spt[0] + " " + spt[1] + " " + spt[2] + " " + spt[3];
                //bwn.write(PosVec + " 1\n");
                // generate negative data
                String obj_s =data.get(num).split("\t\t")[0].split("\t")[2];
                Set<String> negObjs = getNegObj(obj_s);
                for (String negObj: negObjs)
                    bwn.write(PosVec  + " " + negObj + " " + String.valueOf(num+1) + "\n");
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
                String PosVec = spt[0] + " " + spt[1] + " " + spt[2] + " " + spt[3] + " " + String.valueOf(num+1);
                bwt.write(PosVec + " 1\n");
                // generate negative data
                String obj_s =data.get(num).split("\t\t")[0].split("\t")[2];
                Set<String> negObjs = getNegObj(obj_s);
                for (String negObj: negObjs)
                    bwt.write(spt[0] + " " + spt[1] + " " + spt[2] + " " + negObj + " 0\n");
            }
        }
        LogInfo.logs("[log] testing data generated.");

        //bwn.close();
        bwt.close();
        LogInfo.end_track();
    }

    // get the negative object entity embeddings of one object surface form
    // todo: to use the prior file
    public static Set<String> getNegObj(String obj_s) {
        LogInfo.logs("[log] get negative samples for %s... [%s].", obj_s, new Date().toString());
        FixLenRankList<Set<String>, Double> rankList = new FixLenRankList<>(30);
        for (Map.Entry<String, Set<String>> entry: anchorTextMap.entrySet()) {
            double score = getSimilarity(entry.getKey(), obj_s);
            rankList.insert(new Pair<>(entry.getValue(), score));
            //if (rankList.isFull() && rankList.getLastVal() > 0.0) break;
        }
        List<Set<String>> retList = rankList.getList();
        Set<String> ret = new HashSet<>();
        boolean flag = true;
        for (int i=0; i<retList.size(); i++) {
            Set<String> set = retList.get(i);
            for (String str: set) {
                if (!str.equals(obj_s) && vectors.containsKey(addMark(str)))
                    ret.add(vectors.get(addMark(str)) + " " + str.replace(" ", "_"));
                if (ret.size() == 9) {
                    flag = false;
                    break;
                }
            }
            if (!flag) break;
        }
        LogInfo.logs("[log] negative samples generated for %s. [%s]", obj_s, new Date().toString());
        return ret;
    }

    // calculate the similarity of two embedding vectors
    // todo: n-gram similarity will be better?
    public static double getSimilarity(String str1, String str2) {
        String[] spt1 = str1.split(" ");
        String[] spt2 = str2.split(" ");
        Set<String> set1 = new HashSet<>();
        for (String str: spt1) set1.add(str);
        Set<String> set2 = new HashSet<>();
        for (String str: spt2) set2.add(str);
        int intersect = 0;
        int union = 0;
        for (String str: set1)
            if (set2.contains(str))
                intersect ++;
        union = set1.size() + set2.size() - intersect;
        double score = (double) intersect / union;
        return score;
    }

    // simply average embeddings of relation/object surface form words
    // to get a representative vector
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

    public static void getCleanInfoboxFromDbpedia() throws IOException {
        LogInfo.begin_track("Begin to get clean wiki infobox data.");
        File f = new File(rootFp + "/dbpedia/infobox_properties_en.ttl");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        f = new File(rootFp + "/nn/data/dbpedia/wiki_info_linked.tsv");
        BufferedWriter bwl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        f = new File(rootFp + "/nn/data/dbpedia/wiki_info_unlinked.tsv");
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

    // todo: extract infobox information from raw wiki xml.
    public static void getCleanInfoboxFromWikipedia() throws IOException {
        LogInfo.begin_track("Begin to get clean wiki infobox data.");
        File f = new File(rootFp + "/wikipedia/enwiki-20160920-pages-articles-multistream.xml");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        f = new File(rootFp + "/nn/data/wikipedia/wiki_info_linked.tsv");
        BufferedWriter bwl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        f = new File(rootFp + "/nn/data/wikipedia/wiki_info_unlinked.tsv");
        BufferedWriter bwu = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 1000000);
            line = line.trim();
            if (line.startsWith("<title>")) {
                String subj = line.split(">")[1].split("<")[0].toLowerCase();
                while (!(line = br.readLine()).trim().startsWith("</page>"))
                    if (line.trim().startsWith("{{Infobox")) {
                        while (!(line = br.readLine()).trim().startsWith("}}")) {
                            try {
                                String[] spt = line.split("=");
                                String rel = removeOthers(spt[0].trim().toLowerCase());
                                String obj = spt[1].trim();
                                // if has links
                                LogInfo.logs(subj + "\t" + rel + "\t" + obj);
                                Pattern pattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
                                Matcher matcher = pattern.matcher(obj);
                                boolean flag = true;
                                while (matcher.find()) {
                                    flag = false;
                                    String raw = matcher.group(1);
                                    String entity = raw.toLowerCase();
                                    String mention = raw; // maintain original case
                                    String[] sptt = raw.split("\\|");
                                    if (sptt.length > 1) {
                                        entity = sptt[0].toLowerCase();
                                        mention = sptt[1];
                                    }
                                    bwl.write(subj + "\t" + rel + "\t" + mention + "\t" + entity + "\n");
                                }
                                // no links in obj
                                if (flag)
                                    bwu.write(subj + "\t" + rel + "\t" + obj + "\n");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                LogInfo.logs("[error] %s", line);
                            }
                        }
                    }
            }
        }
        br.close();
        bwl.close();
        bwu.close();
    }

    public static String removeOthers(String str) {
        int i = str.length() - 1;
        while (str.charAt(i) >= '0' && str.charAt(i) <= '9')
            i --;
        i++;
        int j=0;
        while (str.charAt(j) < 'a' || str.charAt(j) > 'z')
            j ++;
        return str.substring(j, i);
    }

    public static int lenOfw2v = 50;
    public static void main(String[] args) throws IOException {
        lenOfw2v = Integer.parseInt(args[0]);
        getCleanInfoboxFromWikipedia();
        getFullPositiveData();
        //getTrainTestData(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
}
