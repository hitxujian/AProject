package xusheng.experiment;

import fig.basic.LogInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Xusheng Luo on 3/3/16.
 */
public class WebQexchanger {

    public static String dir = "/home/xusheng/jacana/freebase-data/webquestions";
    public static String quesPath = "/home/xusheng/WebQ/questions.test";

    public static HashSet<String> newTrain = new HashSet<>();
    public static HashSet<String> newDev = new HashSet<>();
    public static ArrayList<String> leftTest = new ArrayList<>();

    public static void exchange(String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dir + file));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("[") || line.startsWith("]")) continue;
            String[] spt = line.split("\"");
            String q = spt[spt.length-2];
            if (questions.contains(q)) {
                bw.write(line + "\n");
                cnt ++;
            }
            else {
                if (file.equals("/webquestions.examples.train.80.json")) newTrain.add(line);
                else if (file.equals("/webquestions.examples.dev.20.json")) newDev.add(line);
                else leftTest.add(line);
            }
        }
        br.close();
        LogInfo.logs("%s done,, size: %d", file, cnt);
    }

    public static HashSet<String> questions = new HashSet<>();
    public static void readQues() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(quesPath));
        String line;
        while ((line = br.readLine()) != null) {
            line = br.readLine();
            String[] spt = line.split("\t");
            questions.add(spt[1]);
        }
        LogInfo.logs("Questions' size: %d", questions.size());
    }

    public static BufferedWriter bw;
    public static void main(String[] args) throws IOException {
        readQues();
        bw = new BufferedWriter(new FileWriter(dir + "/webquestions.new.test.json"));
        bw.write("[\n");
        exchange("/webquestions.examples.train.80.json");
        exchange("/webquestions.examples.dev.20.json");
        exchange("/webquestions.examples.test.json");
        bw.write("]");
        bw.close();
        LogInfo.logs(newTrain.size());
        LogInfo.logs(newDev.size());
        LogInfo.logs(leftTest.size());
        int idx = 0;
        while (newTrain.size() != 3023) {
            newTrain.add(leftTest.get(idx));
            idx ++;
        }
        while (newDev.size() != 755) {
            newDev.add(leftTest.get(idx));
            idx ++;
        }
        bw = new BufferedWriter(new FileWriter(dir + "/webquestions.new.train.json"));
        bw.write("[\n");
        for (String line: newTrain) bw.write(line + "\n");
        bw.write("]");
        bw.close();
        bw = new BufferedWriter(new FileWriter(dir + "/webquestions.new.dev.json"));
        bw.write("[\n");
        for (String line: newDev) bw.write(line + "\n");
        bw.write("]");
        bw.close();
    }
}
