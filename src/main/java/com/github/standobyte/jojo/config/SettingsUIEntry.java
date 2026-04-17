package com.github.standobyte.jojo.config;

@Deprecated
public interface SettingsUIEntry<T> {
	T get();
	void set(T value);
}
