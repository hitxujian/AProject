package xusheng.aaai2016.experiment;

import xusheng.freebase.EntityIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by Administrator on 2015/9/16.
 */
public class Reverber {

    public static void main(String[] args) throws Exception {
        generateInput(args[0], args[1], args[2]);
    }

    public static void generateInput(String inFile, String outFile, String fbFile) throws Exception {
        EntityIndex.initFromMid2Idx(fbFile);
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (! line.startsWith("#")) continue;
            String[] spt = line.split("\t");
            bw.write("###\t" + spt[0].substring(1, spt[0].length()) + "\t:\n");
            for (int i=1; i<spt.length; i++) {
                String[] pair = spt[i].split(" ");
                String ent1 = EntityIndex.getIdx(pair[0]);
                String ent2 = EntityIndex.getIdx(pair[1]);
                if (ent1 != null && ent2 != null)
                    bw.write(ent1 + "\t" + ent2 + "\n");
            }
        }
        bw.close();
    }
}
