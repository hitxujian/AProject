package xusheng.misc;

import java.util.*;

/**
 * Created by angrymidiao on 3/29/16.
 */

public class LocalTester {

    public static void main(String[] args) throws Exception {
        Set<Integer> set = new HashSet<>();
        while (set.size() < 10) {
            int num = (int) (Math.random() * (20+1));
            set.add(num);
        }
        List<Integer> list = new ArrayList<>(set);
        Collections.sort(list);
        System.out.println(list.toString());
    }
}