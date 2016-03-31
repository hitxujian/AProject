package xusheng.kg.starry;

import fig.basic.LogInfo;
import xusheng.kg.fbzh.EntityHandler;
import xusheng.util.log.LogUpgrader;

import java.io.*;
import java.util.*;

/**
 * Created by Luo Xusheng on 3/31/16.
 */

public class PredLinker {
    public static String root = "/home/xusheng/starry";
    public static String fp_fb_raw = root + "/fb-zh.mid.triple";
    public static String fp_fb = root + "/fb-zh.triple";
    public static String fp_bb = root + "/infobox.triple";

    public static boolean isChinese(char c) {
        return c >= 0x4E00 &&  c <= 0x9FA5;
    }

    public static boolean isChinese(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c)) return true;
        }
        return false;
    }

    public static boolean match(String a, String b) {
        Set<String> setA = new HashSet<>();
        Set<String> setB = new HashSet<>();
        for (int i=0; i<a.length(); i++) {
            if (a.charAt(i) == '(') break;
            if (a.charAt(i) != ' ')
                setA.add(String.valueOf(a.charAt(i)));
        }
        LogInfo.logs(setA.toString());

        for (int i=0; i<b.length(); i++)
            if (b.charAt(i) != ' ')
                setB.add(String.valueOf(b.charAt(i)));
        LogInfo.logs(setB.toString());

        double interset = 0.0;
        for (String ch : setA)
            if (setB.contains(ch)) interset += 1;
        double perA = interset / setA.size();
        double perB = interset / setB.size();
        if (perA >= 0.5 &&  perB >= 0.5) return true;
        else return false;
    }

    public static String[] search(String task, List<String> list) {
        for (String str : list) {
            if (match(task, str)) {
                String[] ret = new String[2];
                ret[0] = str.split("\t")[0];
                ret[1] = str.split("\t")[1];
                return ret;
            }
        }
        return null;
    }

    public static void link() {
        if (fbMap == null || taskList == null) return;
        int cnt = 0;
        for (String task: taskList) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100);
            String[] spt = task.split("\t");
            if (!isChinese(spt[0]) || !isChinese(spt[2]))
                continue;
            LogInfo.logs("Now for %s...", task);
            for (Map.Entry<String, List<String>> entry: fbMap.entrySet()) {
                if (match(spt[0], entry.getKey())) {
                    String[] ret = search(spt[2], entry.getValue());
                    if (ret != null)
                        LogInfo.logs("%s\t%s: [%s, %s] [%s, %s]",
                                spt[1], ret[0], spt[0], spt[2], entry.getKey(),ret[1]);

                }
            }
        }
        LogInfo.logs("Linking Job Done.");
    }

    public static List<String> taskList = null;
    public static Map<String, List<String> > fbMap = null;

    public static void readData() throws IOException {
        /*
        if fb-zh.triple not exists, generate it in this run.
         */
        if (!new File(fp_fb).exists()) {
            LogInfo.logs("%s doesn't exist. Start constructing...", fp_fb);
            Map<String, String> entMap = EntityHandler.getEntityMap();
            BufferedReader br = new BufferedReader(new FileReader(fp_fb_raw));
            File file = new File(fp_fb);
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            String line;
            while ((line = br.readLine()) != null) {
                String[] spt = line.split("\t");
                writer.write(entMap.get(spt[0]) + "\t" + spt[1] + "\t" + entMap.get(spt[2]) + "\n");
            }
            br.close();
            writer.close();
            LogInfo.logs("%s all good.", fp_fb);
            return;
        }

        /*
        read data
         */
        taskList = new ArrayList<>();
        fbMap = new HashMap<>();

        File f = new File(fp_fb);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            if (fbMap.containsKey(spt[0])) {
                fbMap.get(spt[0]).add(spt[1] + "\t" + spt[2]);
            } else {
                List<String> tmp = new ArrayList<>();
                tmp.add(spt[1] + "\t" + spt[2]);
                fbMap.put(spt[0], tmp);
            }
        }
        br.close();
        LogInfo.logs("%d left-sided fb triples read.", fbMap.size());

        f = new File(fp_bb);
        br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        while ((line = br.readLine()) != null) {
            taskList.add(line);
        }
        br.close();
        LogInfo.logs("%d baike infobox triples read.", taskList.size());
    }

    public static void main(String[] args) throws IOException {
        readData();
        link();
    }
}
