package xusheng.kg.baike.link.structure;

import fig.basic.LogInfo;

import java.util.*;

/**
 * Created by Xusheng on 9/5/2016.
 * Undirected graph
 */

public class Graph {
    private Map<Integer, Set<Integer>> linkedList;
    private int source;
    private Set<Integer> ends;
    public int numOfPath;

    public Graph(int stIdx) {
        source = stIdx;
        ends = new HashSet<>();
        numOfPath = 0;
        linkedList = new HashMap<>();
        linkedList.put(source, new HashSet<>());
    }

    private void addEdge(int lidx, int ridx) {
        add(lidx, ridx);
        add(ridx, lidx);
    }

    private void add(int key, int val) {
        if (!linkedList.containsKey(key))
            linkedList.put(key, new HashSet<>());
        linkedList.get(key).add(val);
    }

    public void addPath(List<Integer> path) {
        for (int i=0; i<path.size()-1; i++)
            addEdge(path.get(i), path.get(i+1));
        ends.add(path.get(path.size()-1));
        numOfPath ++;
    }

    public void printGraph() {
        LogInfo.begin_track("Graph from Source [%d] to Targets %s", source, ends.toString());
        for (Map.Entry<Integer, Set<Integer>> entry: linkedList.entrySet()) {
            LogInfo.logs("%d -> (%s).", entry.getKey(), entry.getValue().toString());
        }
    }

    public int pageRank() {
        if (ends.size() == 0) return -1;
        int maxi = 0, ret = -1;
        for (int ed: ends) {
            if (linkedList.containsKey(ed) && linkedList.get(ed).size() > maxi) {
                maxi = linkedList.get(ed).size();
                ret = ed;
            }
        }
        return ret;
    }
}
