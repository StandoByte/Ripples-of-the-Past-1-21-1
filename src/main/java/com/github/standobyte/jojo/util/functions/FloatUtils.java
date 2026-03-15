package com.github.standobyte.jojo.util.functions;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class FloatUtils {

	@SuppressWarnings("unchecked")
	public static<T> Comparator<T> comparingDouble(ToFloatFunction<? super T> keyExtractor) {
		Objects.requireNonNull(keyExtractor);
		return (Comparator<T> & Serializable)
				(c1, c2) -> Double.compare(keyExtractor.applyAsFloat(c1), keyExtractor.applyAsFloat(c2));
	}

	@FunctionalInterface
	public static interface ToFloatFunction<T> {
		double applyAsFloat(T value);
	}
}
