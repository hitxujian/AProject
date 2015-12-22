package xusheng.util.nlp;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

/**
 * Created by Xusheng on 2015/9/7.
 * Load Stop Words from file.
 */

public class StopWordLoader {

    private static HashSet<String> stopSet = null;

    public static HashSet<String> getStopSet(String file) throws Exception {
        if (stopSet != null) return stopSet;
        stopSet = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) stopSet.add(line);
        LogInfo.logs("%d Stop Words Loaded.", stopSet.size());
        return stopSet;
    }
}
