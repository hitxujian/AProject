package xusheng.kg.baike;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xusheng on 8/5/16.
 * Scan all the crawled pages and save the visited urls into "visited.DATE".
 */

public class VisitedPageScanner {

    public static String root = "/home/xusheng/crawl/data_v2";

    public static void extractURLs(String today, String yesterday, int num) throws IOException {
        Set<String> urls = new HashSet<>();
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/visited." + today));
        int cnt = 0;
        while (cnt < num) {
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
        BufferedReader br = new BufferedReader(new FileReader(root + "/unvisited." + yesterday));
        bw = new BufferedWriter(new FileWriter(root + "/unvisited." + today));
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

    public static void main(String[] args) throws IOException {
        extractURLs(args[0], args[1], Integer.parseInt(args[2]));
    }
}
