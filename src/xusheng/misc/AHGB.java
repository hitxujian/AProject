package xusheng.misc;

import fig.basic.LogInfo;
import fig.basic.Pair;

import java.io.IOException;
import java.util.*;

/**
 * Created by Xusheng on 6/18/2016.
 * AHGB (Approximate to Horizontal Grid Barrier)
 * Consisting of two steps:
 * 1) Horizontal Grid Barrier Selection, which finds a row from A as a horizontal grid barrier.
 * 2) Optimal Movement, which finds the optimal movement strategy for relocating mobile sensors to
 *    the selected horizontal grid barrier, subject to minimize the sum of moving distance.
 */

public class AHGB {

    public static int n, L, K, R, nl, nw;
    public static double[] x, xs, y, ys;
    public static double[][] edges;

    public static void strongDetect() {
        int tarRow = HGBS();
        LogInfo.logs("Target row position: %d", tarRow);

        // construct the bipartite graph
        edges = new double[nl][n];
        if (verbose) LogInfo.logs("Edge Matrix: ");
        for (int i=0; i<nl; i++) {
            String str = "";
            for (int j = 0; j < n; j++) {
                edges[i][j] = findDist((2 * i + 1) * R, (2 * tarRow + 1) * R, x[j + 1], y[j + 1]);
                str += (edges[i][j] + "\t");
            }
            if (verbose) LogInfo.logs(str);
        }
        Hungarian(edges, tarRow);
        printRet();
    }

    // row: barriers/ column: sensors
    public static void Hungarian(double[][] matrix, int tarRow) {
        Set<Integer> set = new HashSet<>();
        for (int i=0; i<nl; i++) {
            double minCost = 2*L*L;
            int minIdx = 0;
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] < minCost && !set.contains(j+1)) {
                    minCost = matrix[i][j];
                    minIdx = j+1;
                }
            }
            y[minIdx] = (2 * tarRow + 1) * R;
            x[minIdx] = (2 * i + 1) * R;
            set.add(minIdx);
        }
        return;
    }

    // Select the Horizontal Grid Barrier
    public static Map<Pair<Integer, Integer>, Double> grids;
    public static int HGBS() {
        nw = nl = L / (2 * R);
        double maxDist = Math.sqrt(2*L*L);
        grids = new HashMap<>();
        for (int i=0; i<nw; i++) {
            for (int j=0; j<nl; j++) {
                Pair<Integer, Integer> idxPair = new Pair<>(2*i+1, 2*j+1);
                grids.put(idxPair, maxDist);
            }
        }
        // find the nearest grid of a sensor, update the distance
        for (int i=1; i<=n; i++) {
            int xPos = ((int) ((x[i]-1)/(2*R)))*2+1;
            int yPos = ((int) ((y[i]-1)/(2*R)))*2+1;
            double dist = findDist(xPos*R, yPos*R, x[i], y[i]);
            if (dist < grids.get(new Pair<>(xPos, yPos)))
                grids.put(new Pair<>(xPos, yPos), dist);
        }
        //find the minimum 1-barrier position
        int minRow = 0;
        double minSum = maxDist*nl;
        for (int i=0; i<nw; i++) {
            double sum = 0;
            for (int j=0; j<nl; j++)
                sum += grids.get(new Pair<>(2*i+1, 2*j+1));
            if (sum < minSum) {
                minSum = sum;
                minRow = i;
            }
        }
        return minRow;
    }

    public static double findDist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    public static void autoTest(int k, int cals) {
        int settingCnt = 0;
        R = 20;
        K = k;
        for (L = 200; L <=800; L+= 200) {
            for (double rate=1.25; rate<=2; rate+=0.25) {
                settingCnt ++;
                LogInfo.begin_track("Testing data setting %d", settingCnt);
                n = (int) (rate * L * K / (2 * R)) + 1;
                LogInfo.logs("n = %d, L = %d, K = %d, R = %d, redundancy rate: %.2f", n, L, K, R, rate);
                long stTime = System.currentTimeMillis();
                for (int numCnt=1; numCnt<=cals; numCnt++) {
                    x = new double[n + 2];
                    xs = new double[n + 2];
                    y = new double[n + 2];
                    ys = new double[n + 2];

                    // generate x positions
                    Set<Integer> set = new HashSet<>();
                    while (set.size() < n) {
                        int num = (int) (Math.random() * (L-1)) + 1;
                        set.add(num);
                    }
                    List<Integer> list = new ArrayList<>(set);
                    Collections.sort(list);
                    for (int i = 0; i < n; i++)
                        xs[i + 1] = x[i + 1] = list.get(i);

                    // generate y positions
                    set = new HashSet<>();
                    while (set.size() < n) {
                        int num = (int) (Math.random() * (L-1)) + 1;
                        set.add(num);
                    }
                    list = new ArrayList<>(set);
                    for (int i = 0; i < n; i++)
                        ys[i + 1] = y[i + 1] = list.get(i);

                    if (verbose) {
                        LogInfo.begin_track("Data #%d:", numCnt);
                        printXY();
                    }
                    strongDetect();
                    if (verbose) LogInfo.end_track();
                }
                long edTime = System.currentTimeMillis();
                long time = edTime - stTime;
                LogInfo.logs("Time: %dms, [n = %d, L = %d, K = %d, R = %d, redundancy rate: %.2f]", time, n, L, K, R, rate);
                LogInfo.end_track();
            }
        }

    }

    public static void printXY() {
        String str = "[";
        for (int j=1; j<=n-1; j++) str += ("(" + x[j] + "," + y[j] + ")\t");
        str += ("(" + x[n] + "," + y[n] + ")]");
        LogInfo.logs(str);
    }

    public static void printRet() {
        if (verbose) LogInfo.logs("Final Result:");
        String initX = "[";
        for (int i=1; i<n; i++)
            initX += ("(" + xs[i]+ "," + ys[i] + ")\t");
        initX += ("(" + xs[n] + "," + ys[n] + ")]");
        LogInfo.logs(initX);

        String finalX = "[";
        for (int i=1; i<n; i++)
            finalX += ("(" + x[i]+ "," + y[i] + ")\t");
        finalX += ("(" + x[n] + "," + y[n] + ")]");
        LogInfo.logs(finalX);
        double totalDis = 0;
        for (int i=1; i<=n; i++) {
            totalDis += Math.sqrt((x[i]-xs[i])*(x[i]-xs[i]) + (y[i]-ys[i])*(y[i]-ys[i]));
        }
        LogInfo.logs("Total movements: %.2f", totalDis);
    }

    public static boolean verbose = false;
    public static void main(String[] args) throws IOException {
        int k = Integer.parseInt(args[0]);
        int cals = Integer.parseInt(args[1]);
        if (args[2].equals("verbose=1")) verbose = true;
        autoTest(k, cals);
    }
}
