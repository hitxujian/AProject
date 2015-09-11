package xusheng.nell.emnlp2011labeleddata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by Administrator on 2015/9/11.
 */
public class process {

    public static void extractPos(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split(",");
            if (spt.length < 2) {}
        }
    }

    public static void main(String[] args) throws Exception {

    }
}
