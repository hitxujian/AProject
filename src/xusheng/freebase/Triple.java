package xusheng.freebase;

/**
 * Created by Administrator on 2015/6/3.
 */
public class Triple {
    public String leftRel;
    public String rightRel;
    public String midEnt;
    public boolean is2hopConnected;

    public Triple(String left, String right, String mid) {
        leftRel = left;
        rightRel = right;
        midEnt = mid;
        is2hopConnected = true;
    }

    public Triple(String rel) {
        leftRel = rel;
        rightRel = rel;
        is2hopConnected = false;
    }
}
