package xusheng.misc;

/**
 * Created by angrymidiao on 3/29/16.
 */

public class LocalTester {

    public static void main(String[] args) throws Exception {
        String a = "<a href = ;;;;>hello</a>-<a herefshdskd>hi</a>";
        String[] spt = a.split("<.+?>");
        System.out.println(spt.length);
        for (int i=0; i<spt.length; i++)
            System.out.println(spt[i]);
    }
}