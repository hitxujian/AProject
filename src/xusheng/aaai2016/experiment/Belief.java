package xusheng.aaai2016.experiment;

/**
 * Created by Administrator on 2015/9/6.
 */
public class Belief {

    public String entity, relation, value;
    public boolean isType;

    public Belief(String line) {
        String[] spt = line.split("\t");
        entity = spt[0];
        relation = spt[2];
        value = spt[3];
        if (relation.equals("generalization")) isType = true;
        else isType = false;
    }

    public String toString() {
        return entity + "\t" + relation + "\t" + value;
    }
}
