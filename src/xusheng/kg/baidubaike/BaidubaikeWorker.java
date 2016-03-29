package xusheng.kg.baidubaike;

import fig.basic.LogInfo;

import java.io.*;

/**
 * Created by Xusheng Luo on 3/29/16.
 * Usage: process raw baidubaike htms.
 */

public class BaidubaikeWorker {
    public static String root = "/home/xusheng/crawl";

    public static void extractInfobox() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(root + "/infobox.triple"));
        int cnt = 0;
        while (cnt < 100000) {
            cnt += 10000;
            String name = (cnt-10000+1) + "-" + cnt;
            for (int i=cnt-10000+1; i<=cnt; i++) {
                String fp = root + "/data/" + name + "/" + i + ".html";
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
                        String itemValue = line;
                        if (line.startsWith("<a")) {
                            spt = line.split("<(.*)>");
                            itemValue = "";
                            for (int j=0; j<spt.length; j++) {
                                itemValue += spt[j];
                            }
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

    public static void main(String[] args) throws IOException {
        extractInfobox();
    }
}
