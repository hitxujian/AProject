package xusheng.util.nlp;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

/**
 * Created by Xusheng on 2015/9/7.
 * Load Preprosition words from file.
 */

public class PrepWordLoader {

    private static HashSet<String> prepSet = null;

    public static HashSet<String> getPrepSet(String file) throws Exception{
        if (prepSet != null) return prepSet;
        prepSet = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) prepSet.add(line);
        LogInfo.logs("%d Prep Words Loaded.", prepSet.size());
        return prepSet;
    }
}
