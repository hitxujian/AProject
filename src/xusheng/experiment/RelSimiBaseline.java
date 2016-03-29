package xusheng.experiment;

import fig.basic.LogInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xusheng on 1/29/2016.
 */

public class RelSimiBaseline {

    public static void main(String[] args) throws Exception {
        load();
        work();
    }

    public static String dir = "/home/xusheng/AProject/data/patty";
    public static void work() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dir + "/filtered-20-EP"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(dir + "/baseline.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            bw.write(line + "\n");
            line = br.readLine();
            String[] spt = line.split("\\]\t\\[");
            String[] left = spt[0].split("\\[")[1].split(", ");
            String[] right = spt[1].substring(0, spt[1].length()-1).split(", ");
            for (String str: left) System.out.print(str + "\t");
            System.out.print("\n");
            for (String str: right) System.out.print(str + "\t");
            System.out.print("\n");
            double max = -1;
            String ret = "NULL";
            for (String lword: left) {
                for (String rword: right) {
                    if (lword.equals(rword)) continue;
                    double tmp = cos(lword, rword);
                    if (tmp > max) {
                        max = tmp;
                        ret = lword + "\t" + rword;
                    }
                }
            }
            bw.write(ret + "\t" + max + "\n");
            br.readLine();
        }
        br.close();
        bw.close();
        LogInfo.logs("Job finished!");
    }

    public static double cos(String a, String b) {
        return multi(vectors.get(a), vectors.get(b));
    }

    public static double multi(ArrayList<Double> arrA, ArrayList<Double> arrB) {
        if (arrA == null || arrB == null) return -1;
        double sum = 0;
        int len = arrA.size() > arrB.size()? arrB.size(): arrA.size();
        for (int i=0; i<len; i++) {
            sum += arrA.get(i) * arrB.get(i);
        }
        return sum;
    }

    public static String googleNewsDir = "/home/xusheng/word2vec/GoogleNews-vectors-negative300.txt";
    public static HashMap<String, ArrayList<Double>> vectors = null;
    public static void load() throws Exception {
        if (vectors != null) return;
        vectors = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(googleNewsDir));
        String line = br.readLine();
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            String[] spt = line.split(" ");
            ArrayList<Double> vec = new ArrayList<>();
            for (int i=1; i<spt.length; i++) vec.add(Double.parseDouble(spt[i]));
            //LogInfo.logs(spt[0] + "\t" + vec.size());
            vectors.put(spt[0], vec);
        }
        br.close();
        LogInfo.logs("Google News Vectors Loaded. Size: %d", vectors.size());
    }
}
