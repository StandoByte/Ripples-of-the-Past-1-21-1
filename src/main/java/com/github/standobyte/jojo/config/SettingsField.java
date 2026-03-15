package com.github.standobyte.jojo.config;

public interface SettingsField<T> {
	T get();
	void set(T value);
}
