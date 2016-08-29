package xusheng.kg.baike;

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
 * Created by Xusheng on 8/26/2016.
 * Usage: process raw baike html using multi threads working.
 * During the extracting procedure, different alias for entities are recorded.
 */

public class HudongbaikeParser implements Runnable{
    public static String rootFp = "/home/xusheng/starry/hudongbaike";
    public static int curr = -1, end = -1, inc = 0;

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

    public static BufferedWriter bwTriple = null;
    public static List<String> triples = null;
    public static void multiThreadWork() throws Exception {
        readTasks();
        curr = 0; end = taskList.size();
        triples = new ArrayList<>();
        bwTriple = new BufferedWriter(new FileWriter(rootFp + "/infobox/triples.tsv"));
        LogInfo.logs("Task: Extract infobox for Hudong Baike (%d pages). [%s]", end, new Date().toString());
        int numOfThreads = 32;
        HudongbaikeParser workThread = new HudongbaikeParser();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        //---------------------------------------------------------------------------------------------------------
        LogInfo.logs("[log] Now writting infobox triples... [%s]", new Date().toString());
        for (String triple : triples)
            bwTriple.write(triple + "\n");
        bwTriple.close();
        // --------------------------------------------------------------------------------------------------------
        LogInfo.logs("[log] Triples written. Now writting entity-name map... [%s]", new Date().toString());
        BufferedWriter bwName = new BufferedWriter(new FileWriter(rootFp + "/infobox/entName.tsv"));
        for (Map.Entry<Integer, Set<String>> entry : entNameMap.entrySet()) {
            bwName.write(entry.getKey());
            for (String name : entry.getValue())
                bwName.write("\t" + name);
            bwName.write("\n");
        }
        bwName.close();
        LogInfo.logs("[log] Entity-name map written. Size: %d. [%s]", entNameMap.size(), new Date().toString());
        // --------------------------------------------------------------------------------------------------------
        LogInfo.end_track();
    }

    public static void extractInfobox(int idx) throws Exception {
        String task = taskList.get(idx);
        String[] spt = task.split("\t");
        int index = Integer.parseInt(spt[0]);
        String name = spt[2];
        addToEntNameFile(index, name);
        int st = index / 10000 * 10000 + 1;
        int ed = st + 9999;
        String fp = rootFp + "/" + st + "-" + ed;
        if (spt[1].equals("1"))
            fp += "/" + index + "_wiki.html";
        else
            fp += "/" + index + "_search.html";
        if (! new File(fp).exists()) {
            LogInfo.logs("[error] Page %d does not exist.", idx);
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(fp));
        String line;
        while ((line = br.readLine()) != null) {
            // infobox start-point!
            if (line.trim().startsWith("<div class=\"module-edit")) {
                while (!line.trim().equals("</body>") && line != null) {
                    line = br.readLine();
                    if (line.trim().startsWith("<strong>")) {
                        try {
                            String relation = line.split("<strong>")[1].split("</strong>")[0];
                            relation = relation.substring(0,relation.length()-1); // why-2? => "rel: ".
                            while ((line = br.readLine()).trim().startsWith("<span>")) {
                                if (line.equals("<span>")) line = br.readLine(); // context is in the next line
                                String obj = extractObj(line);
                                //String obj = line.split("<span>")[1].split("</span>")[0];
                                String triple = name + "\t" + relation + "\t" + obj;
                                triples.add(triple);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        br.close();
        //LogInfo.logs("[T%s] Page %d parsed. [%s]", Thread.currentThread().getName(), index, new Date().toString());
    }

    public static String extractObj(String line) {
        Document doc = Jsoup.parse(line);
        String text = doc.body().text();
        String ret = text;
        Elements links = doc.select("a");
        if (links.size() == 0)
            return ret;
        else if (links.size() > 1)
            LogInfo.logs("[attention]\t%s", line);
        for (int i=0; i<links.size(); i++) {
            Element link = links.get(i);
            String linkHref = link.attr("href");
            if (linkHref.equals("")) continue;
            String linkText = link.text();
            if (linkText.equals("")) continue;
            String url = urlDecode(linkHref);
            if (urlEntMap.containsKey(url)) {
                addToEntNameFile(urlEntMap.get(url), linkText);
                if (links.size() == 1 &&
                        (text.equals(linkText) || text.equals(url.split("/")[url.split("/").length-1])))
                    ret = url;
            }
            else
                LogInfo.logs("[error] %s cannot find its index. [%s]", url, new Date().toString());

        }
        return ret;
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

    public static Map<Integer, Set<String>> entNameMap = new HashMap<>();
    public static synchronized void addToEntNameFile(int idx, String url) {
        if (!entNameMap.containsKey(idx))
            entNameMap.put(idx, new HashSet<>());
        String[] spt = url.split("/");
        String name = spt[spt.length-1];
        entNameMap.get(idx).add(name);
    }

    public static synchronized void writeTriple(String triple) throws IOException {
        bwTriple.write(triple + "\n");
        bwTriple.flush();
    }

    public static List<String> taskList = null;
    public static Map<String, Integer> urlEntMap = null;
    public static void readTasks() throws IOException {
        String[] nameList = new String[] {"kangqi.tsv",
                                          "darkstar.tsv",
                                          "xusheng_1.tsv", "xusheng_2.tsv"};
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
        LogInfo.logs("[log] Tasks loaded. Size: %d. Start writing entity-index map...", taskList.size());
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/infobox/entIdx.tsv"));
        for (Map.Entry<String, Integer> entry: urlEntMap.entrySet()) {
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        bw.close();
        LogInfo.logs("[log] Entity-index map written.");
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
