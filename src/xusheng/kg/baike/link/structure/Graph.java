package xusheng.kg.baike.link.structure;

import java.util.*;

/**
 * Created by Xusheng on 9/5/2016.
 * Undirected graph
 */

public class Graph {
    private Map<Integer, Set<Integer>> linkedList;
    private int source;
    private Set<Integer> ends;

    public Graph(int stIdx) {
        source = stIdx;
        ends = new HashSet<>();
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
    }

    public int pageRank() {
        if (ends.size() == 0) return -1;
        for (int i: ends) return i;
        return 0;
    }
}
