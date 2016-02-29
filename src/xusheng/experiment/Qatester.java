package xusheng.experiment;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by angrymidiao on 2/29/2016.
 */
public class Qatester {

    public static String yaoPath = "/home/kangqi/jacana/tmp/standard/results";
    public static String quesPath = "/home/xusheng/WebQ/questions.test";

    public static void pickRelated() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(yaoPath + "/" +
                "sort.result.no_feature.sparse5"));
        String line;
        double score = 0;
        int hasAnswer = 0;
        int total = 0;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (questions.contains(spt[1])) {
                total ++;
                double tmp = Double.parseDouble(spt[0]);
                if (tmp != -1) {
                    score += tmp;
                    hasAnswer++;
                }
            }
        }
        LogInfo.logs("totally %d questions appeared, %d has answers, score: %f," +
                "precision?%f", total, hasAnswer, score, score/hasAnswer);
    }

    public static HashSet<String> questions = new HashSet<>();
    public static void readQues() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(quesPath));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            questions.add(spt[1]);
            br.readLine();
        }
        LogInfo.logs("Questions' size: %d", questions.size());
    }

    public static void main(String[] args) throws IOException {
        readQues();
        pickRelated();
    }
}
