package xusheng.kg.wiki;

import fig.basic.LogInfo;
import fig.record.RegexMatcher;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Xusheng on 9/30/16.
 * Add links on texts already mentioned on a particular page.
 * Input: Wikipedia dump in several folders.
 * Output: One file where links are already added.
 */

public class WikiLinkAdder implements Runnable {
    public static String rootFp = "/home/wikipedia/extracted";
    public static int curr = -1, end = -1;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                addLinks(idx);
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

    public static void addLinks(int idx) throws IOException {
        String taskFp = taskList.get(idx);
        BufferedReader br = new BufferedReader(new FileReader(taskFp));
        BufferedWriter bw = new BufferedWriter(new FileWriter(taskFp + "_links"));
        String line;
        String entity = "", mark = "";
        Set<String> names = new HashSet<>();
        while ((line = br.readLine()) != null) {
            if (line.startsWith("<doc")) {
                entity = line.split("title=\"")[1].split("\">")[0];
                mark = String.format("aabb%sbbaa", entity.replace(" ", "")
                        .replace("(","ccdd")).replace(")", "ddcc");
                names.clear();
                if (anchorTextMap.containsKey(entity))
                    names = anchorTextMap.get(entity);
                names.add(entity);
                names.add(entity.split(" \\(")[0]);
                br.readLine();
                br.readLine();
            } else {
                for (String name: names)
                    line.replace(name, mark);
                Pattern pattern = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>");
                Matcher matcher = pattern.matcher(line);
                bw.write(line + "\n");
            }
        }


    }

    public static String urlDecode(String url) {
        String ret = url;
        try {
            while (true) {
                ret = ret.replaceAll("% +", "%");	//correct error encodes like "% 9B" (should be %9B)
                String decode = URLDecoder.decode(ret, "UTF-8");
                if (!decode.equals(ret))
                    ret = decode;
                else
                    break;
            }
        } catch (Exception ex) {
            LogInfo.logs("[T%s] Fail to Decode url [%s].", Thread.currentThread().getName(), ret);
        }
        return ret;
    }

    public static Map<String, Set<String>> anchorTextMap = null;
    public static void multiThreadWork() throws Exception{
        anchorTextMap = AnchorTextReader.ReadData();
        curr = 0; end = 0;
        int numOfThreads = 32;
        WikiLinkAdder workThread = new WikiLinkAdder();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
    }

    public static void main(String[] args) throws IOException {
        for (char Ch ='A'; Ch<='F'; Ch++) {
            for (char ch = 'A'; ch <= 'Z'; ch++) {
                for (int i = 0; i <= 9; i++) {
                    String fp = String.format("%s/%s%s/wiki_0%d", rootFp, Ch, ch, i);
                    if (new File(fp).exists()) {
                        addToTasks(fp);
                    }
                }
                for (int i = 10; i <= 99; i++) {
                    String fp = String.format("%s/%s%s/wiki_%d", rootFp, Ch, ch, i);
                    if (new File(fp).exists()) {
                        addToTasks(fp);
                    }
                }
            }
        }
    }

    public static List<String> taskList = null;
    public static void addToTasks(String fp) {
        taskList = new ArrayList<>();
        taskList.add(fp);
    }
}