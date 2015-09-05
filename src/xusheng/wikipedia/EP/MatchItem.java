package xusheng.wikipedia.EP;

/**
 * Created by Administrator on 2015/6/11.
 */
public class MatchItem {

    public int start, end;
    public String name;
    public String title;

    public MatchItem(int st, int ed, String alias) {
        start = st;
        end = ed;
        name = alias;
    }

    public MatchItem(int st, int ed, String alias, String title) {
        start = st;
        end = ed;
        name = alias;
        this.title = title;
    }

    public String toString() {
        return "[" + start + ", " + end + "} : " + name;
    }
}
