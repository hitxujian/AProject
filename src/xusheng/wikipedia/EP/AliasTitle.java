package xusheng.wikipedia.EP;

import fig.basic.LogInfo;
import xusheng.util.log.LogUpgrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/6/11.
 */
public class AliasTitle {

    public static HashMap<String, String> aliasToTitle = new HashMap<>();
    public static HashMap<String, ArrayList<String>> titleToAlias = new HashMap<>();

    public static String getTitle(String alias) {
        if (aliasToTitle.containsKey(alias))
            return aliasToTitle.get(alias);
        else return null;
    }

    public static ArrayList<String> getAlias(String title) {
        if (titleToAlias.containsKey(title))
            return titleToAlias.get(title);
        else return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = ""; int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            LogUpgrader.showLine(num, 500000);
            String[] spt = line.split("\t");
            aliasToTitle.put(spt[1], spt[0]);
            if (! titleToAlias.containsKey(spt[0]))
                titleToAlias.put(spt[0], new ArrayList<String>());
            titleToAlias.get(spt[0]).add(spt[1]);
        }
        br.close();
        LogInfo.logs("Wikipedia Title Alias read into memory!");
    }

    public static boolean match(String stra, String strb) {
        String[] spt1 = stra.split(" ");
        String[] spt2 = strb.split(" ");
        ArrayList<String> tmp = new ArrayList<>(Arrays.asList(spt2));
        int cnt = 0;
        for (int i=0; i<spt1.length; i++)
            if (tmp.contains(spt1[i])) cnt ++;
        double weight = (double) cnt / tmp.size();
        if (weight > 0.3) return true;
        else return false;
    }

    public static void construct(String inFile, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String title = TitleIndex.getTitle(spt[0]).toLowerCase();
            if (match(title, spt[1].toLowerCase())) {
                bw.write(title + "\t" + spt[1] + "\n");
            }
        }
        br.close();
        bw.close();
        LogInfo.logs("Filtered Table Constructed!");
    }

    public static void main(String[] args) throws Exception {
        TitleIndex.initialize(args[0]);
        construct(args[1], args[2]);
    }
}
