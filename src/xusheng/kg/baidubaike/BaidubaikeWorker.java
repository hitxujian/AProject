package xusheng.kg.baidubaike;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng Luo on 3/29/16.
 * Usage: process raw baidubaike htms using multi threads working.
 */

public class BaidubaikeWorker implements Runnable{
    public static String root = "/home/xusheng/crawl";
    public static int curr = -1, end = -1, inc = 0;
    public static BufferedWriter idxBw, infoBw;
    public static Map<String, Integer> urlMap = new HashMap<>();
    public static Map<Integer, Set<String>> idNameMap = new HashMap<>();
    public static List<String> triples = new ArrayList<>();

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                extractInfobox(idx);
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

    public static void multiThreadWork() throws Exception {
        //idxBw = new BufferedWriter(new FileWriter(root + "/entity.index"));
        infoBw = new BufferedWriter(new FileWriter(root + "/infobox.triple"));
        curr = 1; end = 300;
        LogInfo.logs("Begin to Construct Article Idx and Extract Infobox...");
        int numOfThreads = 8;
        BaidubaikeWorker workThread = new BaidubaikeWorker();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
        //idxBw.close();
        infoBw.close();
        // write results into files
        writeIdx();
        writeIdxText();
        //writeTriples();
    }

    public static synchronized int add2Urls(String url) throws IOException{
        if (! urlMap.containsKey(url)) {
            inc ++;
            urlMap.put(url, inc);
            //idxBw.write(url + "\t" + inc + "\n");
        }
        return urlMap.get(url);
    }

    public static synchronized void add2AnchorTexts(Integer id, String text) {
        if (! idNameMap.containsKey(id)) {
            Set<String> tmp = new HashSet<>();
            tmp.add(text);
            idNameMap.put(id, tmp);
        } else idNameMap.get(id).add(text);
    }

    public static synchronized void writeTriple(String triple) throws IOException {
        infoBw.write(triple);
    }

    public static void extractInfobox(int idx) throws Exception {
        int cnt = idx * 10000;
        String folderName = (cnt-10000+1) + "-" + cnt;
        LogInfo.logs("Entering into %s... [%s]", folderName, new Date().toString());
        for (int i=cnt-10000+1; i<=cnt; i++) {
            String fp = root + "/data_v2/" + folderName + "/" + i + ".html";
            BufferedReader br = new BufferedReader(new FileReader(fp));
            String url = br.readLine().split("com")[1];
            // when meet a new url, add it to map and write it into file
            int leftIdx = add2Urls(url);
            String line;
            String title;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("<title>")) {
                    title = line.split(">")[1].split("<")[0].split("_")[0];
                    add2AnchorTexts(leftIdx, title);
                    continue;
                }
                if (line.startsWith("<dt class=\"basicInfo-item name\"")) {
                    String itemName = line.split(">")[1].split("<")[0];
                    String[] spt = itemName.split("&nbsp;");
                    itemName = "";
                    for (int j=0; j<spt.length; j++)
                        itemName += spt[j];
                    br.readLine();
                    line = br.readLine();
                    spt = line.split("<.+?>");
                    String itemValue = "";
                    for (int j=0; j<spt.length; j++) {
                        itemValue += spt[j];
                    }
                    spt = line.split("href=\"");
                    String href, triple = "";
                    // if meet a link in the infobox
                    if (spt.length > 1) {
                        href = spt[1].split("\">")[0];
                        int rightIdx = add2Urls(href);
                        add2AnchorTexts(rightIdx, itemValue);
                        triple  = leftIdx + "\t" + itemName + "\t" + rightIdx + "\n";
                        writeTriple(triple);
                    // if no link, then write plain text
                    } else {
                        triple = leftIdx + "\t" + itemName + "\t" + itemValue + "\n";
                        //triples.add(triple);
                        writeTriple(triple);
                    }
                }
            }
            br.close();
        }
        LogInfo.logs("Job %s is Finished. [%s]", folderName, new Date().toString());
    }

    public static void writeIdx() throws IOException {
        LogInfo.begin_track("Now writing entity-idx into file...");
        int cnt = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/entity.index"));
        for (Map.Entry<String, Integer> entry: urlMap.entrySet()) {
            cnt ++;
            LogUpgrader.showLine(cnt, 10000);
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        bw.close();
        LogInfo.end_track();
    }

    public static void writeTriples() throws IOException {
        LogInfo.begin_track("Now writing infobox triple...");
        int cnt = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/infobox.triple"));
        for (String str : triples) {
            cnt ++;
            LogUpgrader.showLine(cnt, 100000);
            bw.write(str);
        }
        bw.close();
        LogInfo.end_track();
    }

    public static void writeIdxText() throws IOException {
        LogInfo.begin_track("Now writing idx-name into file...");
        int cnt = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/entity.name"));
        for (Map.Entry<Integer, Set<String>> entry: idNameMap.entrySet()) {
            cnt ++;
            LogUpgrader.showLine(cnt, 10000);
            bw.write(entry.getKey());
            for (String str : entry.getValue()) bw.write("\t" + str);
            bw.write("\n");
        }
        bw.close();
        LogInfo.end_track();
    }

    public static void extractURLs() throws IOException {
        Set<String> urls = new HashSet<>();
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/visited.0402"));
        int cnt = 0;
        while (cnt < 270000) {
            cnt += 10000;
            String name = (cnt - 10000 + 1) + "-" + cnt;
            for (int i = cnt - 10000 + 1; i <= cnt; i++) {
                String fp = root + "/data_v2/" + name + "/" + i + ".html";
                if (! new File(fp).exists()) break;
                LogInfo.begin_track("Entering into %s...", fp);
                BufferedReader br = new BufferedReader(new FileReader(fp));
                String line = br.readLine();
                if (!urls.contains(line)) {
                    urls.add(line);
                    bw.write(line + "\n");
                }
                br.close();
                LogInfo.end_track();
            }
        }
        bw.close();
        Set<String> todo = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(root + "/data_v2/todo.txt"));
        bw = new BufferedWriter(new FileWriter(root + "/unvisited.0402"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("#viewPage");
            if (spt.length > 1) line = spt[0];
            if (!urls.contains(line) && !todo.contains(line)) {
                todo.add(line);
                bw.write(line + "\n");
            }
        }
        br.close();
        bw.close();
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
