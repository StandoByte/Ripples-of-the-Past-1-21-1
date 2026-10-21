package com.github.standobyte.jojo.config.internal;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.include.com.google.common.base.Objects;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.interfaces.ObjConfig;

public class ConfigOptionsState {
	private final Map<String, Object> stateSnapshot;
	
	public static ConfigOptionsState create(ObjConfig config) {
		Map<String, ConfigOption<?>> configOptions = config.getAllConfigOptions();
		return new ConfigOptionsState(makeSnapshot(configOptions));
	}
	
	public ConfigOptionsState(Map<String, Object> stateSnapshot) {
		this.stateSnapshot = stateSnapshot;
	}
	
	public boolean hasChanged(ObjConfig config) {
		Map<String, ConfigOption<?>> configOptions = config.getAllConfigOptions();
		return hasChanged(this.stateSnapshot, configOptions);
	}
	
	public Map<String, ConfigOption<?>> filterToOnlyChanged(ObjConfig config) {
		Map<String, ConfigOption<?>> configOptions = config.getAllConfigOptions();
		return filterToOnlyChanged(this.stateSnapshot, configOptions);
	}
	
	public void apply(ObjConfig config) {
		apply(this.stateSnapshot, config.getAllConfigOptions());
	}



	public static Map<String, Object> makeSnapshot(Map<String, ConfigOption<?>> configOptions) {
		Map<String, Object> stateSnapshot = new HashMap<>();
		for (var configOptionEntry : configOptions.entrySet()) {
			stateSnapshot.put(configOptionEntry.getKey(), configOptionEntry.getValue().get());
		}
		return stateSnapshot;
	}
	
	public static boolean hasChanged(Map<String, Object> stateSnapshot, Map<String, ConfigOption<?>> configOptions) {
		for (var configOptionEntry : configOptions.entrySet()) {
			if (!Objects.equal(configOptionEntry.getValue().get(), stateSnapshot.get(configOptionEntry.getKey()))) {
				return true;
			}
		}
		return false;
	}
	
	public static Map<String, ConfigOption<?>> filterToOnlyChanged(Map<String, Object> stateSnapshot, Map<String, ConfigOption<?>> configOptions) {
		Map<String, ConfigOption<?>> changedOnly = new HashMap<>();
		for (var configOptionEntry : configOptions.entrySet()) {
			String fieldName = configOptionEntry.getKey();
			ConfigOption<?> configOption = configOptionEntry.getValue();
			if (!Objects.equal(configOption.get(), stateSnapshot.get(fieldName))) {
				changedOnly.put(fieldName, configOption);
			}
		}
		return changedOnly;
	}
	
	public static void apply(Map<String, Object> stateSnapshot, Map<String, ConfigOption<?>> configOptions) {
		for (var configOptionEntry : configOptions.entrySet()) {
			String fieldName = configOptionEntry.getKey();
			ConfigOption configOption = configOptionEntry.getValue();
			Object snapshotValue = stateSnapshot.get(fieldName);
			if (!Objects.equal(configOption.get(), snapshotValue)) {
				configOption.set(snapshotValue);
			}
		}
	}
	
}
