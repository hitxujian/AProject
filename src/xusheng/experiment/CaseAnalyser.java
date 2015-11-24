package xusheng.experiment;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by angrymidiao on 11/12/15.
 */
public class CaseAnalyser {

    public static String path = "/home/xusheng/caseAnalysis";
    public static String compPath = "/home/xusheng/test_matrix_comp";
    public static void chooseCover() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(compPath));
        BufferedWriter cp = new BufferedWriter(new FileWriter(path + "/Cover-Pos-2.pair"));
        BufferedWriter cn = new BufferedWriter(new FileWriter(path + "/Cover-Neg-2.pair"));
        BufferedWriter up = new BufferedWriter(new FileWriter(path + "/Uncover-Pos-2.pair"));
        BufferedWriter un = new BufferedWriter(new FileWriter(path + "/Uncover-Neg-2.pair"));
        String line; int cnt = 0;
        br.readLine();
        while ((line = br.readLine()) != null) {
            cnt += 2;
            if (cnt == 8148) break;
            boolean pos;
            String[] spt = line.split("\t");
            if (spt[1].equals("+1")) pos = true;
            else pos = false;
            String pairs = spt[2] + "\t" + spt[3];
            line = br.readLine();
            spt = line.split(" ");
            if (spt[437].equals("1"))
                if (pos) cp.write(pairs + "\n");
                else cn.write(pairs + "\n");
            else if (pos) up.write(pairs + "\n");
            else un.write(pairs + "\n");
        }
        br.close();
        cp.close();
        cn.close();
        up.close();
        un.close();
        LogInfo.logs("Job Done.");
    }

    public static void main(String[] args) throws Exception {
        //chooseCover();
        calcuF1();
    }

    public static void calcuF1() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(compPath));
        String line; int cnt = 0;
        int TP = 0, FP = 0, TN = 0, FN = 0;
        br.readLine();
        while ((line = br.readLine()) != null) {
            cnt += 2;
            if (cnt == 2088) break;
            boolean pos;
            String[] spt = line.split("\t");
            if (spt[1].equals("+1")) pos = true;
            else pos = false;
            line = br.readLine();
            spt = line.split(" ");
            if (spt[450].equals("1") || spt[437].equals("1"))
                if (pos) TP += 1;
                else FP += 1;
            else if (pos) FN +=1;
            else TN += 1;
        }
        double f1 = (double) 2*TP / (2*TP + FP + FN);
        LogInfo.logs("TP: %d, FP: %d, TN: %d, FN: %d\nF1: %.2f", TP, FP, TN, FN, f1);
    }
}
