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
	
	
	public static <T extends Enum<T>> T getNextWrapped(T constant) {
		return getNextWrapped(constant, (T[]) constant.getClass().getEnumConstants());
	}
	
	public static <T extends Enum<T>> T getNextWrapped(T constant, Class<T> enumClass) {
		return getNextWrapped(constant, enumClass.getEnumConstants());
	}
	
	public static <T extends Enum<T>> T getNextWrapped(T constant, T[] values) {
		return values[(constant.ordinal() + 1) % values.length];
	}
	
	
	public static <T extends Enum<T>> T getPrevWrapped(T constant) {
		return getPrevWrapped(constant, (T[]) constant.getClass().getEnumConstants());
	}
	
	public static <T extends Enum<T>> T getPrevWrapped(T constant, Class<T> enumClass) {
		return getPrevWrapped(constant, enumClass.getEnumConstants());
	}
	
	public static <T extends Enum<T>> T getPrevWrapped(T constant, T[] values) {
		return values[(constant.ordinal() - 1 + values.length) % values.length];
	}
	
}