package com.github.standobyte.jojo.config.interfaces;

import java.util.Map;

import com.github.standobyte.jojo.config.core.ConfigOption;

public interface ObjConfig {
	boolean isDefault();
	void reset();
	
	Map<String, ConfigOption<?>> getAllConfigOptions();
}
