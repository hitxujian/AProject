package xusheng.experiment;

import fig.basic.LogInfo;
import xusheng.util.struct.MapHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng Luo on 11/27/15.
 *
 * Calculate the tf-idf of schema edges for the whole relation domain;
 */

public class PredRanker {

    public static String dir = "/home/kangqi/workspace/UniformProject/resources/paraphrase/emnlp2015/" +
            "PATTY120_Matt-Fb2m_med_gGD_s20_len3_fb1_sh0_aT0_c150_c21.2_aD1_SF1_SL1_cov0.10_pH10_dt1.0_sz30000_aI1";

    public static String retPath = "/home/xusheng/p1127";

    public static void main(String[] args) throws IOException {
        //fileProcess();
        run();
    }

    public static void fileProcess() throws IOException {
        try {
            File root = new File(dir);
            File[] files = root.listFiles();
            for (int i=0; i<files.length; i++) {
                if (files[i].isDirectory()) {
                    String dirIdx = files[i].getAbsolutePath();
                    File file = new File(dirIdx + "/schema");
                    if (!file.exists()) continue;
                    processSchemaEdge(dirIdx);
                    processCoverInfo(dirIdx);
                    writeRet(retPath + "/" + files[i].getName());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static HashMap<Integer, ArrayList<String>> schemaEdges;

    public static void processSchemaEdge(String path) throws IOException{
        LogInfo.logs("Start to process " + path + "/schema...");
        schemaEdges = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path + "/schema"));
        String line; int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("=")) {
                cnt ++;
                br.readLine();
                schemaEdges.put(cnt, new ArrayList<>());
                while (!(line = br.readLine()).equals("")) {
                    String[] spt = line.split(" ");
                    String edge;
                    if (spt[1].equals("IsA,")) edge = spt[1] + spt[2];
                    else edge = spt[1];
                    schemaEdges.get(cnt).add(edge);
                }
            }
        }
        br.close();
        LogInfo.logs(".../schema processed. Size: %d", schemaEdges.size());
    }

    public static HashMap<String, Integer> coverInfo;

    public static void processCoverInfo(String path) throws IOException {
        LogInfo.logs("Start to process " + path + "/matrix...");
        coverInfo = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path + "/train_matrix_comp"));
        String line; int cnt = 0;
        br.readLine();
        while (!(line = br.readLine()).startsWith("===")) {
            String pos = line.split("\t")[1];
            line = br.readLine();
            if (pos.equals("+1")) {
                String[] spt = line.split(" ");
                for (int i=0; i<spt.length-1; i++) {
                    if (spt[i].equals("1")) {
                        for (String edge : schemaEdges.get(i + 1)) {
                            if (!coverInfo.containsKey(edge)) coverInfo.put(edge, 1);
                            else {
                                int tmp = coverInfo.get(edge) + 1;
                                coverInfo.put(edge, tmp);
                            }
                        }
                    }
                }
            }
        }
        br.close();
        LogInfo.logs(".../train_matrix_comp processed. Size: %d", coverInfo.size());
    }

    public static void writeRet(String path) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        ArrayList<Map.Entry<String, Integer>> sorted = MapHelper.sort(coverInfo);
        for (Map.Entry<String, Integer> entry: sorted) {
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        bw.close();
        LogInfo.logs("write into " + path);
    }

    public static HashMap<String, HashMap<String, Double>> tFreq = new HashMap<>();
    public static HashMap<String, Double> idFreq = new HashMap<>();
    public static final double total = 103;

    public static void run() throws IOException {
        File root = new File(retPath);
        File[] files = root.listFiles();
        HashMap<String, Integer> idf = new HashMap<>();
        for (int i=0; i<files.length; i++) {
            String path = files[i].getAbsolutePath();
            LogInfo.logs("Reading %s...", path);
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            HashMap<String, Integer> tmp = new HashMap<>();
            int sum = 0;
            while ((line = br.readLine()) != null) {
                String[] spt = line.split("\t");
                if (!idf.containsKey(spt[0])) idf.put(spt[0], 1);
                else {
                    int cnt = idf.get(spt[0]);
                    idf.put(spt[0], cnt + 1);
                }
                tmp.put(spt[0], Integer.parseInt(spt[1]));
                sum += Integer.parseInt(spt[1]);
            }
            HashMap<String, Double> tf = new HashMap<>();
            for (Map.Entry<String, Integer> entry: tmp.entrySet()) {
                double a = (double) entry.getValue() / sum;
                tf.put(entry.getKey(), a);
            }
            tFreq.put(files[i].getName(), tf);
            br.close();
        }
        for (Map.Entry<String, Integer> entry: idf.entrySet()) {
            double a = Math.log(total / entry.getValue());
            idFreq.put(entry.getKey(), a);
        }
        LogInfo.logs("Frequency File Read.");

        for (Map.Entry<String, HashMap<String, Double>> entry: tFreq.entrySet()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/p1127-score/" + entry.getKey()));
            ArrayList<Map.Entry<String, Double>> sorted = MapHelper.sort(entry.getValue());
            for (Map.Entry<String, Double> entry1: sorted)
                bw.write(entry1.getKey() + "\t" + entry1.getValue() + "\n");
            bw.close();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/p1127-score/idf-score"));
        ArrayList<Map.Entry<String, Double>> sorted = MapHelper.sort(idFreq);
        for (Map.Entry<String, Double> entry: sorted)
           bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        bw.close();

        String path = "/home/xusheng/p1127-ret";
        for (Map.Entry<String, HashMap<String, Double>> entry: tFreq.entrySet()) {
            String rel = entry.getKey();
            LogInfo.logs("Calculate tf-idf score for %s...", path + "/" + rel);
            HashMap<String, Double> ret = new HashMap<>();
            for (Map.Entry<String, Double> entry1: entry.getValue().entrySet()) {
                double a = entry1.getValue() * idFreq.get(entry1.getKey());
                ret.put(entry1.getKey(), a);
            }
            sorted = MapHelper.sort(ret);
            bw = new BufferedWriter(new FileWriter(path + "/" + rel));
            for (Map.Entry<String, Double> entry1: sorted)
                bw.write(entry1.getKey() + "\t" + entry1.getValue() + "\n");
            bw.close();
        }
    }
}
