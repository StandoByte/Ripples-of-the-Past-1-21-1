package com.github.standobyte.jojo.util.objects_java;

import java.util.function.Supplier;

public class ObjectWrapper<T> implements Supplier<T> {
	private T object;

	public ObjectWrapper(T object) {
		this.object = object;
	}

	public void set(T object) {
		this.object = object;
	}

	@Override
	public T get() {
		return object;
	}

}
