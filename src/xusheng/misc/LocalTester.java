package xusheng.misc;

import java.util.*;

/**
 * Created by angrymidiao on 3/29/16.
 */

public class LocalTester {

    public static void main(String[] args) throws Exception {
        double[][] matrix = {{90, 75, 75, 80},
                             {35, 85, 55, 65},
                             {125, 95, 90, 105},
                             {45, 110, 95, 115}};
        Hungarian hungarian = new Hungarian(matrix);
        int[] ret = hungarian.execute();
        for (int i=0; i<ret.length; i++) System.out.println(ret[i]);
    }
}