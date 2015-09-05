package xusheng.reverb;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Administrator on 2015/6/7.
 */
public class SynsetCreator {

    public static HashSet<String> set = new HashSet<>();

    public static void create(String inFile, String outFile, String th) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        Double threshold = Double.parseDouble(th);
        LogInfo.logs("Threshold: %f", threshold);
        String line = ""; int size = 0;
        LogInfo.logs("Start to create Synsets...");
        while ((line = br.readLine()) != null) {
            LogInfo.begin_track("New Sysnet...");
            String[] spt = line.split("\t");
            ArrayList<String> list = new ArrayList<>();
            for (int i=1; i<spt.length; i++) {
                String[] words = spt[i].split(" ");
                StringBuffer phrase = new StringBuffer();
                phrase.append(words[0]);
                for (int j=1; j<words.length-1; j++) phrase.append(" " + words[j]);
                String relation = phrase.toString();
                Double score = Double.parseDouble(words[words.length-1]);
                //if (!set.contains(relation) && score > threshold) {
                if (score > threshold) {
                    list.add(spt[i]);
                    set.add(relation);
                    LogInfo.logs("relation: %s, score: %f", relation, score);
                }
            }
            if (list.size() > 1) {
                size ++;
                bw.write("#" + list.get(0));
                for (int j = 1; j < list.size(); j++)
                    bw.write("\t" + list.get(j));
                bw.write("\n");
                LogInfo.logs("size: %d", list.size());
            }
            LogInfo.end_track();
        }
        br.close();
        bw.close();
        LogInfo.logs("Synsets created! size: %d", size);
    }

    public static void main(String[] args) throws Exception {
        create(args[0], args[1], args[2]);
    }
}
