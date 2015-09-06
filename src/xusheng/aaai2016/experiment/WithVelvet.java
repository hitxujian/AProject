package xusheng.aaai2016.experiment;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Administrator on 2015/9/5.
 */
public class WithVelvet {

    public static void main(String[] args) throws Exception {
        //countRel(args[0], args[1]);
        process(args[0], args[2], args[3]);
    }

    public static void countRel(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        HashSet<String> set = new HashSet<>();
        String line = "";
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rel = spt[2];
            if (! set.contains(rel)) {
                bw.write(rel + "\n");
                set.add(rel);
            }
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
        }
        br.close();
        bw.close();
        LogInfo.logs("Total rel for %s: %d", inFile, set.size());
    }

    /*process the belief file,
    * output a result file consisting 52 smallest instances relations*/
    public static void process(String inFile, String outFile, String typeFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw_o = new BufferedWriter(new FileWriter(outFile));
        BufferedWriter bw_t = new BufferedWriter(new FileWriter(typeFile));
        HashMap<String, ArrayList<Belief>> contents = new HashMap<>();

        String line = "";
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String rel = spt[2];

        }
    }
}
