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
                //LogInfo.begin_track(line);
                double prob = Double.parseDouble(line.split(" ")[7]);
                //LogInfo.logs(prob);
                line = br.readLine();
                int numOfBones = Integer.parseInt(line.split(" ")[3]);
                //LogInfo.logs(numOfBones);
                String skeleton = "";
                for (int i = 0; i<numOfBones; i++) {
                    line = br.readLine();
                    skeleton += line;
                }
                if (skeletons.containsKey(skeleton)) {
                    skeletons.get(skeleton).numOfSchema ++;
                    skeletons.get(skeleton).prob += prob;
                } else skeletons.put(skeleton, new Skeleton(prob));

                boolean flag = true;
                while (!(line = br.readLine()).equals("")) {
                    flag = false;
                    String edge = line.substring(6);
                    //LogInfo.logs(edge);
                    skeletons.get(skeleton).addEdge(edge);
                }
                // empty edge counts
                if (flag) {
                    String edge = "EMPTY";
                    skeletons.get(skeleton).addEdge(edge);
                }
                //LogInfo.end_track();
            }
        }
        br.close();
        LogInfo.begin_track("Info for " + name);
        LogInfo.logs("Size of skeletons: " + skeletons.size());
        int cnt = 0;
        double sum = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter(home + "/similarity/" + name));
        for (Map.Entry<String, Skeleton> entry: skeletons.entrySet()) {
            cnt ++;
            // calculate the probability for each constraint
            entry.getValue().calcuEdgeProb();
            // calculate the probability for each skeleton+constraint combination
            entry.getValue().calcuCombProb(entry.getKey());
            LogInfo.logs("Skeleton %d => numOfSchema: %d, prob: %f, numOfEdge: %d",
                    cnt, entry.getValue().numOfSchema, entry.getValue().prob,
                    entry.getValue().edgeCount.size());
            HashMap<String, Double> ret = entry.getValue().combProb;
            for (Map.Entry<String, Double> entry1: ret.entrySet()) {
                sum += entry1.getValue();
                bw.write(entry1.getKey() + "\t\t\t" + entry1.getValue() + "\n");
            }
        }
        LogInfo.logs("===> sum of distribution: %f", sum);
        bw.close();
        LogInfo.end_track();
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
        if (STR.equals("KL")) ret = kl(distribution_1, distribution_2);
        LogInfo.logs(name_1 + "\t" + name_2 + "\t:\t" + ret + "\n");
    }

    public static double cos(HashMap<String, Double> distribution_1, HashMap<String, Double> distribution_2) {
        double sum = 0;
        for (Map.Entry<String, Double> entry: distribution_1.entrySet()) {
            sum += entry.getValue() * entry.getValue();
        }
        sum = Math.sqrt(sum);
        HashMap<String, Double> new_1 = new HashMap<>();
        for (Map.Entry<String, Double> entry: distribution_1.entrySet()) {
            new_1.put(entry.getKey(), entry.getValue() / sum);
        }
        sum = 0;
        for (Map.Entry<String, Double> entry: distribution_2.entrySet()) {
            sum += entry.getValue() * entry.getValue();
        }
        sum = Math.sqrt(sum);
        HashMap<String, Double> new_2 = new HashMap<>();
        for (Map.Entry<String, Double> entry: distribution_2.entrySet()) {
            new_1.put(entry.getKey(), entry.getValue() / sum);
        }
        return _cos(new_1, new_2);
    }

    public static double _cos(HashMap<String, Double> distribution_1, HashMap<String, Double> distribution_2) {
        double ret = 0;
        for (Map.Entry<String, Double> entry: distribution_1.entrySet()) {
            if (distribution_2.containsKey(entry.getKey()))
                ret += distribution_2.get(entry.getKey()) * entry.getValue();
        }
        return ret;
    }

    public static double kl(HashMap<String, Double> distribution_1, HashMap<String, Double> distribution_2) {
        double ret_1 = 0, ret_2 = 0;
        for (Map.Entry<String, Double> entry : distribution_1.entrySet()) {
            if (distribution_2.containsKey(entry.getKey())) {
                double p = distribution_2.get(entry.getKey());
                double q = entry.getValue();
                double term = p * Math.log(p / q);
                ret_1 += term;
                term = q * Math.log(q / p);
                ret_2 += term;
            }
        }
        double ret = 2 * ret_1 * ret_2 / (ret_1 + ret_2);
        return ret;
    }

    public static HashMap<Integer, Integer> pairs = null;
    public static void getPairs() throws IOException {
        pairs = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(home + "/filtered-20-EP"));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("#")) continue;
            String[] spt = line.substring(1).split("\t");
            //LogInfo.logs(spt[0] + "\t" + spt[1]);
            pairs.put(Integer.parseInt(spt[0]), Integer.parseInt(spt[1]));
        }
        br.close();
        LogInfo.logs("Relation Pairs Read, size: %d, real size: 20", pairs.size());
    }
}
