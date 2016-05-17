package xusheng.webquestion;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 5/17/2016.
 * To map the WebQuestion relations to the Patty Synsets
 */

public class PattyMapper {
    public static String pattyFp = "/home/data/PATTY/patty-dataset-freebase/" +
            "remove-type-signature/Matt-Fb3m_med/pattern-support-dist.txt";
    public static String pattyKeyWFp = "/home/xusheng/AProject/data/patty/keywords_clean.txt";
    public static String webqFp = "/home/xusheng/WebQ/questions.v2";

    public static Map<Integer, String> webqMap = new HashMap<>();
    public static void readWebQ() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(webqFp));
        String line;
        while ((line = br.readLine()) != null) {
            String idx = line.split("\t")[1];
            line = br.readLine();
            String question = line.split("\t")[1];
            webqMap.put(Integer.parseInt(idx), question);
        }
        br.close();
        LogInfo.logs("webquestions read. size: %d", webqMap.size());
    }

    public static Map<String, String> pattyMap = new HashMap<>();
    public static void readPattyKeyWords() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(pattyKeyWFp));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (spt.length > 1) pattyMap.put(spt[0], spt[1]);
        }
        br.close();
        LogInfo.logs("patty key words read. size: %d", pattyMap.size());
    }

    public static void map() {
        for (int i=1; i<=webqMap.size(); i++) {
            String q = webqMap.get(i).substring(0, -1);

        }
    }

    public static void main(String[] args) throws IOException {
        map();
    }
}
