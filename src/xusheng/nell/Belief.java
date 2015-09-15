package xusheng.nell;

/**
 * Created by Administrator on 2015/9/6.
 */
public class Belief {

    public String entity, relation, value;
    public boolean isType = false;

    public Belief(String line) {
        String[] spt = line.split("\t");
        entity = spt[0];
        relation = spt[1];
        value = spt[2];
        if (relation.equals("generalizations")) isType = true;
    }

    public String toString() {
        return entity + "\t" + relation + "\t" + value;
    }
}
