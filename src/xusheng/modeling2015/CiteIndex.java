package xusheng.modeling2015;

import fig.basic.LogInfo;
import fig.basic.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by angrymidiao on 9/19/15.
 */
public class CiteIndex {

    public static HashMap<String, Integer> cite2idx = new HashMap<>();
    public static HashMap<Integer, String> idx2cite = new HashMap<>();
    public static HashMap<Pair<Integer, Integer>, Double> matrix = new HashMap<>();

    public static Integer getIdx(String cite) {
        if (cite2idx.containsKey(cite))
            return cite2idx.get(cite);
        else return null;
    }

    public static String getCite(String idx) {
        if (cite2idx.containsKey(idx))
            return idx2cite.get(idx);
        else return null;
    }

    public static double transform(String dist) {
        double tmp = Double.parseDouble(dist);
        int increment = (int)(tmp/8) * 4;
        return increment + tmp;
    }

    public static void construct(String inFile, String outFile, String retFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

        String line; int idx = -1;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (Integer.parseInt(spt[0]) > 1000 || Integer.parseInt(spt[1]) > 1000)
                continue;

            if (!cite2idx.containsKey(spt[0])) {
                idx ++;
                cite2idx.put(spt[0], idx);
                idx2cite.put(idx, spt[0]);
                bw.write(idx + "\t" + spt[0] + "\n");
            }
            if (!cite2idx.containsKey(spt[1])) {
                idx ++;
                cite2idx.put(spt[1], idx);
                idx2cite.put(idx, spt[1]);
                bw.write(idx + "\t" + spt[1] + "\n");
            }
            int idx1 = getIdx(spt[0]);
            int idx2 = getIdx(spt[1]);
            double dist = transform(spt[2]);
            matrix.put(new Pair<>(idx1, idx2), dist);
        }
        br.close();
        bw.close();

        LogInfo.logs("Index Done, size : %d", cite2idx.size());
        bw = new BufferedWriter(new FileWriter(retFile));
        for (int i=1; i<=232; i++) {
            for (int j=1; j<232; j++)
                if (matrix.containsKey(new Pair<>(i, j)))
                    bw.write(String.format("%.2f\t", matrix.get(new Pair<>(i, j))));
                else
                    bw.write("0.00\t");
            bw.write("\n");
        }
        bw.close();
        LogInfo.logs("Index-Matrix Done.");
    }

    public static void main(String[] args) throws Exception {
        construct(args[0], args[1], args[2]);
    }

}
