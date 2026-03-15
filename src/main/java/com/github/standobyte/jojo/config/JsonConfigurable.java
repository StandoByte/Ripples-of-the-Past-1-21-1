package com.github.standobyte.jojo.config;

import com.google.gson.JsonElement;

public interface JsonConfigurable {
	/**
	 * Creates a JSON config template with the <b>default</b> values for the users to edit.
	 */
	JsonElement makeConfigTemplate();

	/**
	 * Edit the configurable values based on the config (read from a datapack).
	 */
	void applyConfig(JsonElement config);
	
	/**
	 * A method to restore the default configurable values of the object.
	 */
	void restoreDefaults();
}
