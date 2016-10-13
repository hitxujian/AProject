package xusheng.util.struct;

import fig.basic.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xusheng on 13/10/2016.
 */
public class FixLenRankList <T, V extends Comparable>{
    public int length;
    private List<Pair<T, V>> list;

    public FixLenRankList(int len) {
        length = len;
        list = new ArrayList<>();

    }

    public void insert(Pair<T, V> elem) {
        if (getSize() == 0) {
            list.add(elem);
            return;
        }

        if (getSize() < length)
            list.add(elem);
        else if (getLast().getSecond().compareTo(elem.getSecond()) == -1)
            list.set(getSize()-1, elem);

        adjust();
    }

    public String toString() {
        String str = "";
        for (Pair elem: list)
            str += elem.toString() + " ";
        return str;
    }

    public List<T> getList() {
        List<T> ret = new ArrayList<>();
        for (int i=0; i<list.size(); i++)
            ret.add(list.get(i).getFirst());
        return ret;
    }

    private void adjust() {
        for (int i=getSize()-1; i>0; i--) {
            if (list.get(i).getSecond().compareTo(list.get(i-1).getSecond()) == 1) {
                Pair tmp = list.get(i);
                list.set(i, list.get(i-1));
                list.set(i-1, tmp);
            } else break;
        }
    }

    private int getSize() {
        return list.size();
    }

    private Pair<T, V> getLast() {
        if (getSize() != 0)
            return list.get(list.size()-1);
        else return null;
    }


    public static void main(String args[]) {
        FixLenRankList<Integer, Double> object = new FixLenRankList<>(5);
        object.insert(new Pair(1, 4.0));
        System.out.println(object.toString());
        object.insert(new Pair(2, 3.0));
        System.out.println(object.toString());
        object.insert(new Pair(3, 5.0));
        System.out.println(object.toString());
        object.insert(new Pair(4, 7.0));
        System.out.println(object.toString());
        object.insert(new Pair(5, 1.0));
        System.out.println(object.toString());
        object.insert(new Pair(6, 2.0));
        System.out.println(object.toString());
        object.insert(new Pair(7, 9.0));
        System.out.println(object.toString());
    }
}
