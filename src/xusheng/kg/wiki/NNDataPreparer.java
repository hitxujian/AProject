package xusheng.kg.wiki;

import fig.basic.LogInfo;
import fig.basic.Pair;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.FixLenRankList;
import xusheng.util.struct.MultiThread;
import xusheng.util.struct.MultiThreadTemplate;
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

public class NNDataPreparer implements Runnable {
    public static String rootFp = "/home/xusheng";
    public static Map<String, String> vectors = null;
    public static Map<String, List<String>> anchorTextMap = null;
    public static List<String> data = null;
    public static Map<Integer, List<String>> trainData = null, testData = null;

    public static void getFullPositiveData() throws IOException {
        LogInfo.begin_track("Begin to get full positive data.");
        // load word2vec
        vectors = VecLoader.load(rootFp + "/word2vec/vec/wiki_link_" + String.valueOf(lenOfw2v) +".txt");
        // load clean wiki infobox data
        BufferedReader br = new BufferedReader(new FileReader(rootFp +
                "/nn/data/wikipedia/wiki_info_linked.tsv"));

        BufferedWriter bwp = new BufferedWriter(new FileWriter(rootFp +
                "/nn/data/wikipedia/positive_full.tsv.special"));

        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            try {
                String[] spt = line.split("\t");
                if (spt[0].equals(" ") || spt[3].equals(" "))
                    continue;
                // 4 elements
                String markedSubj = addMark(spt[0]);
                String markedObj = addMark(spt[3]);
                String rel_s[] = spt[1].split(" ");
                // note that here we only focus the w2v, so change to lower case!
                String obj_s[] = spt[3].toLowerCase().split(" "); // todo: change 3-> 2
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
                // todo: need to modify here!
                line = spt[0] + " " + spt[1] + " " + spt[3] + " " + spt[3];
                bwp.write(line + "\t\t" + newLine + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                LogInfo.logs("[error] %s", line.replace("\t", " \\t "));
            }
        }
        br.close();
        bwp.close();
        LogInfo.end_track();
    }

    public static List<Pair<Integer, Integer>> taskList = new ArrayList<>();
    public static void getTaskData() throws IOException {
        LogInfo.begin_track("Start to get training & testing data.");

        // load full positive data
        BufferedReader br = new BufferedReader(new FileReader(rootFp +
                "/nn/data/wikipedia/positive_full.tsv.special"));
        String line;
        data = new ArrayList<>();
        while ((line = br.readLine()) != null)
            data.add(line);
        LogInfo.logs("[log] %s loaded. Size: %d.", rootFp + "/nn/data/wikipedia/positive_full.tsv.special", data.size());

        // load anchor text data
        if (anchorTextMap == null)
            anchorTextMap = AnchorTextReader.ReadDataFromName2Ent("Prior");
        // load word2vec
        if (vectors == null)
            vectors = VecLoader.load(rootFp + "/word2vec/vec/wiki_link_" + String.valueOf(lenOfw2v) +".txt");

        int cnt = 0;
        LogInfo.logs("[log] sampling training/testing data.");
        Random rand = new Random();
        Set<Integer> set = new HashSet<>();

        int num = -1;
        try {
            cnt = 0;
            // pos:neg = 1:9, that's why /10!
            while (cnt < numOfTrain) {
                num = rand.nextInt(data.size());
                String[] part = data.get(num).split("\t\t");
                if (!set.contains(num)) {
                    int lenOfRel = part[0].split("\t")[1].split(" ").length;
                    if (setting.equals("single")) {
                        if (lenOfRel > 1) continue;
                    } else if (setting.equals("multi")) {
                        if (lenOfRel == 1) continue;
                    }
                    set.add(num);
                    cnt++;
                    taskList.add(new Pair<>(num, 1));
                }
            }
            LogInfo.logs("[log] %d training data add to task list.", cnt);

            cnt = 0;
            while (cnt < numOfTest) {
                num = rand.nextInt(data.size());
                String[] part = data.get(num).split("\t\t");
                if (!set.contains(num)) {
                    int lenOfRel = part[0].split("\t")[1].split(" ").length;
                    if (setting.equals("single")) {
                        if (lenOfRel > 1) continue;
                    } else if (setting.equals("multi")) {
                        if (lenOfRel == 1) continue;
                    }
                    set.add(num);
                    cnt++;
                    taskList.add(new Pair<>(num, 2));
                }
            }
            LogInfo.logs("[log] %d testing data add to task list.", cnt);
        } catch (Exception ex) {
            ex.printStackTrace();
            LogInfo.logs("[error] line %d in positive.full.tsv", num + 1);
        }
        LogInfo.end_track();
    }

    public static void getSingleTrainTestData(int idx) throws IOException {
        Pair<Integer, Integer> task = taskList.get(idx);
        int num = task.getFirst();
        String obj_s = data.get(num).split("\t\t")[0].split("\t")[2];
        String obj_l = data.get(num).split("\t\t")[0].split("\t")[3];

        if (task.getSecond() == 1) { // training data
            String[] spt = data.get(num).split("\t\t")[1].split("\t");
            String PosVec = spt[0] + " " + spt[1] + " " + spt[2] + " " + spt[3];
            LogInfo.logs("[log][%d] positive sample generated for training data [%s].", idx, data.get(num).split("\t\t")[0]);
            //bwnWrite(PosVec + " 1\n");
            // generate negative data
            Set<String> negObjs = getNegObj(idx, obj_s, obj_l);
            for (String negObj : negObjs)
                add2trainData(idx, PosVec + " " + negObj + " " + String.valueOf(num + 1) + "\n");
        } else { // testing data
            String[] spt = data.get(num).split("\t\t")[1].split("\t");
            String PosVec = spt[0] + " " + spt[1] + " " + spt[2] + " " + spt[3] + " " + String.valueOf(num + 1);
            add2testData(idx, PosVec + " 1\n");
            LogInfo.logs("[log][%d] positive sample generated for testing data [%s].", idx, data.get(num).split("\t\t")[0]);
            // generate negative data
            Set<String> negObjs = getNegObj(idx, obj_s, obj_l);
            for (String negObj : negObjs)
                add2testData(idx, spt[0] + " " + spt[1] + " " + spt[2] + " " + negObj + " 0\n");
        }
    }

    // get the negative object entity embeddings of one object surface form
    public static Set<String> getNegObj(int idx, String obj_s, String obj_l) {
        LogInfo.logs("[log][%d] get negative samples for %s... [%s].", idx, obj_s, new Date().toString());
        Set<String> ret = new HashSet<>();

        // exactly match
        if (anchorTextMap.containsKey(obj_s)) {
            List<String> entityList = anchorTextMap.get(obj_s);
            for (String str: entityList) {
                if (str.trim().length() == 0) continue;
                // attention here we need obj_l!!!
                if (!str.equals(obj_l) && vectors.containsKey(addMark(str)))
                    ret.add(vectors.get(addMark(str)) + " " + str.replace(" ", "_"));
                if (ret.size() == 9) {
                    LogInfo.logs("[log][%d] %d negative samples generated for %s. [%s]",
                            idx, ret.size(), obj_s, new Date().toString());
                    return ret;
                }
            }
        }

        // fuzzy match if num of negative samples are not enough
        FixLenRankList<List<String>, Double> rankList = new FixLenRankList<>(30);
        for (Map.Entry<String, List<String>> entry: anchorTextMap.entrySet()) {
            double score = getSimilarity(entry.getKey(), obj_s);
            rankList.insert(new Pair<>(entry.getValue(), score));
            //if (rankList.isFull() && rankList.getLastVal() > 0.0) break;
        }
        List<List<String>> retList = rankList.getList();
        for (int i=0; i<retList.size(); i++) {
            List<String> entityList = retList.get(i);
            for (String str: entityList) {
                if (str.trim().length() == 0) continue;
                // attention here we need obj_l!!!
                if (!str.equals(obj_l) && vectors.containsKey(addMark(str)))
                    ret.add(vectors.get(addMark(str)) + " " + str.replace(" ", "_"));
                if (ret.size() == 9) {
                    LogInfo.logs("[log][%d] %d negative samples generated for %s. [%s]",
                            idx, ret.size(), obj_s, new Date().toString());
                    return ret;
                }
            }
        }
        LogInfo.logs("[log][%d] %d negative samples generated for %s. [%s]",
                idx, ret.size(), obj_s, new Date().toString());
        return ret;
    }

    // calculate the similarity of two embedding vectors
    // todo: n-gram similarity will be better?
    public static double getSimilarity(String str1, String str2) {
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
        String[] spt1 = str1.split(" ");
        String[] spt2 = str2.split(" ");
        Set<String> set1 = new HashSet<>();
        for (String str: spt1) set1.add(str);
        Set<String> set2 = new HashSet<>();
        for (String str: spt2) set2.add(str);
        int intersect = 0;
        for (String str: set1)
            if (set2.contains(str))
                intersect ++;
        int union = set1.size() + set2.size() - intersect;
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

    // -------------------------------------------------------------------------

    public static int curr = -1, end = -1;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                getSingleTrainTestData(idx);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            return ret;
        }
        return -1;
    }

    public static synchronized void add2trainData(int idx, String line) {
        if (!trainData.containsKey(idx))
            trainData.put(idx, new ArrayList<>());
        trainData.get(idx).add(line);
    }

    public static synchronized void add2testData(int idx, String line) {
        if (!testData.containsKey(idx))
            testData.put(idx, new ArrayList<>());
        testData.get(idx).add(line);
    }

    public static void multiThreadWork() throws Exception{
        getTaskData();
        trainData = new HashMap<>();
        testData = new HashMap<>();
        curr = 0; end = taskList.size();
        int numOfThreads = 30;
        NNDataPreparer workThread = new NNDataPreparer();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running on %d tasks...", numOfThreads, taskList.size());
        multi.runMultiThread();
        writeRet();
        LogInfo.end_track();
    }

    public static void writeRet() throws IOException {
        File file = new File(rootFp + "/nn/data/margin/" + dir);
        if (!file.exists()) file.mkdir();
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp +
                "/nn/data/margin/" + dir + "/training_" + String.valueOf(numOfTrain) + ".tsv.full." + setting));
        for (int i=0; i<numOfTrain; i++)
            for (String line : trainData.get(i))
                bw.write(line);
        bw.close();
        bw = new BufferedWriter(new FileWriter(rootFp +
                "/nn/data/margin/" + dir + "/testing_" + String.valueOf(numOfTest) + ".tsv.full." + setting));
        for (int i=numOfTrain; i<taskList.size(); i++)
            for (String line : testData.get(i))
                bw.write(line);
        bw.close();
    }

    // -------------------------------------------------------------------------

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
                                //LogInfo.logs(subj + "\t" + rel + "\t" + obj);
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
                                //ex.printStackTrace();
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

    public static int lenOfw2v = 50, numOfTrain = 5000, numOfTest = 2000;
    public static String setting = null, dir = null;
    public static void main(String[] args) throws Exception {
        //getCleanInfoboxFromWikipedia();
        lenOfw2v = Integer.parseInt(args[0]);
        numOfTrain = Integer.parseInt(args[1]);
        numOfTest = Integer.parseInt(args[2]);
        setting = args[3];
        dir= args[4];
        if (args[5].equals("1")) getFullPositiveData();
        multiThreadWork();
    }
}
