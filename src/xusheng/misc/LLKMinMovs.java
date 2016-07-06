package xusheng.misc;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Xusheng on 6/8/16.
 * Algorithm Final Project
 *
 * In our layer-based algorithm, named as LLK-MinMovs algorithm,
 * we try to find the two closest overlaps and select a cheaper one to fill a target gap.
 * Note that such movement process should maintain current coverage level.
 * That means the algorithm will not bring new gaps or downgrade the current coverage quality.
 */

public class LLKMinMovs {

    public static int n, L, K, R;
    public static int[] x, xs, x0;

    public static void weakDetect() {
        x[0] = -R; x[n+1] = L + R;
        for (int k =1; k<=K; k++) {
            if (verbose) LogInfo.begin_track("Starting round K=%d", k);
            // At the beginning of each iteration, record it's initial position
            for (int i=0; i<=n+1; i++) x0[i] = x[i];

            for (int i=0; i<=n+1-k; i++) {
                while (! isCovered(i, k)) {
                    int l = findOverLap(i, k, "Left");
                    int r = findOverLap(i, k, "Right");
                    if (l == -1 && r == -1) {
                        LogInfo.logs("Cannot find any overlap. No solution. ");
                        return;
                    }
                    if (Lcost(i, l, k) < Rcost(i, r, k))
                        moveByLeft(l, i, k, Ldist(l, i, k));
                    else
                        moveByRight(i, r, k, -Rdist(i, r, k));
                }
            }
            if (verbose) LogInfo.end_track();
        }
        printRet();
    }

    // Check whether [x[i] + R, x[i+k] - R] is k-line covered
    public static boolean isCovered(int i, int k) {
        if (x[i] + R >= x[i+k] - R)
            return true;
        return false;
    }

    // Find the nearest overlap
    public static int findOverLap(int i, int k, String dir) {
        boolean left = false;
        if (dir.equals("Left")) left = true;
        if (left) i -= k;
        else i += k;
        while (i >= 0 && i+k <= n+1) {
            if (x[i+k]-R < x[i]+R) return i; // "=" means exactly k-line covered
            if (left) i -= k;
            else i += k;
        }
        return -1;
    }

    // Left overlap cost
    public static int Lcost(int i, int l, int k) {
        if (l == -1) return L;
        int pos = 0, neg = 0;
        for (int j=l+k; j<=i; j+=k) {
            if (x[j] >=  x0[j]) pos ++;
            else neg ++;
        }
        return pos - neg;
    }

    // Right overlap cost
    public static int Rcost(int i, int r, int k) {
        if (r == -1) return L;
        int num = 0;
        for (int j=r; j>=i+k; j-=k) num ++;
        return num;
    }

    public static void moveByLeft(int l, int i, int k, int dist) {
        if (verbose) LogInfo.logs("Use left overlap(%d, %d) to fill gap(%d, %d) by dist = %d", l, k, i, k, dist);
        if (verbose) printX();
        for (int j=l+k; j<=i; j++)
            x[j] += dist;
        if (verbose) printX();
    }

    public static void moveByRight(int i, int r, int k, int dist) {
        if (verbose) LogInfo.logs("Use right overlap(%d, %d) to fill gap(%d, %d) by dist = %d:", r, k, i, k, dist);
        if (verbose) printX();
        for (int j=r; j>=i+k; j--)
            x[j] += dist;
        if (verbose) printX();
    }

    // Left overlap shift distance. Need to consider the effect shift window
    // to maintain the current coverage level.
    public static int Ldist(int l, int i, int k) {
        int tmp = Math.min(x[i+k]-x[i]-2*R, x[l]-x[l+k]+2*R);
        int minShift = L;
        for (int j=l+k; j<=i; j+=k) {
            if (x[j] < x0[j] && (x0[j] - x[j]) < minShift)
                minShift = x0[j] - x[j];
        }
        return Math.min(tmp, minShift);
    }

    // Right overlap shift distance
    public static int Rdist(int i, int r, int k) {
        return Math.min(x[i+k]-x[i]-2*R, x[r]-x[r+k]+2*R);
    }

    public static void printX() {
        String str = "[";
        for (int j=1; j<n; j++) {
            str += (x[j] + "\t");
        }
        str += x[n] + "]";
        LogInfo.logs(str);
    }

    public static void printX4draw() {
        String str = "";
        int dist = 0;
        for (int j=1; j<=n; j++) {
            if (x[j] > xs[j])
                str += (x[j] + "(+) & ");
            else if (x[j] < xs[j])
                str += (x[j] + "(-) & ");
            else
                str += (x[j] + " & ");
            dist += Math.abs(x[j] - xs[j]);
        }
        str += String.valueOf(dist) + " \\\\";
        LogInfo.logs(str);
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
                    x = new int[n + 2];
                    xs = new int[n + 2];
                    x0 = new int[n + 2];
                    Set<Integer> set = new HashSet<>();
                    while (set.size() < n) {
                        int num = (int) (Math.random() * (L + 1));
                        set.add(num);
                    }
                    List<Integer> list = new ArrayList<>(set);
                    Collections.sort(list);
                    for (int i = 0; i < n; i++)
                        xs[i + 1] = x[i + 1] = list.get(i);
                    if (verbose) {
                        LogInfo.begin_track("Data #%d:", numCnt);
                        printX();
                    }
                    weakDetect();
                    if (verbose) LogInfo.end_track();
                }
                long edTime = System.currentTimeMillis();
                long time = edTime - stTime;
                LogInfo.logs("Time: %dms, [n = %d, L = %d, K = %d, R = %d, redundancy rate: %.2f]", time, n, L, K, R, rate);
                LogInfo.end_track();
            }
        }

    }

    public static void readData(String inputFp) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFp));
        String line = br.readLine();
        String spt[] = line.split("\t");
        n = Integer.parseInt(spt[0]);
        L = Integer.parseInt(spt[1]);
        K = Integer.parseInt(spt[2]);
        R = Integer.parseInt(spt[3]);
        x = new int[n+2];
        xs = new int[n+2];
        x0 = new int[n+2];
        while ((line = br.readLine()) != null) {
            spt = line.split("\t");
            for (int i=0; i<spt.length; i++)
                xs[i+1] = x[i+1] = Integer.parseInt(spt[i]);
        }
        br.close();
        LogInfo.begin_track("Input data loaded");
        LogInfo.logs("n = %d, L = %d, K = %d, R = %d", n, L, K, R);
        printX();
        LogInfo.end_track();
    }

    public static void printRet() {
        if (verbose) LogInfo.logs("Final Result:");
        String initX = "[";
        for (int i=1; i<n; i++) initX += (xs[i] + "\t");
        initX += (xs[n] + "]");
        LogInfo.logs(initX);
        String finalX = "[";
        for (int i=1; i<n; i++) finalX += (x[i] + "\t");
        finalX += (x[n] + "]");
        LogInfo.logs(finalX);
        int totalDis = 0;
        for (int i=1; i<=n; i++) {
            totalDis += Math.abs(x[i] - xs[i]);
        }
        LogInfo.logs("Total movements: %d", totalDis);
    }

    public static boolean verbose = false;
    public static void main(String[] args) throws IOException {
        if (args[0].equals("AUTO")) {
            int k = Integer.parseInt(args[1]);
            int cals = Integer.parseInt(args[2]);
            if (args[3].equals("verbose=1")) verbose = true;
            autoTest(k, cals);
        }
        else {
            readData(args[0]);
            if (args[1].equals("verbose=1")) verbose = true;
            weakDetect();
        }
    }
}