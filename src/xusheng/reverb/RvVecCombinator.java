package xusheng.reverb;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Administrator on 2015/5/2.
 */
public class RvVecCombinator {

    public static HashSet<String> linked = new HashSet<>();
    public static boolean verbose = true;

    public static void readLinked(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line=br.readLine()) != null) {
            linked.add(line);
        }
        br.close();
        if (verbose) LogInfo.logs("Linked file read into memory!");
    }

    public static void combine(String file1, String file2, String file3, String file4, String file5) throws Exception {
        BufferedReader br1 = new BufferedReader(new FileReader(file1));
        BufferedReader br2 = new BufferedReader(new FileReader(file2));
        BufferedReader br3 = new BufferedReader(new FileReader(file3));
        BufferedWriter bw = new BufferedWriter(new FileWriter(file4));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(file5));
        String line = ""; int num = 0;
        while ((line=br1.readLine()) != null) {
            num ++;
            if (num % 10000 == 0) LogInfo.logs("%d lines processed...", num);
            double total = 0;
            HashMap<String, Integer> vector = new HashMap<>();

            String[] spt = line.split("\t");
            for (int i=1; i<spt.length; i++) {
                String idx = spt[i].split(" ")[0];
                int cnt = Integer.parseInt(spt[i].split(" ")[1]);
                if (vector.containsKey(idx)) {
                    int count = vector.get(idx) + cnt;
                    vector.put(idx, count);
                }
                else vector.put(idx, cnt);
            }

            line = br2.readLine();
            spt = line.split("\t");
            for (int i=1; i<spt.length; i++) {
                String idx = spt[i].split(" ")[0];
                int cnt = Integer.parseInt(spt[i].split(" ")[1]);
                if (vector.containsKey(idx)) {
                    int count = vector.get(idx) + cnt;
                    vector.put(idx, count);
                }
                else vector.put(idx, cnt);
            }

            line = br3.readLine();
            spt = line.split("\t");
            for (int i=1; i<spt.length; i++) {
                String idx = spt[i].split(" ")[0];
                int cnt = Integer.parseInt(spt[i].split(" ")[1]);
                if (vector.containsKey(idx)) {
                    int count = vector.get(idx) + cnt;
                    vector.put(idx, count);
                }
                else vector.put(idx, cnt);
            }

            double[] arr = new double[4970]; boolean flag = false;

            for (Map.Entry<String, Integer> entry: vector.entrySet()) {
                total += (double) entry.getValue() * (double) entry.getValue();
            }

            total = Math.sqrt(total);

            // add ratio of linked supports
            int tmp = 0;
            ArrayList<String> supp = RvEqualSet.getSupport(String.valueOf(num));
            for (String idx: supp) {
                if (linked.contains(idx)) tmp ++;
            }
            double ratio = (double) tmp / supp.size();
            //

            bw2.write(String.valueOf(num));
            for (Map.Entry<String, Integer> entry: vector.entrySet()) {
                int index = Integer.parseInt(entry.getKey());
                arr[index] = (double) entry.getValue() / total * ratio;
                flag = true;
                bw2.write("\t" + entry.getKey() + " " + entry.getValue());
            }
            bw2.write("\n");

            if (flag) {
                bw.write(String.valueOf(num));
                for (int i = 1; i <= 4969; i++) bw.write("\t" + arr[i]);
                /*if (num == 73196)
                    for (int i = 1; i <= 4969; i++)
                        if (arr[i] >0) LogInfo.logs(i + " : " + arr[i]);*/
                bw.write("\n");
            }
        }
        br1.close();
        br2.close();
        br3.close();
        bw.close();
        bw2.close();
    }


    public static void main(String args[]) throws Exception {
        readLinked(args[5]);
        RvEqualSet.initializeFor3M(args[6]);
        combine(args[0], args[1], args[2], args[3], args[4]);
    }
}
