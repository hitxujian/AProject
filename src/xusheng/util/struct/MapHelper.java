package xusheng.util.struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class MapHelper<T, V extends Comparable<V>> {

	// descending order
	public static <T, V extends Comparable<V>> ArrayList<Entry<T, V>> sort(HashMap<T, V> mp) {
		ArrayList<Entry<T, V>> ret = new ArrayList<>(mp.entrySet());
		Collections.sort(ret, new Comparator<Entry<T, V>>() {
			public int compare(Entry<T, V> o1, Entry<T, V> o2) {
				return -(o1.getValue().compareTo(o2.getValue()));
			}
		});
		return ret;
	}

	// ascending order
	public static <T, V extends Comparable<V>> ArrayList<Entry<T, V>> sort(HashMap<T, V> mp, boolean asc) {
		ArrayList<Entry<T, V>> ret = new ArrayList<>(mp.entrySet());
		Collections.sort(ret, new Comparator<Entry<T, V>>() {
			public int compare(Entry<T, V> o1, Entry<T, V> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return ret;
	}
}