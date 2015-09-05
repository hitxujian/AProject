package xusheng.freebase;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by angrymidiao on 4/28/15.
 */
public class TypeIndex {
    public static HashMap<String, String> typeToIdx = new HashMap<>();
    public static HashMap<String, String> idxToType = new HashMap<>();
    public static boolean verbose = true;

    public static String getType(String idx) {
        if (idxToType.containsKey(idx))
            return idxToType.get(idx);
        else
            return null;
    }

    public static String getIdx(String type) {
        if (typeToIdx.containsKey(type))
            return typeToIdx.get(type);
        else
            return null;
    }

    public static void initialize(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line=br.readLine()) != null) {
            String[] spt = line.split("\t");
            typeToIdx.put(spt[1], spt[0]);
            idxToType.put(spt[0], spt[1]);
        }
        br.close();
        if (verbose) LogInfo.logs("Freebase Type-Index read into memory!");
    }

    public static void main(String[] args) throws Exception {
        initialize(args[0]);
    }
}
