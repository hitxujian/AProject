package xusheng.experiment;

import fig.basic.LogInfo;

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

    public static String retPath = "/home/xusheng/p1117";

    public static void main(String[] args) throws IOException {
        fileProcess();
        run();
    }

    public static void fileProcess() throws IOException {
        try {
            File root = new File(dir);
            File[] files = root.listFiles();
            for (int i=0; i<files.length; i++) {
                if (files[i].isDirectory()) {
                    String dirIdx = files[i].getAbsolutePath();
                    processSchemaEdge(dirIdx);
                    processCoverInfo(dirIdx);
                    writeRet(retPath + files[i].getName());
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
                while ((line = br.readLine()) != "") {
                    LogInfo.logs(line);
                    String[] spt = line.split(" ");
                    LogInfo.logs(spt);
                    LogInfo.logs(spt.length);
                    String edge;
                    if (spt[1].equals("IsA,")) edge = spt[1] + spt[2];
                    else edge = spt[1];
                    schemaEdges.get(cnt).add(edge);
                }
            }
        }
        br.close();
        LogInfo.logs(path + "/schema processed.");
    }

    public static HashMap<String, Integer> coverInfo;

    public static void processCoverInfo(String path) throws IOException {
        LogInfo.logs("Start to process " + path + "/matrix...");
        coverInfo = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path + "/train_matrix_comp"));
        String line; int cnt = 0;
        br.readLine();
        while (!(line = br.readLine()).startsWith("===")) {
            String[] spt = line.split("\t");
            if (spt[1].equals("+1")) {
                line = br.readLine();
                spt = line.split(" ");
                for (int i=0; i<spt.length; i++) {
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
        LogInfo.logs(path + "/train_matrix_comp processed.");
    }

    public static void writeRet(String path) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        for (Map.Entry<String, Integer> entry: coverInfo.entrySet()) {
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        bw.close();
        LogInfo.logs("write into " + path);
    }

    public static void run() throws IOException {

    }
}
