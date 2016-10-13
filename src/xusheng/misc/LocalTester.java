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
        String line = " <p id=\"openCatp\" style=\"display:none\">开放分类：<a target=\"_blank\" title=\"日本漫画\" " +
                "href=\"http://fenlei.baike.com/%E6%97%A5%E6%9C%AC%E6%BC%AB%E7%94%BB/?prd=zhengwenye_left_kaifangfenlei\">" +
                "日本漫画</a><a target=\"_blank\" title=\"漫画\" href=\"http://fenlei.baike.com/%E6%BC%AB%E7%94%BB" +
                "/?prd=zhengwenye_left_kaifangfenlei\">漫画</a></p>\n";
        Pattern pat = Pattern.compile("title=\"(.*?)\" href");
        Matcher mat = pat.matcher(line);
        while (mat.find()) {
            LogInfo.logs(urlDecode(mat.group(0)));
            LogInfo.logs(urlDecode(mat.group(1)));
            //line= line.replace(mat.group(0), urlDecode(mat.group(1)));
            //LogInfo.logs(line);
        }
    }
}