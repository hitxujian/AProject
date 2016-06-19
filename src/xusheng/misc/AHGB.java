package xusheng.misc;

import fig.basic.LogInfo;
import fig.basic.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
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
    public static Set<Integer> used;

    public static void strongDetect() {
        used = new HashSet<>();
        nw = nl = L / (2 * R);
        for (int k=1; k<=K; k++) {
            LogInfo.begin_track("Starting round K = %d", k);
            // Select the Horizontal Grid Barrier
            //int tarRow = HGBS();
            int tarRow = 0;
            LogInfo.logs("Target row position: %d @ K = %d", tarRow, k);

            // Construct the bipartite graph
            edges = new double[nl][n];
            if (verbose) LogInfo.logs("Edge Matrix @ K = %d: ", k);
            for (int i = 0; i < nl; i++) {
                String str = "";
                for (int j = 0; j < n; j++) {
                    if (! used.contains(j+1))
                        edges[i][j] = findDist((2 * i + 1) * R, (2 * tarRow + 1) * R, x[j + 1], y[j + 1]);
                    else
                        edges[i][j] = 2 * L * L;
                    String tmp = String.format("%.2f", edges[i][j]);
                    str += (tmp + "\t");
                }
                if (verbose) LogInfo.logs(str);
            }
            //naive(edges, tarRow);
            Hungarian hungarian = new Hungarian(edges);
            int[] ret = hungarian.execute();
            LogInfo.logs("Matching result by Hungarian Algo. @ K = %d: ", k);
            String str = "";
            for (int i = 0; i < ret.length; i++) {
                str += ret[i] + "\t";
                int sidx = ret[i];
                x[sidx + 1] = (2 * i + 1) * R;
                y[sidx + 1] = (2 * tarRow + 1) * R;
                used.add(sidx + 1);
            }
            LogInfo.logs("[%s]", str);
            LogInfo.end_track();
        }
        printRet();
        //printRet4Draw();
    }

    // row: barriers/ column: sensors
    public static void naive(double[][] matrix, int tarRow) {
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
        double maxDist = Math.sqrt(2*L*L);
        grids = new HashMap<>();
        for (int i=0; i<nw; i++) {
            for (int j=0; j<nl; j++) {
                Pair<Integer, Integer> idxPair = new Pair<>(2*i+1, 2*j+1);
                grids.put(idxPair, maxDist);
            }
        }
        // Find the nearest grid of a sensor, update the distance
        for (int i=1; i<=n; i++) {
            if (used.contains(i)) continue;
            int xPos = ((int) ((x[i]-1)/(2*R)))*2+1;
            int yPos = ((int) ((y[i]-1)/(2*R)))*2+1;
            double dist = findDist(xPos*R, yPos*R, x[i], y[i]);
            if (dist < grids.get(new Pair<>(xPos, yPos)))
                grids.put(new Pair<>(xPos, yPos), dist);
        }
        // Find the minimum 1-barrier position
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

                    // Generate x positions
                    Set<Integer> set = new HashSet<>();
                    while (set.size() < n) {
                        int num = (int) (Math.random() * (L-1)) + 1;
                        set.add(num);
                    }
                    List<Integer> list = new ArrayList<>(set);
                    Collections.sort(list);
                    for (int i = 0; i < n; i++)
                        xs[i + 1] = x[i + 1] = list.get(i);

                    // Generate y positions
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

    public static void printRet4Draw() {
        String initX = "";
        for (int i=1; i<n; i++)
            initX += xs[i] + "\t";
        initX += xs[n];
        LogInfo.logs(initX);

        String initY = "";
        for (int i=1; i<n; i++)
            initY += ys[i] + "\t";
        initY += ys[n];
        LogInfo.logs(initY);

        String finalX = "";
        for (int i=1; i<n; i++)
            finalX += x[i] + "\t";
        finalX += x[n];
        LogInfo.logs(finalX);

        String finalY = "";
        for (int i=1; i<n; i++)
            finalY += y[i] + "\t";
        finalY += y[n];
        LogInfo.logs(finalY);

        double totalDis = 0;
        for (int i=1; i<=n; i++) {
            totalDis += Math.sqrt((x[i]-xs[i])*(x[i]-xs[i]) + (y[i]-ys[i])*(y[i]-ys[i]));
        }
        LogInfo.logs("Total movements: %.2f", totalDis);
    }

    public static void printRet() {
        if (verbose) LogInfo.logs("Final Result:");
        String initX = "[";
        for (int i=1; i<n; i++)
            initX += ("(" + String.format("%.0f",xs[i])+ "," + String.format("%.0f",ys[i]) + ")\t");
        initX += ("(" + String.format("%.0f",xs[n]) + "," + String.format("%.0f",ys[n]) + ")]");
        LogInfo.logs(initX);

        String finalX = "[";
        for (int i=1; i<n; i++)
            finalX += ("(" + String.format("%.0f",x[i])+ "," + String.format("%.0f",y[i]) + ")\t");
        finalX += ("(" + String.format("%.0f",x[n]) + "," + String.format("%.0f",y[n]) + ")]");
        LogInfo.logs(finalX);
        double totalDis = 0;
        for (int i=1; i<=n; i++) {
            totalDis += Math.sqrt((x[i]-xs[i])*(x[i]-xs[i]) + (y[i]-ys[i])*(y[i]-ys[i]));
        }
        LogInfo.logs("Total movements: %.2f", totalDis);
    }

    public static void readData(String inputFp) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFp));
        String line = br.readLine();
        String spt[] = line.split("\t");
        n = Integer.parseInt(spt[0]);
        L = Integer.parseInt(spt[1]);
        K = Integer.parseInt(spt[2]);
        R = Integer.parseInt(spt[3]);
        x = new double[n+2];
        xs = new double[n+2];
        y = new double[n+2];
        ys = new double[n+2];
        line = br.readLine();
        spt = line.split("\t");
        for (int i=0; i<spt.length; i++)
            xs[i+1] = x[i+1] = Double.parseDouble(spt[i]);
        line = br.readLine();
        spt = line.split("\t");
        for (int i=0; i<spt.length; i++)
            ys[i+1] = y[i+1] = Double.parseDouble(spt[i]);
        br.close();
        LogInfo.begin_track("Input data loaded");
        LogInfo.logs("n = %d, L = %d, K = %d, R = %d", n, L, K, R);
        printXY();
        LogInfo.end_track();
    }

    public static boolean verbose = false;
    public static void main(String[] args) throws IOException {
        if (!args[0].equals("AUTO")) {
            if (args[1].equals("verbose=1")) verbose = true;
            readData(args[0]);
            strongDetect();
        } else {
            int k = Integer.parseInt(args[1]);
            int cals = Integer.parseInt(args[2]);
            if (args[3].equals("verbose=1")) verbose = true;
            autoTest(k, cals);
        }
    }
}
