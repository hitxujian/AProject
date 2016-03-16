package xusheng.course;

import fig.basic.LogInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by angrymidiao on 3/15/16.
 */

public class MLhw1 {
    public static String trainFp = "/home/xusheng/ml/train.csv";
    public static String testFp = "/home/xusheng/ml/test.csv";
    public static String plotFp = "/home/xusheng/ml/plotdata";
    public static List<Integer> prices = new ArrayList<>();
    public static List<Integer> sqfts = new ArrayList<>();

    public static void readData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(trainFp));
        BufferedWriter bw = new BufferedWriter(new FileWriter(plotFp));
        String line = "";
        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split(",");
            int price = Integer.parseInt(spt[2]);
            int sqft_living = Integer.parseInt(spt[5]);
            prices.add(price);
            sqfts.add(sqft_living);
            bw.write(price + "\t" + sqft_living + "\n");
        }
        LogInfo.logs(prices.size() + "\t" + sqfts.size());
    }

    public static void gd() {

    }

    public static void newton() {

    }

    public static void normal() {

    }

    public static void main(String[] args) throws IOException {
        readData();
        gd();
        newton();
        normal();
    }
}
