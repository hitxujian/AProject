package xusheng.kg.baike;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MultiThread;
import xusheng.util.struct.MultiThreadTemplate;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Xusheng on 13/10/2016.
 */

public class EduNamedEntExtractor implements Runnable{
    public static String rootFp = "/home/xusheng/starry/hudongbaike";

    public static int curr = -1, end = -1;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                check(idx);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            return ret;
        }
        return -1;
    }

    public static void main(String[] args) throws Exception {
        readClasses();
        readTasks();
        multiThreadWork();
    }

    public static Set<String> classes = new HashSet<>();
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
    public static List<String> taskList = new ArrayList<>();
    public static void readTasks() throws IOException {
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
                        taskList.add(path + "\t" + name);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    LogInfo.logs("[Exception] %s at %s.", line, fp);
                }
            }
            br.close();
        }
        LogInfo.logs("[log] Tasks info loaded. Size: %d.", taskList.size());
    }

    public static void multiThreadWork() throws Exception{
        curr = 0; end = taskList.size();
        int numOfThreads = 32;
        // todo: re-write the work thread
        EduNamedEntExtractor workThread = new EduNamedEntExtractor();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
        bw.close();
    }

    public static void check(int idx) throws IOException {
        LogUpgrader.showLine(idx, 10000);
        String[] task = taskList.get(idx).split("\t");
        String fp = task[0];
        String name = task[1];
        BufferedReader br = new BufferedReader(new FileReader(fp));
        String line;
        boolean flag = true;
        while ((line = br.readLine()) != null && flag) {
            if (line.trim().startsWith("<p id=\"openCatp")) {
                Pattern pat = Pattern.compile("title=\"(.*?)\" href");
                Matcher mat = pat.matcher(line);
                while (mat.find()) {
                    String cate = mat.group(1);
                    if (classes.contains(cate)) {
                        writeRet(name + "\t" + cate + "\n");
                        flag = false;
                        break;
                    }
                }
            }

        }
        br.close();
    }

    public static synchronized void writeRet(String ret) throws IOException {
        bw.write(ret);
    }

}
