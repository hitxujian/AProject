package xusheng.misc;

import java.io.IOException;

/**
 * Created by Xusheng on 6/18/2016.
 * AHGB (Approximate to Horizontal Grid Barrier)
 * Consisting of two steps:
 * 1) Horizontal Grid Barrier Selection, which finds a row from A as a horizontal grid barrier.
 * 2) Optimal Movement, which finds the optimal movement strategy for relocating mobile sensors to
 *    the selected horizontal grid barrier, subject to minimize the sum of moving distance.
 */

public class AHGB {

    public static int n, L, K, R;
    public static double[] x, xs, y, ys;

    public static void strongDetect() {

    }

    // Select the Horizontal Grid Barrier
    public static void HGBS() {
        constructGrid();

    }



    public static void constructGrid() {
        int nl = L / (2 * R);
        double maxY = 0;
        for (int i=1; i<=n; i++)
            if (maxY < y[i]) maxY = y[i];
    }

    public static void main(String[] args) throws IOException {

    }
}
