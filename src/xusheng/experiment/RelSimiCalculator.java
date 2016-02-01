package xusheng.experiment;

import fig.basic.LogInfo;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 1/31/16.
 */

public class RelSimiCalculator {

    public static String dir = "/home/kangqi/workspace/UniformProject/" +
            "resources/paraphrase/ranktest/schema_v4_dc_p42_smart/intermediate";
    public static String home = "/home/xusheng/AProject/data/patty";
    public static String STR = null;

    public static void main(String[] args) throws IOException {
        if (args[0].equals("Task_1"))
            getFiles();
        else if (args[0].equals("Task_2")) {
            STR = args[1];
            calcuSimi();
        }
    }

    public static void getFiles() {
        try {
            File root = new File(dir);
            File[] files = root.listFiles();
            for (int i=0; i<files.length; i++) {
                if (files[i].isDirectory()) {
                    String file = files[i].getAbsolutePath() +
                            "/probVisualize/prob_0.5_l2_0.001_visualize";
                    workForFile(file, files[i].getName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void workForFile(String file, String name) throws IOException {
        HashMap<String, Skeleton> skeletons = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) {
            if (line.startsWith("==")) {
                LogInfo.begin_track(line);
                double prob = Double.parseDouble(line.split(" ")[7]);
                LogInfo.logs(prob);
                line = br.readLine();
                int numOfBones = Integer.parseInt(line.split(" ")[3]);
                LogInfo.logs(numOfBones);
                String skeleton = "";
                for (int i = 0; i<numOfBones; i++) {
                    line = br.readLine();
                    skeleton += line;
                }
                if (skeletons.containsKey(skeleton)) {
                    skeletons.get(skeleton).numOfSchema ++;
                    skeletons.get(skeleton).prob += prob;
                } else skeletons.put(skeleton, new Skeleton(prob));

                while (!(line = br.readLine()).equals("")) {
                    String edge = line.substring(6);
                    LogInfo.logs(edge);
                    skeletons.get(skeleton).addEdge(edge);
                }
                LogInfo.end_track();
            }
        }
        br.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter(home + "/similarity" + name));
        for (Map.Entry<String, Skeleton> entry: skeletons.entrySet()) {
            // calculate the probability for each constraint
            entry.getValue().calcuEdgeProb();
            // calculate the probability for each skeleton+constraint combination
            entry.getValue().calcuCombProb(entry.getKey());
            HashMap<String, Double> ret = entry.getValue().combProb;
            for (Map.Entry<String, Double> entry1: ret.entrySet())
                bw.write(entry1.getKey() + "\t\t\t" + entry1.getValue() + "\n");
        }
        bw.close();
    }

    public static void calcuSimi() throws IOException {
        getPairs();
        for (Map.Entry<Integer, Integer> entry: pairs.entrySet()) {
            String name_1 = home + "/similarity/" + entry.getKey() + "_" + entry.getKey();
            String name_2 = home + "/similarity/" + entry.getValue() + "_" + entry.getValue();
            if (new File(name_1).exists() && new File(name_2).exists()) {
                work4Pair(name_1, name_2, entry.getKey(), entry.getValue());
            }
        }

    }

    public static void work4Pair(String file_1, String file_2, int name_1, int name_2) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file_1));
        HashMap<String, Double> distribution_1 = new HashMap<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t\t\t");
            distribution_1.put(spt[0], Double.parseDouble(spt[1]));
        }
        br.close();
        br = new BufferedReader(new FileReader(file_2));
        HashMap<String, Double> distribution_2 = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t\t\t");
            distribution_2.put(spt[0], Double.parseDouble(spt[1]));
        }
        br.close();

        double ret = 0;
        if (STR.equals("COS")) ret = cos(distribution_1, distribution_2);
        LogInfo.logs(name_1 + "\t" + name_2 + "\t:\t" + ret + "\n");
    }

    public static double cos(HashMap<String, Double> distribution_1, HashMap<String, Double> distribution_2) {
        double ret = 0;
        for (Map.Entry<String, Double> entry: distribution_1.entrySet()) {
            if (distribution_2.containsKey(entry.getKey()))
                ret += distribution_2.get(entry.getKey()) * entry.getValue();
        }
        return ret;
    }

    public static HashMap<Integer, Integer> pairs = null;
    public static void getPairs() throws IOException {
        pairs = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(home + "/filtered-20-EP"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.substring(1).split("\t");
            pairs.put(Integer.parseInt(spt[0]), Integer.parseInt(spt[1]));
        }
        br.close();
        LogInfo.logs("Relation Pairs Read, size: %d", pairs.size());
    }
}
