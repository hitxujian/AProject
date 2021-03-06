package xusheng.util.struct;

import fig.basic.LogInfo;

import java.net.URLDecoder;

/**
 * Created by Xusheng on 9/1/2016.
 */

public class UrlHelper {

    public static String urlDecode(String url) {
        String ret = url;
        try {
            while (true) {
                ret = ret.replaceAll("% +", "%");    //correct error encodes like "% 9B" (should be %9B)
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

    public static void writeRet(String ret) {
        System.out.println(urlDecode(ret));
    }

    public static void main(String[] args) throws Exception {
        writeRet("http://comments.youku.com/comments/~ajax/vpcommentContent.html?__callback=vpcommentContent_html&__ap=%7B%22videoid%22%3A%22427636374%22%2C%22showid%22%3A%22305119%22%2C%22isAjax%22%3A1%2C%22sid%22%3A%22%22%2C%22page%22%3A1%2C%22chkpgc%22%3A0%2C%22last_modify%22%3A%22%22%7D");}
}