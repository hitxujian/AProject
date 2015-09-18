package xusheng.modeling2015;

import fig.basic.LogInfo;
import xusheng.util.struct.Pair;

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

    }

    public static void calcuPath() {
        for (String mid: citeSet)
            for (String st: citeSet)
                for (String ed: citeSet)
                    if (!st.equals(ed) && !mid.equals(st) && !mid.equals(ed)) {
                        double dis = dist.get(new Pair<>(st, ed));
                        double sum = dist.get(new Pair<>(st, mid)) + dist.get(new Pair<>(mid, ed));
                        if (dis < sum) {
                            dist.put(new Pair<>(st, ed), sum);
                            dist.put(new Pair<>(ed, st), sum);
                        }
                    }
    }

    public static void main(String[] args) throws Exception {
        processSingleFile(args[0], args[1]);
        //calcuPath();
    }
}
