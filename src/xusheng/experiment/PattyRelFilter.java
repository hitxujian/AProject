package xusheng.experiment;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Xusheng Luo on 3/9/16.
 */

public class PattyRelFilter {
    private static String patternFp = "/home/data/PATTY/patty-dataset-freebase/" +
            "remove-type-signature/Matt-Fb3m_med/pattern-support-dist.txt";
    private static String instanceFp = "/home/data/PATTY/patty-dataset-freebase/" +
            "remove-type-signature/Matt-Fb3m_med/wikipedia-instances.txt.fb_link";

    public static boolean filter(int idx, int num) {
        LogInfo.logs("Now for %d: %d",num, idx);
        Set<String> curr = map.get(idx);
        LogInfo.logs(curr.toString());
        for (int i=1; i<num; i++) {
            Set<String> set = map.get(index.get(i));
            LogInfo.logs(set.toString());
            int a = 0;
            for (String ele: curr)
                if (set.contains(ele)) a++;
            int b = curr.size() + set.size() - a;
            LogInfo.logs("%d, %d, %.2f", a, b, (double) a / b);
            if ((double) a / b > 0.5) return false;
        }
        return true;
    }

    private static List<Integer> index = new ArrayList<>();
    public static void work() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(patternFp));
        String line; int cnt = 0; index.add(0);
        while ((line = br.readLine()) != null) {
            cnt ++;
            String[] spt = line.split(" \\| ")[0].split(" ");
            int idx = Integer.parseInt(spt[spt.length-1]);
            index.add(idx);
            if (cnt >400 && cnt < 1000 || cnt > 1400 && cnt <3000) {
                if (filter(idx, cnt)) {
                    LogInfo.logs(line);
                }
            }
        }
    }

    private static Map<Integer, Set<String>> map = null;
    public static void readInstanceFile() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(instanceFp));
        map = new HashMap<>();
        String line;
        br.readLine();
        int prev = 0;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            int idx = Integer.parseInt(spt[0]);
            if (idx != prev) {
                //LogInfo.logs("%d: %d", prev, set.size());
                map.put(prev, set);
                set = new HashSet<>();
                prev = idx;
            }
            String pair = spt[1] + spt[2];
            set.add(pair);
        }
        //LogInfo.logs("%d: %d", prev, set.size());
        map.put(prev, set);
        LogInfo.logs("total size: %d", map.size());
        for (Map.Entry<Integer, Set<String>> entry: map.entrySet())
            LogInfo.logs("%d: %s", entry.getKey(), entry.getValue().toString());
    }

    public static void main(String[] args) throws Exception {
        readInstanceFile();
        work();
    }
}
