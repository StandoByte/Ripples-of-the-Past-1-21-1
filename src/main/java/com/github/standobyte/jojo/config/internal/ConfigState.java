package com.github.standobyte.jojo.config.internal;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.core.ConfigOption;

public class ConfigState {
	private final Map<String, ConfigOption<?>> configOptions;

	public static ConfigState create(Map<String, ConfigOption<?>> configOptions) {
		ConfigState configState = new ConfigState(configOptions);

		for (var configOptionEntry : configOptions.entrySet()) {
			String fieldName = configOptionEntry.getKey();
			ConfigOption<?> configOption = configOptionEntry.getValue();
			configOption.init(fieldName);
		}
		
		return configState;
	}

	private ConfigState(Map<String, ConfigOption<?>> configOptions) {
		this.configOptions = configOptions;
	}
	
	@Nullable
	public ConfigOption<?> get(String fieldName) {
		return configOptions.get(fieldName);
	}
	
	public Iterable<ConfigOption<?>> values() {
		return configOptions.values();
	}
	
	public Map<String, ConfigOption<?>> configOptions() {
		return configOptions;
	}
	
}
