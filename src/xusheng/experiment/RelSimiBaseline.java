package xusheng.experiment;

import fig.basic.LogInfo;
import xusheng.word2vec.VecLoader;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by angrymidiao on 1/29/2016.
 */
public class RelSimiBaseline {

    public static void main(String[] args) throws Exception {
        VecLoader.load();
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
            LogInfo.logs(left.toString());
            LogInfo.logs(right.toString());
            double max = -1;
            String ret = "NULL";
            for (String lword: left) {
                for (String rword: right) {
                    double tmp = cos(lword, rword);
                    if (tmp > max) {
                        max = tmp;
                        ret = lword + "\t" + rword;
                    }
                }
            }
            bw.write(ret + "\n");
            br.readLine();
        }
        br.close();
        bw.close();
    }

    public static double cos(String a, String b) {
        return multi(VecLoader.vectors.get(a), VecLoader.vectors.get(b));
    }

    public static double multi(ArrayList<Double> arrA, ArrayList<Double> arrB) {
        if (arrA == null || arrB == null) return -1;
        double sum = 0;
        for (int i=0; i<arrA.size(); i++) {
            for (int j=0; j<arrB.size(); j++) sum += arrA.get(i) * arrB.get(j);
        }
        return sum;
    }
}
