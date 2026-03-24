package com.github.standobyte.jojo.util.objects_java;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;

public class OptionalFloat {
	private static final OptionalFloat EMPTY = new OptionalFloat(false);

	private final boolean isPresent;
	private final float value;
	
	private OptionalFloat(float value) {
		this.isPresent = true;
		this.value = value;
	}
	
	private OptionalFloat(boolean isPresent) {
		this.isPresent = isPresent;
		this.value = 0;
	}
	
	public static OptionalFloat of(float value) {
		return new OptionalFloat(value);
	}
	
	public static OptionalFloat empty() {
		return EMPTY;
	}
	
	public boolean isPresent() {
		return isPresent;
	}
	
	public float getAsFloat() {
		if (isPresent()) {
			return value;
		}
		else {
			throw new NoSuchElementException("No value present");
		}
	}
	
	public void ifPresent(FloatConsumer consumer) {
		if (isPresent) {
			consumer.accept(value);
		}
	}
	
	public float orElseGet(FloatSupplier supplier) {
		return isPresent ? value : supplier.get();
	}

	public OptionalFloat map(FloatUnaryOperator operator) {
		Objects.requireNonNull(operator);
		if (!isPresent()) {
			return empty();
		} else {
			return OptionalFloat.of(operator.apply(value));
		}
	}

	public Optional<Float> toOptional() {
		return isPresent ? Optional.of(value) : Optional.empty();
	}
	
	
	@FunctionalInterface
	public static interface FloatSupplier {
		float get();
	}
	
	@FunctionalInterface
	public static interface FloatConsumer {
		void accept(float value);
	}
}
