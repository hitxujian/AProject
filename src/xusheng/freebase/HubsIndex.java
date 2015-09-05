package xusheng.freebase;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/5/13.
 */
public class HubsIndex {
    public static HashMap<String, String> idxToHub = new HashMap<>();
    public static HashMap<String, String> hubToIdx = new HashMap<>();
    public static HashMap<Integer, String> index = new HashMap<>();
    public static int size;

    public static String getHub(String idx) {
        if (idxToHub.containsKey(idx)) return idxToHub.get(idx);
        else return null;
    }

    public static String getIdx(String hub) {
        if (hubToIdx.containsKey(hub)) return hubToIdx.get(hub);
        else return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            idxToHub.put(spt[0], spt[1]);
            hubToIdx.put(spt[1], spt[0]);
        }
        br.close();
        size = idxToHub.size();
        LogInfo.logs("Hub Index read into memory!");
    }

    public static void createIdx(String file1, String file2, String outFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file1));
        String line = ""; int num = 0;
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            for (int i=1; i<spt.length; i++)
                if (!index.containsValue(spt[i])) {
                    num ++;
                    index.put(num, spt[i]);
                }
        }
        br.close();
        br = new BufferedReader(new FileReader(file2));
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            for (int i=1; i<spt.length; i++)
                if (!index.containsValue(spt[i])) {
                    num ++;
                    index.put(num, spt[i]);
                }
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        for (int i=1; i<=index.size(); i++)
            bw.write(i + "\t" + index.get(i) + "\n");
        bw.close();
    }

    public static void main(String[] args) throws Exception {
        createIdx(args[0], args[1], args[2]);
    }
}
