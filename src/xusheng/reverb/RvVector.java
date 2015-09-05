package xusheng.reverb;


import fig.basic.LogInfo;
import kangqi.util.struct.MapDoubleHelper;
import kangqi.util.struct.MapIntHelper;
import xusheng.util.log.LogUpgrader;
import xusheng.freebase.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by angrymidiao on 4/24/15;
 * To group reverb predicates into synsets in semantical level;
 */

public class RvVector {

    public static HashMap<String, ArrayList<String>> equalset =new HashMap<>();
    public static HashMap<String, RvTuple> RvTuples = new HashMap<>();

    public static boolean verbose = true;

    // detele non-english characters
    public static String smallProcess(String line) {
        String[] spt = line.split(" ");
        for (int i=0; i<spt.length; i++) {
            String word = spt[i];
            if (word.equals("@")) word = "at";
            String newWord = "";
            for (int j=0; j<word.length(); j++) {
                if (word.charAt(j) <= 'z' && word.charAt(j) >= 'a' || word.charAt(j) == '\'')
                    newWord += word.charAt(j);
            }
            spt[i] = newWord;
        }
        String newLine = "";
        for (int i=0; i<spt.length; i++) {
            if (!spt[i].equals(""))
                newLine += spt[i] + " ";
        }
        newLine = newLine.trim();
        return newLine;
    }

    // delete non-english characters, transformed ReVerb, 22w edition
    public static void processWTF(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        ArrayList<String> newFile = new ArrayList<>();
        String line = "";
        while ((line=br.readLine()) != null) {
            String newLine = smallProcess(line);
            newFile.add(newLine);
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (String str: newFile) {
            bw.write(str + "\n");
        }
        bw.close();
        System.out.println("new transformed file done");
    }

    // extract predicates only from RvTable, 11w edition
    public static void extractRvTable(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        while ((line=br.readLine()) != null) {
            if (line.startsWith("#Pred")) {
                String predicate = smallProcess(line.split("\t")[2]);
                bw.write(predicate + "\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("relations.txt generated!");
    }

    // group all reverb patterns into synsets at syntactical level, 22w edition
    public static void groupRelation(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        int num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            line = line.trim();
            if (equalset.containsKey(line))
                equalset.get(line).add(String.valueOf(num));
            else {
                ArrayList<String> tmp = new ArrayList<>();
                tmp.add(String.valueOf(num));
                equalset.put(line, tmp);
            }
        }

        System.out.printf("write equalset into file, size of groups: %d\n", equalset.size());
        for (String rel : equalset.keySet()) {
            ArrayList<String> tmp = equalset.get(rel);
            bw.write(rel);
            for (String index : tmp) bw.write("\t" + index);
            bw.write("\n");
        }
        br.close();
        bw.close();
    }

    // read relations and supports, write relation-supports file
    public static void groupRelation(String file1, String file2, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file1));
        String line = ""; int identical = 0;
        HashMap<String, ArrayList<String>> rel = new HashMap<>();
        while ((line=br.readLine()) != null) {
            if (rel.containsKey(line)) identical ++;
            else rel.put(line, new ArrayList<String>());
        }
        br.close();

        LogInfo.logs("identical relation: %d", identical);

        br = new BufferedReader(new FileReader(file2));
        int idx = 0;
        while ((line=br.readLine()) != null) {
            idx ++;
            if (rel.containsKey(line))
                rel.get(line).add(String.valueOf(idx));
        }

        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        for (Map.Entry<String, ArrayList<String>> entry: rel.entrySet()) {
            bw.write(entry.getKey());
            for (String supp: entry.getValue())
                bw.write("\t" + supp);
            bw.write("\n");
        }
        bw.close();

        LogInfo.logs("relation-supports.txt generated!");
    }

    // process rvtable into equalset format, via type schema, 11w edition
    public static void processRvTable(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashMap<String, Double> pairs = new HashMap<>();
        String line = "", predicate = "";
        if (verbose) LogInfo.logs("Start process RvTable...");
        while ((line=br.readLine()) != null) {
            if (line.startsWith("#Pred")) {
                predicate = line.split("\t")[2];
                pairs.clear();
                continue;
            }
            if (line.startsWith("##")) {
                ArrayList<Map.Entry<String, Double>> sorted = new MapDoubleHelper<String>().sort(pairs);
                bw.write(predicate);
                for (Map.Entry<String, Double> entry: sorted) {
                    String tmp = entry.getKey();
                    String arg1 = TypeIndex.getType(String.valueOf(1 + Integer.valueOf(tmp) / 1979));
                    String arg2 = TypeIndex.getType(String.valueOf(1 + Integer.valueOf(tmp) % 1979));
                    bw.write("\t" + arg1 + " " + arg2);
                }
                bw.write("\n");
                continue;
            }
            String[] spt = line.split("\t");
            pairs.put(spt[0], Double.valueOf(spt[1]));
        }
        br.close();
        bw.close();
    }


    // read 3M ReVerb source file into memory
    public static void read3M(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        int num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            RvTuple rt = new RvTuple(line, String.valueOf(num));
            RvTuples.put(String.valueOf(num), rt);
        }
        br.close();
        if (verbose) LogInfo.logs("ReVerb 3M file read into memory!");
    }

    // read 3M ReVerb entity linking file into memory
    public static void read3MLinking(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line=br.readLine()) != null) {
            RvTuple rt = new RvTuple(line);
            RvTuples.put(rt.idx, rt);
        }
        br.close();
        if (verbose) LogInfo.logs("ReVerb simple linking read into memory!");
    }

    // show arguments on both sides for an input relation phrase
    public static void showArgs(String rel) {
        String idx = RvEqualSet.getIdx(rel);
        ArrayList<String> support = RvEqualSet.getSupport(idx);
        for (String str: support) {
            System.out.println(RvTuples.get(str).arg1 + " ||| " + RvTuples.get(str).arg2);
        }
    }

    // get vector for a relation phrase
    public static HashMap<String, Double> getVector(String idx, boolean left) {
        ArrayList<String> support = RvEqualSet.getSupport(idx);
        HashMap<String, Integer> vec = new HashMap<>();
        if (verbose) LogInfo.begin_track("get vector for \"%s\"", RvEqualSet.getName(idx));
        for (int i = 0; i < RvEqualSet.threshold; i++) {
            String pair = support.get(i);
            String type, word;
            int tmp;
            if (left) type = pair.split(" ")[0];
            else type = pair.split(" ")[1];
            word = type.split("\\.")[0];
            if (vec.containsKey(word)) tmp = vec.get(word) + 1;
            else tmp = 1;
            vec.put(word, tmp);
            word = type.split("\\.")[1];
            if (vec.containsKey(word)) tmp = vec.get(word) + 1;
            else tmp = 1;
            vec.put(word, tmp);
        }
        LogInfo.logs("size of vector: %d", vec.size());
        ArrayList<Map.Entry<String, Integer>> sorted = new MapIntHelper<String>().sort(vec);
        HashMap<String, Double> res = new HashMap<String, Double>();
        int sum = 0;
        for (Map.Entry<String, Integer> entry : sorted) sum += entry.getValue() * entry.getValue();
        double qsum = Math.sqrt(sum);
        for (Map.Entry<String, Integer> entry : sorted)
            res.put(entry.getKey(), (double) entry.getValue() / qsum);
        LogInfo.end_track();
    	return res;
    }

    // extract feature vector for each equalset and print to a file
    public static void vectorize_1(String outFile) throws Exception{
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        int size = RvEqualSet.withEnoughSupp.size();
        if (verbose) LogInfo.logs("total equalsets with supports > %d: %d", RvEqualSet.threshold, size);
        for (int i=0; i<size; i++) {
        	String idx = RvEqualSet.withEnoughSupp.get(i);
        	HashMap<String, Double> leftVec = getVector(idx, true);
            HashMap<String, Double> rightVec =getVector(idx, false);
            bw.write(idx);
            bw.write("\nleft");
            for (Map.Entry<String, Double> entry: leftVec.entrySet())
                bw .write("\t" + entry.getKey() + " " + entry.getValue());
            bw.write("\nright");
            for (Map.Entry<String, Double> entry: rightVec.entrySet())
                bw.write("\t" + entry.getKey() + " " + entry.getValue());
            bw.write("\n");
        }
        bw.close();
    }

    // new version, freebase based
    public static void vectorize(String outFile) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        for (int i=1; i<= RvEqualSet.size; i++) {
            LogInfo.begin_track("%d. relation: %s", i, RvEqualSet.getName(String.valueOf(i)));
            HashMap<String, Integer> vector = new HashMap<>();
            ArrayList<String> supp = RvEqualSet.getSupport(String.valueOf(i));
            for (String str: supp) {
                if (RvTuples.containsKey(str)) {

                    String lidx = RvTuples.get(str).arg1;
                    String ridx = RvTuples.get(str).arg2;
                    String lmid = EntityIndex.getMid(lidx);
                    String rmid = EntityIndex.getMid(ridx);
                    HashSet<String> fbMatchs = FbTuples.related(lmid, rmid);
                    if (fbMatchs != null) {
                        for (String fbpred: fbMatchs) {
                            if (vector.containsKey(fbpred)) {
                                int cnt = vector.get(fbpred) + 1;
                                vector.put(fbpred, cnt);
                            } else
                                vector.put(fbpred, 1);
                        }
                    }
                }
            }
            LogInfo.logs("vector size: %d", vector.size());
            bw.write(String.valueOf(i) + "\t");
            for (Map.Entry<String, Integer> entry: vector.entrySet())
                bw.write("\t" + entry.getKey() + " " + entry.getValue());
            bw.write("\n");
            LogInfo.end_track();
        }
        bw.close();
    }

    // check arguments mapping to freebase
    public static void showNamesForVec(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

        String line = "";
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            bw.write("#" + RvEqualSet.getName(spt[0]) + "#");
            HashMap<String, Integer> tmp = new HashMap<>();
            for (int i=1; i<spt.length; i++) {
                String[] str = spt[i].split(" ");
                tmp.put(str[0], Integer.parseInt(str[1]));
            }
            ArrayList<Map.Entry<String, Integer>> sorted = new MapIntHelper<String>().sort(tmp);
            for (Map.Entry<String, Integer> entry: sorted)
                bw.write("\t" + PredicateIndex.getPre(entry.getKey()) + " " + entry.getValue());
            bw.write("\n");
        }
        br.close();
        bw.close();
    }

    public static void newVectorize(String _1hubFile, String _2hubFile, String outFile, String logFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(_1hubFile));
        HashMap<String, ArrayList<String>> instanceMap = new HashMap<>();
        String line = "";
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            instanceMap.put(spt[0], new ArrayList<String>());
            for (int i=1; i<spt.length; i++)
                instanceMap.get(spt[0]).add(HopsIndex.getIdx(spt[i]));
        }
        br.close();
        br = new BufferedReader(new FileReader(_2hubFile));
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (!instanceMap.containsKey(spt[0])) instanceMap.put(spt[0], new ArrayList<String>());
            for (int i=1; i<spt.length; i++)
                instanceMap.get(spt[0]).add(HopsIndex.getIdx(spt[i]));
        }
        br.close();
        LogInfo.logs("ReVerb instances map read into memory!");

        LogInfo.begin_track("Start to generate vectors...");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        BufferedWriter bl = new BufferedWriter(new FileWriter(logFile));
        for (int i=1; i<= RvEqualSet.size; i++) {
            ArrayList<String> supps = RvEqualSet.getSupport(String.valueOf(i));
            HashMap<String, Integer> cnt = new HashMap<>();
            for (String supp: supps)
                if (instanceMap.containsKey(supp)) {
                    ArrayList<String> preds = instanceMap.get(supp);
                    for (String pred: preds)
                        if (cnt.containsKey(pred)) {
                            int tmp = cnt.get(pred) + 1;
                            cnt.put(pred, tmp);
                        } else
                            cnt.put(pred, 1);
                }
            LogUpgrader.showLine(i, 100);
            if (cnt.size() == 0) continue;
            double sum = 0;
            for (Map.Entry<String, Integer> entry: cnt.entrySet())
                sum += (double) entry.getValue() * (double) entry.getValue();
            sum = Math.sqrt(sum);


            bl.write(String.valueOf(i));
            double[] vec = new double[HopsIndex.size+1];
            for (Map.Entry<String, Integer> entry: cnt.entrySet()) {
                int idx = Integer.parseInt(entry.getKey());
                vec[idx] = (double) entry.getValue() / sum;
                bl.write("\t" + entry.getKey() + " " + entry.getValue());
            }
            bl.write("\n");

            bw.write(String.valueOf(i));
            for (int idx=1; idx<= HopsIndex.size; idx++)
                bw.write("\t" + vec[idx]);
            bw.write("\n");
        }
        LogInfo.end_track();
        bl.close();
        bw.close();
    }

    public static void main(String [] args) throws Exception{
        //@ option 1: process raw 22w relations:
        //input: transformed-reverb.txt, output: equal set \t support tuple1 \t 2....
        //processWTF(args[0]);
        //groupRelation(args[0], args[1]);

        //@ option 2: process RvTable with 11w relations using type schema:
        //TypeIndex.initialize(args[2]);
        //processRvTable(args[0], args[1]);

        //step 1: process RvTable and WTF file to get a relation-supports file
        //extractRvTable(args[0], args[1]);
        //sgroupRelation(args[1], args[2], args[3]);

    	//step 2: get vectors for each equal set and calculate the vectors
        RvEqualSet.initializeFor3M(args[3]);
        //read3MLinking(args[4]);
        //EntityIndex.initialize(args[5]);
        //PredicateIndex.initialize(args[6]);
        //FbTuples.initialize(args[7]);
        //vectorize(args[8]);
        //showNamesForVec(args[9], args[10]);
        HopsIndex.initialize(args[11]);
        newVectorize(args[12], args[13], args[14], args[15]);

        //step 3: calculate similarities according to vectors
        //move to class RvScorer4Name
    }

}
