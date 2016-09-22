package xusheng.misc;

import fig.basic.LogInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.*;

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
        String line = "<span><a href=\"http://www.baike.com/wiki/%E4%B8%AD%E5%9B%BD\" title=\"中国\" target=\"_blank\"><img src=\"./六小龄童_互动百科_files/chn.png\"></a>中国</span>";
        List<Integer> list = new ArrayList<>(1);
        LogInfo.logs(list.toString());
    }
}