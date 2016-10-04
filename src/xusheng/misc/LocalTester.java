package xusheng.misc;

import fig.basic.LogInfo;


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
        String line = "Andrew Jackson (March 1<a href=\"Battle%20of%20New%20Orleans\">Battle of New Orleans</a>June" +
                " 8, 1845) was an American statesman who served as the seventh <a href=\"President%20of%20the%20United" +
                "%20States\">President of the United States</a> from 1829 to 1837.";
        String s = "<a>123</a><a>456</a><a>789</a>";
        Pattern pat = Pattern.compile("<a>(.*?)</a>");
        Matcher mat = pat.matcher(s);
        boolean rs = mat.find();
        for(int i=1;i<=mat.groupCount();i++){
            System.out.println(mat.group(i));
        }
    }
}