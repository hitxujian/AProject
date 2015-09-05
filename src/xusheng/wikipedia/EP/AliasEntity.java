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
public class AliasEntity {

    public static HashMap<String, String> aliasToEntity = new HashMap<>();

    public static String getEntity(String alias) {
        if (aliasToEntity.containsKey(alias))
            return aliasToEntity.get(alias);
        else return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = ""; int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            LogUpgrader.showLine(num, 500000);
            String[] spt = line.split("\t");
            aliasToEntity.put(spt[0], spt[1]);
        }
        br.close();
        LogInfo.logs("Wikipedia alias to Freebase entity read into file! size: %d", aliasToEntity.size());
    }

    public static void construct(String inFile, String outFile) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line = ""; int num = 0;
        while ((line = br.readLine()) != null) {
            num ++;
            LogUpgrader.showLine(num, 500000);
            String[] spt = line.split("\t");
            if (EntityTitle.getEntity(spt[0]) != null)
                bw.write(spt[1] + "\t" + EntityTitle.getEntity(spt[0]) + "\n");
        }
        br.close();
        bw.close();
        LogInfo.logs("Wikipedia alias to Freebase entity file constructed! total lines: %d", num);
    }

    public static void main(String[] args) throws Exception {
        EntityTitle.initialize(args[2]);
        construct(args[0], args[1]);
    }
}
