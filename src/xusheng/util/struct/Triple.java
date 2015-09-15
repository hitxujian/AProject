package xusheng.util.struct;

/**
 * Created by Administrator on 2015/9/15.
 */
public class Triple<T, S, V> {

    private T first;
    private S second;
    private V third;

    public Triple(T arg1, S arg2, V arg3) {
        first = arg1;
        second = arg2;
        third = arg3;
    }

    public T getFirst(){
        return first;
    }

    public S getSecond() {
        return second;
    }

    public V getThird() {
        return third;
    }
}
