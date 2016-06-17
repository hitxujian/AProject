package xusheng.misc;

import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
    public static String root = "/home/xusheng";
    public static String inputFp = root + "/input.txt";

    public static int n, L, K, R;
    public static int[] x = new int[n+2];
    public static int[] x0 = new int[n+2];

    public static int[] work() {
        x[0] = -R; x[n+1] = L + R;
        for (int k =1; k<=K; k++) {
            // At the beginning of each iteration, record it's initial position
            for (int i=0; i<=n+1; i++) x0[i] = x[i];

            for (int i=0; i<=n+1-k; i++) {
                while (! isCovered(i, k)) {
                    int l = findOverLap(i, k, "Left");
                    int r = findOverLap(i, k, "Right");
                    if (l == -1 && r == -1) {
                        LogInfo.logs("Cannot find any overlap. No solution. ");
                        return null;
                    }
                    if (Lcost(i, l, k) < Rcost(i, r, k))
                        moveByLeft(l, i, k, Ldist(l, i, k));
                    else
                        moveByRight(i, r, k, -Rdist(i, r, k));
                }
            }
        }
        return x;
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
        if (left) i--;
        else i++;
        while (i >= 0 && i+k <= n+1 ) {
            if (x[i+k]-R < x[i]+R) return i; // "=" means exactly k-line covered
            if (left) i--;
            else i++;
        }
        return -1;
    }

    // Left overlap cost
    public static int Lcost(int i, int l, int k) {
        if (l == -1) return -L;
        int pos = 0, neg = 0;
        for (int j=l+k; j<=i; j+=k) {
            if (x[j] >=  x0[j]) pos ++;
            else neg ++;
        }
        return pos - neg;
    }

    // Right overlap cost
    public static int Rcost(int i, int r, int k) {
        if (r == -1) return -L;
        int num = 0;
        for (int j=r; j>=i+k; j-=k) num ++;
        return num;
    }

    public static void moveByLeft(int l, int i, int k, int dist) {
        for (int j=l+k; j<=i; j++)
            x[j] += dist;
    }

    public static void moveByRight(int i, int r, int k, int dist) {
        for (int j=r; j>=i+k; j--)
            x[j] -= dist;
    }

    // Left overlap shift distance. Need to consider the effect shift window
    // to maintain the current coverage level.
    public static int Ldist(int l, int i, int k) {
        int tmp = min(x[i+k]-x[i]-2*R, x[l]-x[l+k]+2*R);
        int minShift = L;
        for (int j=l+k; j<=i; j+=k) {
            if (x[j] < x0[j] && (x0[j] - x[j]) < minShift)
                minShift = x0[j] - x[j];
        }
        return min(tmp, minShift);
    }

    // Right overlap shift distance
    public static int Rdist(int i, int r, int k) {
        return min(x[i+k]-x[i]-2*R, x[r]-x[r+k]+2*R);
    }

    public static int min(int x, int y) {
        if (x < y) return x;
        else return y;
    }

    public static void readData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFp));
        String line = br.readLine();
        String spt[] = line.split("\t");
        n = Integer.parseInt(spt[0]);
        L = Integer.parseInt(spt[1]);
        K = Integer.parseInt(spt[2]);
        R = Integer.parseInt(spt[3]);
        while ((line = br.readLine()) != null) {
            spt = line.split("\t");
            for (int i=0; i<spt.length; i++)
                x[i+1] = Integer.parseInt(spt[i]);
        }
        br.close();
        LogInfo.logs("Input data loaded.");
    }

    public static void main(String[] args) throws IOException {
        readData();
        work();
    }
}
