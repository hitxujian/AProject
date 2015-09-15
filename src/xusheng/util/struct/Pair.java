package xusheng.util.struct;

/**
 * Created by Administrator on 2015/9/15.
 */
public class Pair<T, V> {

    private T first;
    private V second;

    public Pair(T arg1, V arg2) {
        first = arg1;
        second = arg2;
    }

    public T getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }
}
