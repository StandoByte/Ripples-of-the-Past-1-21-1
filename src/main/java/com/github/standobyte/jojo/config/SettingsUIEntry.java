package com.github.standobyte.jojo.config;

public interface SettingsUIEntry<T> {
	T get();
	void set(T value);
}
