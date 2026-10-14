package com.github.standobyte.jojo.config.internal;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.core.ConfigOption;

public class ConfigOptionsMap {
	private final Map<String, ConfigOption<?>> configOptions;

	public static ConfigOptionsMap create(Map<String, ConfigOption<?>> configOptions) {
		ConfigOptionsMap configState = new ConfigOptionsMap(configOptions);

		for (var configOptionEntry : configOptions.entrySet()) {
			String fieldName = configOptionEntry.getKey();
			ConfigOption<?> configOption = configOptionEntry.getValue();
			configOption.init(fieldName);
		}
		
		return configState;
	}

	private ConfigOptionsMap(Map<String, ConfigOption<?>> configOptions) {
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
