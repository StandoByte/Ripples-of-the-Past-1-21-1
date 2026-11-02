package com.github.standobyte.jojo.config.internal;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.core.JojoMod;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class ObjectConfigHelper<C> {
	public final C configObj;
	public static final String DIRECTORY = "config/jojo_rotp/";
	public final ConfigOptionsMap configOptions;
	
	public ObjectConfigHelper(C configObj, ConfigOptionsMap configOptions) {
		this.configObj = configObj;
		this.configOptions = configOptions;
	}
	
	public void toBuf(RegistryFriendlyByteBuf buf) {
		for (ConfigOption<?> option : configOptions.values()) {
			option.toBuf(buf);
		}
	}
	
	public void fromBuf(RegistryFriendlyByteBuf buf, @Nullable Player relevantPlayer) {
		for (ConfigOption<?> option : configOptions.values()) {
			option.fromBufWithCallback(buf, relevantPlayer);
		}
	}
	
	public void editToBuf(String editedOptionName, RegistryFriendlyByteBuf buf) {
		buf.writeUtf(editedOptionName);
		configOptions.get(editedOptionName).toBuf(buf);
	}
	
	public void editFromBuf(RegistryFriendlyByteBuf buf, @Nullable Player relevantPlayer) {
		String editedOptionName = buf.readUtf();
		configOptions.get(editedOptionName).fromBufWithCallback(buf, relevantPlayer);
	}
	
	public JsonObject toJson() {
		return settingsToJson(configOptions.configOptions());
	}
	
	public static JsonObject settingsToJson(Map<String, ConfigOption<?>> settings) {
		return settingsToJson(settings, Function.identity());
	}
	
	public static <K> JsonObject settingsToJson(Map<K, ConfigOption<?>> settings, Function<K, String> getFieldName) {
		JsonObject json = new JsonObject();
		for (var optionEntry : settings.entrySet()) {
			String fieldName = getFieldName.apply(optionEntry.getKey());
			ConfigOption<?> field = optionEntry.getValue();
			json.add(fieldName, field.toJson(fieldName));
		}
		return json;
	}
	
	public void fromJson(JsonObject json) {
		for (var optionEntry : configOptions.configOptions().entrySet()) {
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
	
	public void reset(@Nullable Player relevantPlayer) {
		for (ConfigOption<?> option : configOptions.values()) {
			option.resetWithCallback(relevantPlayer);
		}
	}
	
	

	static Map<Class<?>, Field[]> FIELDS_CACHE = new IdentityHashMap<>();
	
	@Nullable
	public static <C> ObjectConfigHelper<C> create(@Nullable C configObj) {
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
				configOptions.put(fieldName, option);
			}
			ConfigOptionsMap configOptionsMap = ConfigOptionsMap.create(configOptions);
			return new ObjectConfigHelper<>(configObj, configOptionsMap);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			JojoMod.getLogger().error("Error creating config object: {}", e);
			throw new RuntimeException(e);
		}
	}
	
}
