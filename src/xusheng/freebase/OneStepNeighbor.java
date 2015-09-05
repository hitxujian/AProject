package xusheng.freebase;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Administrator on 2015/5/20.
 */
public class OneStepNeighbor {

    public static HashMap<String, String> leftSet = new HashMap<>();
    public static HashMap<String, String> rightSet = new HashMap<>();

    public static String SimConnected(String lIdx, String rIdx) {
        if (!leftSet.containsKey(lIdx) || !rightSet.containsKey(rIdx))
            return null;
        String[] left = leftSet.get(lIdx).split(" ");
        String[] right = rightSet.get(rIdx).split(" ");
        HashSet<String> lset = new HashSet<>(), rset = new HashSet<>();
        for (String str: left) {
            String tmp = str.split("#")[0];
            lset.add(tmp);
        }
        for (String str: right) {
            String tmp = str.split("#")[0];
            rset.add(tmp);
        }
        for (String lstr: lset)
            if (rset.contains(lstr)) return EntityIndex.getMid(lstr);
        return null;
    }

    public static ArrayList<Triple> connected(String lIdx, String rIdx) {
        if (!leftSet.containsKey(lIdx) || !rightSet.containsKey(rIdx))
            return null;
        String[] left = leftSet.get(lIdx).split(" ");
        String[] right = rightSet.get(rIdx).split(" ");
        HashMap<String, String> lset = new HashMap(), rset = new HashMap();
        ArrayList<Triple> ret = new ArrayList<>();

        for (String str: left) {
            String[] spt = str.split("#");
            lset.put(spt[0], spt[1]);
            if (spt[0].equals(rIdx)) {
                Triple triple = new Triple(PredicateIndex.getPre(spt[1]));
                ret.add(triple);
            }
        }
        for (String str: right) {
            String[] spt = str.split("#");
            rset.put(spt[0], spt[1]);
        }

        for (Map.Entry<String, String> entry: lset.entrySet()) {
            String mid = entry.getKey();
            if (rset.containsKey(mid)) {
                Triple triple = new Triple(PredicateIndex.getPre(entry.getValue()),
                        PredicateIndex.getPre(rset.get(mid)), EntityIndex.getMid(mid));
                ret.add(triple);
            }
        }
        if (ret.size() != 0) return ret;
        return null;
    }

    public static void initialize(String lFile, String rFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(lFile));
        LogInfo.begin_track("Start to read left and right entities relations...");
        String line = ""; int num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            if (num % 100000 == 0) LogInfo.logs("left %d lines processed...", num);
            String[] spt = line.split("\t");
            leftSet.put(spt[0], spt[1].trim());
        }
        br.close();
        LogInfo.logs("Left part read into memory!");
        br = new BufferedReader(new FileReader(rFile));
        num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            if (num % 100000 == 0) LogInfo.logs("right %d lines processed...", num);
            String[] spt = line.split("\t");
            rightSet.put(spt[0], spt[1].trim());
        }
        br.close();
        LogInfo.logs("right part read into memory!");
        LogInfo.end_track();
    }

    public static void initialize(String lFile, String rFile, String eFile, String pFile) throws Exception {
        EntityIndex.initialize(eFile);
        PredicateIndex.initialize(pFile);

        BufferedReader br = new BufferedReader(new FileReader(lFile));
        LogInfo.begin_track("Start to read left and right entities relations...");
        String line = ""; int num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            if (num % 100000 == 0) LogInfo.logs("left %d lines processed...", num);
            String[] spt = line.split("\t");
            leftSet.put(spt[0], spt[1].trim());
        }
        br.close();
        LogInfo.logs("Left part read into memory!");
        br = new BufferedReader(new FileReader(rFile));
        num = 0;
        while ((line=br.readLine()) != null) {
            num ++;
            if (num % 100000 == 0) LogInfo.logs("right %d lines processed...", num);
            String[] spt = line.split("\t");
            rightSet.put(spt[0], spt[1].trim());
        }
        br.close();
        LogInfo.logs("right part read into memory!");
        LogInfo.end_track();
    }

    public static void main(String[] args) throws Exception{
        initialize(args[0], args[1], args[2], args[3]);
        Scanner scanner = new Scanner(System.in);
        String cmd ="";
        while (!(cmd = scanner.next()).equals("q")) {
            String[] spt = cmd.split("#");
            ArrayList<Triple> ret = connected(spt[0], spt[1]);
            System.out.println(ret.toString());
        }
    }

}
