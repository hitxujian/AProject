package xusheng.reverb;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by angrymidiao on 4/26/15.
 */
public class RvEqualSet {

    public static HashMap<String, String> idxToName = new HashMap<>();
    public static HashMap<String, String> nameToIdx = new HashMap<>();
    public static HashMap<String, ArrayList<String>> support = new HashMap<>();
    public static ArrayList<String> withEnoughSupp = new ArrayList<>();
    public static int size, threshold = 0;
    public static boolean verbose = true;
    
    public static String getIdx(String name) {
        if (nameToIdx.containsKey(name)) 
        	return nameToIdx.get(name);
        return null;
    }

    public static String getName(String idx) {
    	if (idxToName.containsKey(idx))
    		return idxToName.get(idx);
    	return null;
    }

    public static ArrayList<String> getSupport(String idx) {
    	if (support.containsKey(idx))
    		return support.get(idx);
    	return null;
    }

    public static void filter(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            String idx = spt[0];
            if (getSupport(idx).size() >= 100) bw.write(line + "\n");
        }
        br.close();
        bw.close();
    }

    public static void initializeFor3M(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        int num = 0, supp = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            String[] spt = line.split("\t");
            idxToName.put(String.valueOf(num), spt[0]);
            nameToIdx.put(spt[0], String.valueOf(num));
            ArrayList<String> tmp = new ArrayList<>();
            for (int i=1; i<spt.length; i++)
                tmp.add(spt[i]);
            supp += tmp.size();
            support.put(String.valueOf(num), tmp);
            //if (tmp.size() > threshold)
            //	withEnoughSupp.add(String.valueOf(num));
        }
        br.close();
        size = num;
        if (verbose) LogInfo.logs("ReVerb relations read into memory! relations: %d, supports: %d\n", size, supp);
    }

    public static void initializeForRvTable(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        int num = 0;
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            num ++;
            idxToName.put(String.valueOf(num), spt[0]);
            nameToIdx.put(spt[0], String.valueOf(num));
            ArrayList<String> tmp = new ArrayList<>();
            for (int i=1; i<spt.length; i++)
                tmp.add(spt[i]);
            support.put(String.valueOf(num), tmp);
            if (tmp.size() > threshold)
                withEnoughSupp.add(String.valueOf(num));
        }
        br.close();
        size = num;
        if (verbose) LogInfo.logs("RvTable read into memory! size: %d\n", size);
    }

    public static void calcuCoverage() {
        int _20 = 0, _50 = 0, _100 = 0;
        int sum20 = 0, sum50 = 0, sum100 = 0;
        for (Map.Entry<String, ArrayList<String>> entry : support.entrySet()) {
            if (entry.getValue().size() > 100) {
                _100++;
                sum100 += entry.getValue().size();
            }
            if (entry.getValue().size() > 50) {
                _50++;
                sum50 += entry.getValue().size();
            }
            if (entry.getValue().size() > 20) {
                _20++;
                sum20 += entry.getValue().size();
            }
        }
        LogInfo.logs("@20: %d, @50: %d, @100: %d", _20, _50, _100);
        LogInfo.logs("@20: %d, @50: %d, @100: %d", sum20, sum50, sum100);
    }

    public static void create(String infile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(infile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashMap<String, ArrayList<Integer>> ret = new HashMap<>();
        String line = "";
        int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            if (num % 10000 == 0) LogUpgrader.showLine(num, 10000);
            if (!ret.containsKey(line)) {
                ret.put(line, new ArrayList<Integer>());
            }
            ret.get(line).add(num);
        }
        LogInfo.logs("Total size: %d", ret.size());

        for (Map.Entry<String, ArrayList<Integer>> entry: ret.entrySet()) {
            if (entry.getValue().size() >= 1000) {
                LogInfo.logs(entry.getKey() + " : " + entry.getValue().size());
                bw.write(entry.getKey());
                for (Integer integer: entry.getValue())
                    bw.write("\t" + String.valueOf(integer));
                bw.write("\n");
            }
        }

        br.close();
        bw.close();
    }

    public static void generate(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashMap<String, RvTuple> ret = new HashMap<>();
        String line = "";
        int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            if (num % 10000 == 0) LogUpgrader.showLine(num, 10000);
            if (line.split("\t").length > 1) {
                RvTuple tuple = new RvTuple(line, 1);
                ret.put(tuple.idx, tuple);
            }
        }
        LogInfo.logs("Total size: %d", ret.size());

        for (int i=1; i<=support.size(); i++) {
            StringBuffer stringBuffer = new StringBuffer();
            int cnt = 0;
            for (String str : support.get(String.valueOf(i))) {
                if (ret.containsKey(str)) {
                    stringBuffer.append("\t" + ret.get(str).arg1 + " " + ret.get(str).arg2);
                    cnt++;
                }
            }
            if (cnt >= 1000) {
                bw.write(idxToName.get(String.valueOf(i)));
                bw.write(stringBuffer.toString() + "\n");
            }
        }
        br.close();
        bw.close();
    }

    public static void main(String[] args) throws Exception{
        //---> filter vector file with those supp num < 50/100
        //initialize(args[0]);
        //filter(args[1], args[2]);

        //---> count the coverage of different support number constrains
        //initialize(args[0]);
        //calcuCoverage();

        //---> filter those support number < 100/1000;
        //create(args[1], args[0]);
        initializeFor3M(args[0]);
        generate(args[2], args[3]);
    }
}
