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

    public static String path = "/home/xusheng";
    public static String compPath = "/home/xusheng/train_matrix_comp";
    public static void chooseCover() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(compPath));
        BufferedWriter cp = new BufferedWriter(new FileWriter(path + "/Cover-Pos.pair"));
        BufferedWriter cn = new BufferedWriter(new FileWriter(path + "/Cover-Neg.pair"));
        BufferedWriter up = new BufferedWriter(new FileWriter(path + "/Uncover-Pos.pair"));
        BufferedWriter un = new BufferedWriter(new FileWriter(path + "/Uncover-Neg.pair"));
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
            if (spt[450].equals("1"))
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
        chooseCover();
    }
}
