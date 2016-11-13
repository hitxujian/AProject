package xusheng.kg.wiki;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;
import xusheng.util.struct.MapHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Xusheng on 23/10/2016.
 * 1. Calculate the prior probability of P(e|m)
 * by extract all the [[e|m]] from the raw wikipedia xml
 * 2. Calculate the popularity of all entities
 * by counting their mentioning times
 */
public class PriorGenerator {

    public static String rootFp = "/home/xusheng";
    public static Map<String, HashMap<String, Integer>> appearMap = new HashMap<>();
    public static Map<String, Integer> entTimeMap = new HashMap<>();

    public static void generate() throws IOException {
        File f = new File(rootFp + "/wikipedia/enwiki-20160920-pages-articles-multistream.xml");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        //f = new File(rootFp + "/nn/data/wikipedia/prior.txt");
        f = new File(rootFp + "/nn/data/wikipedia/popularity.txt");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            cnt ++;
            LogUpgrader.showLine(cnt, 1000000);
            Pattern pattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String raw = matcher.group(1);
                String entity = raw.toLowerCase();
                String mention = raw; // maintain original case
                String[] spt = raw.split("\\|");
                if (spt.length > 1) {
                    entity = spt[0].toLowerCase();
                    mention = spt[1];
                }
                //add2apperMap(entity, mention);
                add2entTimeMap(entity);
                if (cnt < 100) LogInfo.logs("[link]\t" + mention + " : " + entity);
            }
            /*
            pattern = Pattern.compile("<title>(.*?)</title>");
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                String raw = matcher.group(1);
                add2apperMap(raw.toLowerCase(), raw);
                if (cnt < 100) LogInfo.logs("[title]\t" + raw + " : " + raw.toLowerCase());
            }
            */
        }
        /*
        for (Map.Entry<String, HashMap<String, Integer>> entry: appearMap.entrySet()) {
            String mention = entry.getKey();
            HashMap<String, Integer> entityMap = entry.getValue();
            int sum = 0;
            for (Map.Entry<String, Integer> entry1: entityMap.entrySet())
                sum += entry1.getValue();
            ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(entityMap);
            for (int i=0; i<sorted.size(); i++)
                bw.write(String.format("%s\t%s\t%.4f\n", mention, sorted.get(i).getKey(),
                        (double) sorted.get(i).getValue() / sum));
        }*/

        for (Map.Entry<String, Integer> entry: entTimeMap.entrySet()) {
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }

        br.close();
        bw.close();
    }

    public static void add2apperMap(String entity, String mention) {
        if (!appearMap.containsKey(mention))
            appearMap.put(mention, new HashMap<>());
        if (!appearMap.get(mention).containsKey(entity))
            appearMap.get(mention).put(entity, 1);
        else {
            int tmp = appearMap.get(mention).get(entity) + 1;
            appearMap.get(mention).put(entity, tmp);
        }
    }

    public static void add2entTimeMap(String entity) {
        if (!entTimeMap.containsKey(entity))
            entTimeMap.put(entity, 1);
        else {
            int tmp = entTimeMap.get(entity) + 1;
            entTimeMap.put(entity, tmp);
        }
    }


    public static void main(String[] args) throws IOException {
        generate();
    }
}
