package xusheng.kg.baike;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng Luo on 3/29/16.
 * Usage: process raw baike htms using multi threads working.
 * During the extracting procedure, the entities are indexed in the mean time.
 */

public class BaidubaikeWorker implements Runnable{
    public static String root = "/home/xusheng/crawl";
    public static int curr = -1, end = -1, inc = 0;
    public static BufferedWriter textBw, infoBw;
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
        infoBw = new BufferedWriter(new FileWriter(root + "/infobox.url"));
        textBw = new BufferedWriter(new FileWriter(root + "/infobox.text"));
        curr = 1; end = 300;
        LogInfo.logs("Begin to Construct Article Idx and Extract Infobox...");
        int numOfThreads = 8;
        BaidubaikeWorker workThread = new BaidubaikeWorker();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
        textBw.close();
        infoBw.close();
        // write results into files
        writeIdx();
        writeIdxText();
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

    public static synchronized void writeTriple(String triple, BufferedWriter bw) throws IOException {
        bw.write(triple);
    }

    public static boolean isChinese(char c) {
        return c >= 0x4E00 &&  c <= 0x9FA5;
    }

    public static void extractInfobox(int idx) throws Exception {
        int cnt = idx * 10000;
        String folderName = (cnt-10000+1) + "-" + cnt;
        LogInfo.logs("Entering into %s... [%s]", folderName, new Date().toString());
        StringBuffer content = new StringBuffer();
        int numOfChar = 0;
        for (int i=cnt-10000+1; i<=cnt; i++) {
            String fp = root + "/data_v2/" + folderName + "/" + i + ".html";
            BufferedReader br = new BufferedReader(new FileReader(fp));
            String url = br.readLine().split("com")[1];
            // when meet a new url, add it to map and write it into file
            int leftIdx = add2Urls(url);
            String line;
            String title = "";
            // control the starting position of content
            boolean _StartKeepContent = false;
            while ((line = br.readLine()) != null) {
                if (!_StartKeepContent && line.startsWith("<span class=\"description\">"))
                    _StartKeepContent = true;
                if (_StartKeepContent) {
                    for (char c : line.toCharArray()) {
                        if (isChinese(c) && c!='\n') {
                            content.append(c);
                            numOfChar++;
                            if (numOfChar % 100 == 0) content.append("\n");
                        }
                    }
                }
                if (line.startsWith("<title>")) {
                    if (line.split(">").length < 2) break;
                    title = line.split(">")[1].split("<")[0].split("_")[0];
                    add2AnchorTexts(leftIdx, title);
                    continue;
                }
                if (line.startsWith("<dt class=\"basicInfo-item name\"")) {
                    //------- deal with relation -------
                    String itemName = line.split(">")[1].split("<")[0];
                    String[] spt = itemName.split("&nbsp;");
                    itemName = "";
                    for (int j=0; j<spt.length; j++)
                        itemName += spt[j];
                    //------- deal with object -------
                    br.readLine();
                    line = br.readLine();
                    spt = line.split("<.+?>");
                    // get the plain text of object
                    String itemValue = "";
                    for (int j=0; j<spt.length; j++) {
                        itemValue += spt[j];
                    }
                    spt = line.split("href=\"");
                    String href, triple = "";
                    /*
                        write to "infobox.url"
                     */
                    // if meet a link in the object
                    if (spt.length == 2 && line.startsWith("<a") && line.endsWith("a>")) {
                        href = spt[1].split("\">")[0];
                        int rightIdx = add2Urls(href);
                        add2AnchorTexts(rightIdx, itemValue);
                        triple  = url + "\t" + itemName + "\t" + href + "\n";
                        writeTriple(triple, infoBw);
                    // if no link or part of link, then write plain text
                    } else {
                        /*
                            Here is a problem need to consider later:
                            If there is only part of object text is linked,
                            should we make use of it?
                            now we just keep it in entity.name file
                         */
                        triple = url + "\t" + itemName + "\t" + itemValue + "\n";
                        writeTriple(triple, infoBw);
                        // add to entity.name to record alias
                        if (spt.length > 2) {
                            for (int j=1; j<spt.length; j++) {
                                String[] sptt = spt[j].split("\">");
                                href = sptt[0];
                                String thisText = sptt[1].split("</a>")[0];
                                int rightIdx = add2Urls(href);
                                add2AnchorTexts(rightIdx, thisText);
                            }
                        }
                    }
                    /*
                        write to "infobox.text
                     */
                    triple = title + "\t" + itemName + "\t" + itemValue + "\n";
                    writeTriple(triple, textBw);
                }
            }
            br.close();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/content/" + idx + ".txt"));
        bw.write(content.toString());
        bw.close();
        LogInfo.logs("Job %s is Finished. [%s]", folderName, new Date().toString());
    }

    public static void writeIdx() throws IOException {
        LogInfo.begin_track("Now writing entity-idx into file...");
        int cnt = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/entity.index"));
        for (Map.Entry<String, Integer> entry: urlMap.entrySet()) {
            cnt ++;
            LogUpgrader.showLine(cnt, 10000);
            bw.write(entry.getValue() + "\t" + entry.getKey() + "\n");
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
            bw.write(entry.getKey().toString());
            for (String str : entry.getValue()) bw.write("\t" + str);
            bw.write("\n");
        }
        bw.close();
        LogInfo.end_track();
    }

    public static void extractURLs() throws IOException {
        Set<String> urls = new HashSet<>();
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/visited.0423"));
        int cnt = 0;
        while (cnt < 3690000) {
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
        BufferedReader br = new BufferedReader(new FileReader(root + "/data_v2/unvisited.0420"));
        bw = new BufferedWriter(new FileWriter(root + "/unvisited.0423"));
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
        //extractURLs();
    }
}
