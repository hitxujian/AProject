package xusheng.nell;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/9/12.
 */
public class EntityIndex {

    private static HashMap<String, String> name2Idx = new HashMap<>();
    private static HashMap<String, String> idx2Name = new HashMap<>();

    public static String getIdx(String name) {
        if (name2Idx.containsKey(name))
            return name2Idx.get(name);
        else return null;
    }

    public static String getName(String idx) {
        if (idx2Name.containsKey(idx))
            return idx2Name.get(idx);
        else return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            name2Idx.put(spt[0], spt[1]);
            idx2Name.put(spt[1], spt[0]);
        }
        br.close();
        LogInfo.logs("NELL Entity Index read into memory!");
    }

    public static void main(String[] args) throws Exception {

    }

}
