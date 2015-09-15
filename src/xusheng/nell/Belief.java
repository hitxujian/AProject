package xusheng.nell;

/**
 * Created by Administrator on 2015/9/6.
 */
public class Belief {

    public String entity, relation, value;
    public boolean isType = false;
    public boolean isConcept = true;

    public Belief(String line) {
        String[] spt = line.split("\t");
        if (!spt[0].startsWith("concept") || !spt[0].startsWith("concept")) {
            isConcept = false;
            return;
        }
        entity = getName(spt[0]);
        relation = getName(spt[1]);
        value = getName(spt[2]);
        if (relation.equals("generalizations")) isType = true;
    }

    public String getName(String arg) {
        String[] spt = arg.split(":");
        return spt[spt.length-1];
    }

    public String toString() {
        return entity + "\t" + value;
    }
}
