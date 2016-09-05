package xusheng.kg.baike.link.structure;

/**
 * Created by Xusheng on 9/5/2016.
 */

public class Node {
    public int idx;
    public Node succ, prev;

    public Node(int idx) {
        this.idx = idx;
        this.succ = null;
        this.prev = null;
    }

    public Node(int idx, Node succ, Node prev) {
        this.idx = idx;
        this.succ = succ;
        this.prev = prev;
    }
}
