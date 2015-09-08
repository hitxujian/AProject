package xusheng.nell;

import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by Xusheng on 2015/9/7.
 */
public class FileProcessor {

    public static void main(String[] args) throws Exception {
        String CMD = args[0];
        if (CMD.equals("Entity")) extractEntity(args[1], args[2], args[3]);
        if (CMD.equals("TYPE")) extractType(args[4], args[5]);
    }

    public static void extractEntity(String inFile, String newFile, String entFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw_n = new BufferedWriter(new FileWriter(newFile));
        BufferedWriter bw_e = new BufferedWriter(new FileWriter(entFile));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
            if (line.startsWith("###")) {
                bw_n.write(line + "\n");
                continue;
            }
            String[] spt = line.split("\t");


        }
    }

    public static void extractType(String inFile, String typeFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(typeFile));
        String line = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
            if (line.startsWith("###")) {
                bw.write(line + "\n");
                continue;
            }
            String[] spt = line.split("\t");

        }
    }
}
