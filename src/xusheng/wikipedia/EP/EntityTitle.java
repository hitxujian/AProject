package xusheng.wikipedia.EP;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/6/11.
 */
public class EntityTitle {

    public static HashMap<String, String> entityToTitle = new HashMap<>();
    public static HashMap<String, String> titleToEntity = new HashMap<>();

    public static String getEntity(String title) {
        if (titleToEntity.containsKey(title))
            return titleToEntity.get(title);
        else return null;
    }

    public static String getTitle(String entity) {
        if (entityToTitle.containsKey(entity))
            return entityToTitle.get(entity);
        else return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = ""; int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            LogUpgrader.showLine(num, 500000);
            String[] spt = line.split("\t");
            entityToTitle.put(spt[0], spt[1]);
            titleToEntity.put(spt[1], spt[0]);
        }
        br.close();
        LogInfo.logs("Freebase entity To Wikipedia title read into memory! size: %d", entityToTitle.size());
    }

    public static void processFbWikiFile(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            LogUpgrader.showLine(num, 500000);
            String[] spt = line.split("\t");
            String[] linkspt = spt[2].split("/");
            String title = linkspt[linkspt.length-1];
            title = title.substring(0, title.length()-1);
            String[] tmp = title.split("_");
            StringBuffer ret = new StringBuffer();
            ret.append(tmp[0]);
            for (int i=1; i<tmp.length; i++) ret.append(" " + tmp[i]);
            if (TitleIndex.getIdx(ret.toString()) != null)
                bw.write(spt[0] + "\t" + TitleIndex.getIdx(ret.toString()) + "\n");
            //bw.write(spt[0] + "\t" + ret + "\t" + TitleIndex.getIdx(ret.toString()) + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Freebase entity To Wikipedia title processed! total lines: %d", num);
    }

    public static void main(String[] args) throws Exception {
        TitleIndex.initialize(args[2]);
        processFbWikiFile(args[0], args[1]);
    }
}
