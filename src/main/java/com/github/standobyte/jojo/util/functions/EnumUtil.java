package com.github.standobyte.jojo.util.functions;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class EnumUtil {

	public static <K extends Enum<K>, V> Map<K, V> makeEnumMap(Class<K> keyClass, Function<K, V> value) {
		Map<K, V> map = new EnumMap<>(keyClass);
		K[] keyEnumValues = keyClass.getEnumConstants();
		for (K enumVal : keyEnumValues) {
			map.put(enumVal, value.apply(enumVal));
		}
		return map;
	}
}