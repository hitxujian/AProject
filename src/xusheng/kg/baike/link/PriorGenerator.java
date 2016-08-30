package xusheng.kg.baike.link;

import fig.basic.LogInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by Xusheng on 8/30/2016.
 * Calculate the prior probability of entity given alias.
 * Input: raw html files.
 * Output: alias-entity-probability.
 */

public class PriorGenerator implements Runnable{

    public static String rootFp = "/home/xusheng/starry/hudongbaike";

    public static int curr = -1, end = -1, inc = 0;

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                scan(idx);
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

    public static void scan(int idx) throws IOException {
        String task = taskList.get(idx);
        String[] spt = task.split("\t");
        int index = Integer.parseInt(spt[0]);
        String name = spt[2];
        int st = index / 10000 * 10000 + 1;
        int ed = st + 9999;
        String fp = rootFp + "/" + st + "-" + ed;
        if (spt[1].equals("1"))
            fp += "/" + index + "_wiki.html";
        else
            fp += "/" + index + "_search.html";
        if (! new File(fp).exists()) {
            LogInfo.logs("[missing] Page %d does not exist.", index);
            return;
        }
        File input = new File(fp);
        Document doc = Jsoup.parse(input, "UTF-8", "");
        Elements links = doc.select("a[href]");
        for (Element link: links) {
            String linkHref = link.attr("abs:href");
            if (!linkHref.startsWith("http://www.baike.com/wiki/")
                    && !linkHref.startsWith("http://so.baike.com/doc/"))
                continue;
            String linkUrl = urlDecode(linkHref);
            String linkText = link.text();
            if (linkText.isEmpty()) continue;
            if (urlEntMap.containsKey(linkUrl)) {
                int entIdx = urlEntMap.get(linkUrl);
                addToPriorMap(linkText, entIdx);
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

    public static synchronized void addToPriorMap(String alias, int entIdx) {
        if (!aliasEntCntMap.containsKey(alias))
            aliasEntCntMap.put(alias, new HashMap<>());
        if (!aliasEntCntMap.get(alias).containsKey(entIdx))
            aliasEntCntMap.get(alias).put(entIdx, 1);
        else {
            int tmp = aliasEntCntMap.get(alias).get(entIdx) + 1;
            aliasEntCntMap.get(alias).put(entIdx, tmp);
        }
    }

    public static Map<String, Map<Integer, Integer>> aliasEntCntMap = new HashMap<>();
    public static void multiThreadWork() throws Exception {
        readTasks();
        curr = 0;
        end = taskList.size();
        LogInfo.logs("Task: Extract infobox for Hudong Baike (%d pages). [%s]", end, new Date().toString());
        int numOfThreads = 32;
        PriorGenerator workThread = new PriorGenerator();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.logs("[log] Scanning done. Now calculating probability... [%s]", new Date().toString());
        getProbability();
        LogInfo.end_track();
    }

    public static void getProbability() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/content/prior.tsv"));
        for (Map.Entry<String, Map<Integer, Integer>> entry: aliasEntCntMap.entrySet()) {
            String alias = entry.getKey();
            Map<Integer, Integer> map = entry.getValue();
            int cnt = 0;
            for (Map.Entry<Integer, Integer> entry1: map.entrySet())
                cnt += entry1.getValue();
            for (Map.Entry<Integer, Integer> entry1: map.entrySet()) {
                double prob = (double) entry1.getValue() / cnt;
                String probv = String.format("%.5f", prob);
                bw.write(alias + "\t" + entry1.getKey().toString() + "\t" + probv + "\n");
            }
        }
        bw.close();
    }

    public static List<String> taskList = null;
    public static Map<String, Integer> urlEntMap = null;
    public static void readTasks() throws IOException {
        String[] nameList = new String[] {
                "kangqi.tsv",
                "darkstar.tsv",
                "xusheng_1.tsv",
                "xusheng_2.tsv"
        };
        taskList = new ArrayList<>();
        urlEntMap = new HashMap<>();
        for (String str: nameList) {
            String fp = rootFp + "/saved_" + str;
            if (! new File(fp).exists()) continue;
            BufferedReader br = new BufferedReader(new FileReader(fp));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] spt = line.split("\t");
                    if (spt[1].equals("1") || spt[1].equals("2")) {
                        taskList.add(line);
                        urlEntMap.put(spt[2], Integer.parseInt(spt[0]));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LogInfo.logs("[Exception] %s at %s.", line, fp);
                }
            }
            br.close();
        }
        LogInfo.logs("[log] Tasks loaded. Size: %d.", taskList.size());
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
