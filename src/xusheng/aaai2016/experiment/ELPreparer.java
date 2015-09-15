package xusheng.aaai2016.experiment;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by Administrator on 2015/9/15.
 */
public class ELPreparer {

    public static void main(String[] args) throws Exception {
        changeFormat(args[0], args[1]);
    }

    public static String removeUnderline(String entity) {
        return entity.replaceAll("_+", " ");
    }

    public static void changeFormat(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line, relation = ""; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            if (cnt % 10000 == 0) LogUpgrader.showLine(cnt, 10000);
            if (line.startsWith("###")) {
                relation = line.split("\t")[1];
                continue;
            }
            String spt[] = line.split("\t");
            bw.write(removeUnderline(spt[0]) + "\t" + relation + "\t" + removeUnderline(spt[1]) + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Job Done.");
    }
}
