package xusheng.modeling2015;

import fig.basic.LogInfo;
import fig.basic.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by angrymidiao on 9/18/15.
 */
public class TransformTable {

    public static HashMap<Pair<String, String>, Double> dist = new HashMap<>();
    public static HashSet<String> citeSet = new HashSet<>();

    public static void processSingleFile(String inFile, String outFile) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        DecimalFormat df = new DecimalFormat("#.00");
        String line; int idx = 1;
        while ((line = br.readLine()) != null) {
            idx ++;
            String[] spt = line.split(",");
            for (int i=1; i<idx; i++) {
                double distance = Double.parseDouble(spt[i])/90;
                //dist.put(new Pair<>(String.valueOf(idx), String.valueOf(i)), distance);
                bw.write(idx + "\t" + i + "\t" + String.format("%.2f", distance) + "\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("%s processed.", inFile);
    }

    public static void readFile(String inFile1, String inFile2, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile1));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            citeSet.add(spt[0]);
            citeSet.add(spt[1]);
            Pair<String, String> pair = new Pair<>(spt[0], spt[1]);
            dist.put(pair, Double.parseDouble(spt[2]));
            pair = new Pair<>(spt[1], spt[0]);
            dist.put(pair, Double.parseDouble(spt[2]));
        }
        br.close();
        LogInfo.logs("Total cite size: %d", citeSet.size());
    }

    public static void calcuPath(String outFile) throws Exception{
        for (String mid: citeSet)
            for (String st: citeSet)
                for (String ed: citeSet) {
                    Pair<String, String> p1 = new Pair<>(st, mid);
                    Pair<String, String> p2 = new Pair<>(mid, ed);
                    if (!st.equals(ed) && !mid.equals(st) && !mid.equals(ed)
                            && dist.containsKey(p1) && dist.containsKey(p2)) {
                        double sum = dist.get(new Pair<>(st, mid)) + dist.get(new Pair<>(mid, ed));
                        Pair<String, String> pair = new Pair<>(st, ed);
                        if (dist.containsKey(pair)) {
                            double dis = dist.get(pair);
                            if (dis > sum) {
                                LogInfo.logs("Update dist <%s ==> %s, %f> : <%s ==> %s ==> %s, %f>",
                                        st, ed, dis, st, mid, ed, sum);
                                dist.put(new Pair<>(st, ed), sum);
                                dist.put(new Pair<>(ed, st), sum);
                            }
                        } else {
                            LogInfo.logs("Add dist <%s ==> %s> : <%s ==> %s ==> %s, %f>",
                                    st, ed, st, mid, ed, sum);
                            dist.put(new Pair<>(st, ed), sum);
                            dist.put(new Pair<>(ed, st), sum);
                        }
                    }
                }
        LogInfo.logs("Path complete, now write ret into file %s...", outFile);
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        for (String st : citeSet)
            for (String ed : citeSet)
                bw.write(st + "\t" + ed + "\t" + dist.get(new Pair<>(st, ed)) + "\n");
        bw.close();
        LogInfo.logs("Complete, total size : %d", dist.size());
    }

    public static void main(String[] args) throws Exception {
        //processSingleFile(args[0], args[1]);
        readFile(args[0], args[1], args[2]);
        calcuPath(args[3]);
    }
}
