package com.github.standobyte.jojo.util.java;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.util.RandomSource;

public class WeightsList<T> implements Weighted<T> {
	public final T emptyValue;
	public final List<Weighted<T>> wrappedList = new ArrayList<>();
	protected int weightsSum = 0;
	
	public WeightsList(T emptyValue) {
		this.emptyValue = emptyValue;
	}

	public void addValue(T value) {
		addValue(value, 1);
	}

	public void addValue(T value, int weight) {
		if (weight <= 0) return;
		addEntry(new WeightedEntry<>(value, weight));
	}
	
	public void addEntry(Weighted<T> value) {
		this.wrappedList.add(value);
		this.weightsSum += value.getWeight();
	}

	@Override
	public int getWeight() {
		return weightsSum;
	}
	
	public boolean isEmpty() {
		return weightsSum <= 0;
	}

	@Override
	public T getSound(RandomSource randomSource) {
		int i = this.getWeight();
		if (this.wrappedList.isEmpty() || i == 0) {
			return emptyValue;
		}
		if (wrappedList.size() == 1) {
			return wrappedList.get(0).getSound(randomSource);
		}
		
		int j = randomSource.nextInt(i);
		for (Weighted<T> weighted : this.wrappedList) {
			j -= weighted.getWeight();
			if (j < 0) {
				return weighted.getSound(randomSource);
			}
		}

		return emptyValue;
	}

	@Override
	public void preloadIfRequired(SoundEngine engine) {
		for (Weighted<T> weighted : this.wrappedList) {
			weighted.preloadIfRequired(engine);
		}
	}


	public static class WeightedEntry<T> implements Weighted<T> {
		protected final T value;
		protected final int weight;

		public WeightedEntry(T value, int weight) {
			this.value = value;
			this.weight = weight;
		}

		@Override
		public int getWeight() {
			return weight;
		}

		@Override
		public T getSound(RandomSource randomSource) {
			return value;
		}

		@Override
		public void preloadIfRequired(SoundEngine engine) {
			throw new UnsupportedOperationException("the hell does this have to do with weights?");
		}

	}

}
