package xusheng.kg.baidubaike;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xusheng Luo on 3/29/16.
 * Usage: process raw baidubaike htms.
 */

public class BaidubaikeWorker {
    public static String root = "/home/xusheng/crawl";

    public static void extractInfobox() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/infobox.triple"));
        int cnt = 0;
        while (cnt < 250000) {
            cnt += 10000;
            String name = (cnt-10000+1) + "-" + cnt;
            for (int i=cnt-10000+1; i<=cnt; i++) {
                String fp = root + "/data_v2/" + name + "/" + i + ".html";
                LogInfo.begin_track("Entering into %s...", fp);
                BufferedReader br = new BufferedReader(new FileReader(fp));
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
                LogInfo.end_track();
            }
        }
        bw.close();
    }

    public static void extractURLs() throws IOException {
        Set<String> urls = new HashSet<>();
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/visited.0402"));
        int cnt = 0;
        while (cnt < 260000) {
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
            if (!urls.contains(line) && !todo.contains(line)) {
                todo.add(line);
                bw.write(line + "\n");
            }
        }
        br.close();
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        //extractInfobox();
        extractURLs();
    }
}
