package xusheng.misc;

import fig.basic.LogInfo;
import sun.rmi.runtime.Log;


import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by angrymidiao on 3/29/16.
 */

public class LocalTester {


    public static String urlDecode(String url) {
        String ret = url;
        try {
            while (true) {
                ret = ret.replaceAll("% +", "%");	//correct error encodes like "% 9B" (should be %9B)
                String decode = URLDecoder.decode(ret, "UTF-8");
                if (!decode.equals(ret))
                    ret = decode;
                else
                    break;
            }
        } catch (Exception ex) {
            LogInfo.logs("[T%s] Fail to Decode url [%s].", Thread.currentThread().getName(), ret);
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        /*String line = "Andrew Jackson (March 1<a href=\"Battle%20of%20New%20Orleans\">Battle of New Orle" +
                "ans</a>June 8, 1845) was an American statesman who served as the seventh <a href=\"President%20of%20the%20United" +
                "%20States\">President of the United States</a> from 1829 to 1837.";
        Pattern pat = Pattern.compile("<a href=\"(.*?)\"(.*?)</a>");
        Matcher mat = pat.matcher(line);
        while (mat.find()) {
            LogInfo.logs(urlDecode(mat.group(0)));
            LogInfo.logs(urlDecode(mat.group(1)));
            String newLine = line.replace(mat.group(0), urlDecode(mat.group(1)));
            line = newLine;
            LogInfo.logs(line);
        } */
        String word = "'[[fdsf]],";
        String[] spt = word.split("\\[\\[|\\]\\]");
        for (String str: spt)
            LogInfo.logs(str);
    }
}