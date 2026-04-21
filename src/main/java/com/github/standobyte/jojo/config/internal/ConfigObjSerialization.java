package com.github.standobyte.jojo.config.internal;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.core.JojoMod;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.RegistryFriendlyByteBuf;

public class ConfigObjSerialization<C> {
	public final C configObj;
	public final Map<String, ConfigOption<?>> configOptions;
	
	protected ConfigObjSerialization(C configObj, Map<String, ConfigOption<?>> configOptions) {
		this.configObj = configObj;
		this.configOptions = configOptions;
	}
	
	public void toBuf(RegistryFriendlyByteBuf buf) {
		for (ConfigOption<?> option : configOptions.values()) {
			option.toBuf(buf);
		}
	}
	
	public void fromBuf(RegistryFriendlyByteBuf buf) {
		for (ConfigOption<?> option : configOptions.values()) {
			option.fromBuf(buf);
		}
	}
	
	public void editToBuf(String editedOptionName, RegistryFriendlyByteBuf buf) {
		buf.writeUtf(editedOptionName);
		configOptions.get(editedOptionName).toBuf(buf);
	}
	
	public void editFromBuf(RegistryFriendlyByteBuf buf) {
		String editedOptionName = buf.readUtf();
		configOptions.get(editedOptionName).fromBuf(buf);
	}
	
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		for (var optionEntry : configOptions.entrySet()) {
			String fieldName = optionEntry.getKey();
			ConfigOption<?> field = optionEntry.getValue();
			json.add(fieldName, field.toJson(fieldName));
		}
		return json;
	}
	
	public void fromJson(JsonObject json) {
		for (var optionEntry : configOptions.entrySet()) {
			String fieldName = optionEntry.getKey();
			ConfigOption<?> field = optionEntry.getValue();
			JsonElement fieldJson = json.get(fieldName);
			if (fieldJson != null) {
				field.fromJson(fieldJson, fieldName);
			}
			else {
				field.reset();
			}
		}
	}
	
	public boolean isDefault() {
		for (ConfigOption<?> option : configOptions.values()) {
			if (!option.isDefault()) return false;
		}
		return true;
	}
	
	public void reset() {
		for (ConfigOption<?> option : configOptions.values()) {
			option.reset();
		}
	}
	
	

	static Map<Class<?>, Field[]> FIELDS_CACHE = new IdentityHashMap<>();
	
	@Nullable
	public static <C> ConfigObjSerialization<C> create(@Nullable C configObj) {
		if (configObj == null) return null;
		
		Class<?> cfgClass = configObj.getClass();
		Field[] fields = FIELDS_CACHE.get(cfgClass);
		if (fields == null) {
			// sorts the fields by their names' alphabetical order
			Map<String, Field> fieldsSorted = new TreeMap<>();
			for (Field field : cfgClass.getDeclaredFields()) {
				if (ConfigOption.class.isAssignableFrom(field.getType())) {
					fieldsSorted.put(field.getName(), field);
				}
			}
			
			fields = new Field[fieldsSorted.size()];
			int i = 0;
			for (Field field : fieldsSorted.values()) {
				fields[i++] = field;
			}
			FIELDS_CACHE.put(cfgClass, fields);
		}
		
		// preserves the previously defined alphabetical order of the config options
		// these map choices should guarantee that the order will be the same on client and server
		Map<String, ConfigOption<?>> configOptions = new LinkedHashMap<>();
		try {
			for (Field field : fields) {
				ConfigOption<?> option = (ConfigOption<?>) field.get(configObj);
				String fieldName = field.getName();
				option.init(fieldName);
				configOptions.put(fieldName, option);
			}
			return new ConfigObjSerialization<>(configObj, configOptions);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			JojoMod.getLogger().error("Error creating config object: {}", e);
			throw new RuntimeException(e);
		}
	}
	
}
