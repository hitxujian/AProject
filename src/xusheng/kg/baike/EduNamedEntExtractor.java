package xusheng.kg.baike;

import fig.basic.LogInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Xusheng on 13/10/2016.
 */

public class EduNamedEntExtractor {
    public static String rootFp = "/home/xusheng/starry/hudongbaike";

    public static void main(String[] args) throws IOException {
        readClasses();
        extract();
    }

    public static List<String> classes = new ArrayList<>();
    public static void readClasses() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/home/xusheng/xbj/classes.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split(" \\| ");
            for (String str: spt) {
                classes.add(str);
                LogInfo.logs(str);
            }
        }
        LogInfo.logs("[log] Classes info loaded. Size: %d.", classes.size());
    }


    public static BufferedWriter bw = null;
    public static void extract() throws IOException {
        bw = new BufferedWriter(new FileWriter(rootFp + "/EduKeyWords.txt"));
        String[] nameList = new String[]{
                "kangqi.tsv",
                "darkstar.tsv",
                "acer.tsv",
                "kenny.tsv", "329.tsv", "316.tsv",
                "xusheng_1.tsv", "xusheng_2.tsv"};
        for (String str : nameList) {
            String fp = rootFp + "/saved_" + str;
            if (!new File(fp).exists()) continue;
            LogInfo.logs("[log] Work for %s.", fp);
            BufferedReader br = new BufferedReader(new FileReader(fp));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] spt = line.split("\t");
                    // only /wiki page used
                    if (spt[1].equals("1")) {
                        int index = Integer.parseInt(spt[0]);
                        String name = spt[2].split("wiki/")[1];
                        int st = (index - 1) / 10000 * 10000 + 1;
                        int ed = st + 9999;
                        String path = rootFp + "/" + st + "-" + ed + "/" + index + "_wiki.html";
                        check(path, name);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    LogInfo.logs("[Exception] %s at %s.", line, fp);
                }
            }
            br.close();
        }
        bw.close();
    }

    public static void check(String fp, String name) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fp));
        String line;
        boolean flag = true;
        while ((line = br.readLine()) != null && flag) {
            for (String str: classes)
                if (line.contains(str)) {
                    bw.write(name + "\t" + str + "\t" + line + "\n");
                    flag = false;
                    break;
                }
        }
        br.close();
    }

}
