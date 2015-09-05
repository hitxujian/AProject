package xusheng.reverb;

/**
 * Created by angrymidiao on 4/26/15.
 */
public class RvTuple {

    public String arg1, arg2, idx;

    // for 3m reverb supports
    public RvTuple(String line, String num) {
        String[] spt = line.split("\t");
        arg1 = spt[0].trim();
        arg2 = spt[2].trim();
        idx = num;
    }

    // for simple linking file, args are entity idxs
    public RvTuple(String line) {
        String[] spt = line.split("\t");
        arg1 = spt[1];
        arg2 = spt[2];
        idx = spt[0];
    }

    //for linking result file
    public RvTuple(String line, int flag) {
        String[] spt = line.split("\t");
        String[] sptt = spt[1].split(" ");
        arg1 = sptt[0];
        arg2 = sptt[1];
        idx = spt[0];
    }
}
