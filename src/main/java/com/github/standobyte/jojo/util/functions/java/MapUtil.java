package com.github.standobyte.jojo.util.functions.java;

import java.util.Map;

import com.github.standobyte.jojo.util.OOPMoment;

public class MapUtil {

	public static <K, V> Map.Entry<K, V> getRandom(Map<K, V> smallMap) {
		if (smallMap == null || smallMap.isEmpty()) {
			return null;
		}

		return getByIndex(smallMap, OOPMoment.RANDOM.nextInt(smallMap.size()));
	}

	public static <K, V> Map.Entry<K, V> getByIndex(Map<K, V> smallMap, int index) {
		if (smallMap == null) {
			throw new IllegalArgumentException();
		}
		if (index < 0 || index >= smallMap.size()) {
			throw new IndexOutOfBoundsException();
		}

		var iter = smallMap.entrySet().iterator();
		for (int i = 0; i < index; i++) {
			iter.next();
		}
		return iter.next();
	}
	
}
