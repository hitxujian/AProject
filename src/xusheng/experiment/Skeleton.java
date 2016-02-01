package xusheng.experiment;

import fig.basic.LogInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xusheng on 2/1/16.
 */

public class Skeleton {

    public int numOfSchema;
    public double prob;
    public HashMap<String, Integer> edgeCount;
    public HashMap<String, Double> edgeProb;
    public HashMap<String, Double> combProb;

    public Skeleton(double _prob) {
        numOfSchema = 1;
        prob = _prob;
        edgeCount = new HashMap<>();
        edgeProb = new HashMap<>();
        combProb = new HashMap<>();
    }

    public void addEdge(String edge) {
        if (edgeCount.containsKey(edge)) {
            int tmp = edgeCount.get(edge) + 1;
            edgeCount.put(edge, tmp);
        } else edgeCount.put(edge, 1);
    }

    public void calcuEdgeProb() {
        for (Map.Entry<String, Integer> entry: edgeCount.entrySet()) {
            edgeProb.put(entry.getKey(), (double) entry.getValue() / numOfSchema);
        }
    }

    public void calcuCombProb(String skeleton) {
        double sum = 0;
        HashMap<String, Double> tmp = new HashMap<>();
        for (Map.Entry<String, Double> entry: edgeProb.entrySet()) {
            String key = skeleton + "\t" + entry.getKey();
            double _prob = prob * entry.getValue();
            sum += (_prob * _prob);
            tmp.put(key, _prob);
        }
        sum = Math.sqrt(sum);
        LogInfo.logs("sum = %f", sum);
        for (Map.Entry<String, Double> entry: tmp.entrySet()) {
            combProb.put(entry.getKey(), entry.getValue() / sum);
        }
    }
}
