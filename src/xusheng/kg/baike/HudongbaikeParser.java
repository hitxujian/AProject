package xusheng.kg.baike;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 8/26/2016.
 * Usage: process raw baike htms using multi threads working.
 * During the extracting procedure, the entities are indexed in the mean time.
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
    public static void multiThreadWork() throws Exception {
        readTasks();
        curr = 0; end = taskList.size();
        bwTriple = new BufferedWriter(new FileWriter(rootFp + "/infobox/triples.tsv"));

        LogInfo.logs("Task: Extract infobox for Hudong Baike (%d pages). [%s]", end, new Date().toString());
        int numOfThreads = 32;
        HudongbaikeParser workThread = new HudongbaikeParser();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
        bwTriple.close();
    }

    public static void extractInfobox(int idx) throws Exception {
        String task = taskList.get(idx);
        String[] spt = task.split("\t");
        int index = Integer.parseInt(spt[0]);
        String name = spt[2];
        int st = index / 10000 * 10000;
        int ed = st + 10000;
        String fp = rootFp + "/" + st + "-" + ed;
        if (spt[1].equals("1"))
            fp += "/" + index + "_wiki.html";
        else
            fp += "/" + index + "_search.html";
        if (! new File(fp).exists()) {
            LogInfo.logs("Cannot find \"%s\".", fp);
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
                            relation = relation.substring(0,relation.length()-2); // why-2? => "rel: ".
                            while ((line = br.readLine()).trim().startsWith("<span>")) {
                                String obj = extractObj(line);
                                //String obj = line.split("<span>")[1].split("</span>")[0];
                                String triple = name + "\t" + relation + "\t" + obj;
                                writeTriple(triple);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        br.close();
        LogInfo.logs("[log] Page %d extracted. [%s]", index, new Date().toString());
    }

    public static String extractObj(String line) {
        String ret = "";
        String obj = line.split("<span>")[1].split("</span>")[0];
        if (!obj.contains("href")) ret = obj;
        else {
            //todo
        }
        return ret;
    }

    public static Map<Integer, Set<String>> entNameMap = new HashMap<>();
    public static synchronized void addToEntNameFile(int idx, String name) {
        if (!entNameMap.containsKey(idx))
            entNameMap.put(idx, new HashSet<>());
        entNameMap.get(idx).add(name);
    }

    public static synchronized void writeTriple(String triple) throws IOException {
        bwTriple.write(triple + "\n");
        bwTriple.flush();
    }

    public static List<String> taskList = null;
    public static void readTasks() throws IOException {
        String[] nameList = new String[] {"kangqi.tsv",
                                          "darkstar.tsv",
                                          "xusheng.tsv"};
        taskList = new ArrayList<>();
        for (String str: nameList) {
            String fp = rootFp + "/saved_" + str;
            if (! new File(fp).exists()) continue;
            BufferedReader br = new BufferedReader(new FileReader(fp));
            String line;
            while ((line = br.readLine()) != null) {
                String[] spt = line.split("\t");
                if (spt[1].equals("1") || spt[1].equals("2"))
                    taskList.add(line);
            }
            br.close();
        }
        LogInfo.logs("Tasks loaded. Size: %d.", taskList.size());
    }

    public static void main(String[] args) throws Exception {
        multiThreadWork();
    }
}
