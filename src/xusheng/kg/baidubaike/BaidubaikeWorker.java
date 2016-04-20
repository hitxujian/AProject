package xusheng.kg.baidubaike;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xusheng Luo on 3/29/16.
 * Usage: process raw baidubaike htms using multi threads working.
 */

public class BaidubaikeWorker implements Runnable{
    public static String root = "/home/xusheng/crawl";
    public static int curr = -1, end = -1, inc = 0;
    public static BufferedWriter idxBw, infoBw;
    public static HashMap<String, Integer> urlMap = new HashMap<>();

    public void run() {
        while (true) {
            try {
                int idx = getCurr();
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
        idxBw = new BufferedWriter(new FileWriter(root + "/entity.index"));
        infoBw = new BufferedWriter(new FileWriter(root + "/infobox.triple"));
        curr = 1; end = 300;
        LogInfo.logs("Begin to Construct Article Idx and Extract Infobox...");
        int numOfThreads = 8;
        BaidubaikeWorker workThread = new BaidubaikeWorker();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...");
        multi.runMultiThread();
        LogInfo.end_track();
        idxBw.close();
        infoBw.close();
    }

    public static synchronized void add2Urls(String url) throws IOException{
        if (! urlMap.containsKey(url)) {
            inc ++;
            urlMap.put(url, inc);
            idxBw.write(url + "\t" + inc + "\n");
        }
    }

    public static synchronized void writeTriple(String triple) throws IOException {
        infoBw.write(triple + "\n");
    }

    public static void extractInfobox(int idx) throws Exception {
        int cnt = idx * 10000;
        String folderName = (cnt-10000+1) + "-" + cnt;
        LogInfo.begin_track("Entering into %s...", folderName);
        for (int i=cnt-10000+1; i<=cnt; i++) {
            String fp = root + "/data_v2/" + folderName + "/" + i + ".html";
            BufferedReader br = new BufferedReader(new FileReader(fp));
            String url = br.readLine().split("com")[1];
            add2Urls(url);
            String line;
            String title = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("<title>")) {
                    title = line.split(">")[1].split("<")[0].split("_")[0];
                    continue;
                }
                if (line.startsWith("<dt class=\"basicInfo-item name\"")) {
                    String itemName = line.split(">")[1].split("<")[0];
                    String[] spt = itemName.split("&nbsp;");
                    itemName = "";
                    for (int j=0; j<spt.length; j++)
                        itemName += spt[j];
                    LogInfo.logs(itemName);
                    br.readLine();
                    line = br.readLine();
                    spt = line.split("<.+?>");
                    String itemValue = "";
                    for (int j=0; j<spt.length; j++) {
                        itemValue += spt[j];
                    }
                    LogInfo.logs(itemValue);
                    if (title!=null) {
                        bw.write(title + "\t" + itemName + "\t" + itemValue + "\n");
                    }
                }
            }
            br.close();
        }
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
