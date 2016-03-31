package xusheng.misc;

/**
 * Created by angrymidiao on 3/29/16.
 */

public class LocalTester {

    public static void main(String[] args) throws Exception {
        String a = "??";
        System.out.print(new String(a.getBytes("ISO-8859-1"), "GBK"));
    }
}