package com.github.standobyte.jojo.config.interfaces.fileimpl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.interfaces.CommonConfig;
import com.github.standobyte.jojo.config.internal.ObjectConfigHelper;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.FileSystemUtil;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Strictness;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.RegistryFriendlyByteBuf;

public class CommonFileConfig<C3> implements CommonConfig<C3> {
	@Nullable private final ObjectConfigHelper<C3> configState;
	private File file;

	public CommonFileConfig(ObjectConfigHelper<C3> config, File mainDirectory, String modId) {
		this.configState = config;
		String filePrefix = makeFilePrefix(modId);
		this.file = new File(mainDirectory, ObjectConfigHelper.DIRECTORY + filePrefix + "settings.json");
	}

	@Override
	public C3 getObj() {
		return configState.configObj;
	}
	
	@Override
	public boolean exists() {
		return configState != null;
	}
	
	@Override
	public boolean isDefault() {
		return configState.isDefault();
	}

	@Override
	public void reset() {
		configState.reset();
	}
	
	@Override
	public void setRemoteState() {
		configState.configOptions.values().forEach(ConfigOption::setRemoteState);
	}
	
	@Override
	public void unsetRemoteState() {
		configState.configOptions.values().forEach(ConfigOption::unsetRemoteState);
	}

	@Override
	public void loadFromFileSystem() {
		if (exists()) {
			JsonObject json = CommonFileConfig.deserializeObj(file);
			if (json != null) {
				configState.fromJson(json);
			}
		}
	}

	@Override
	public void saveToFileSystem() {
		if (exists()) {
			CommonFileConfig.serialize(file, writer -> {
				JsonObject json = configState.toJson();
				CommonFileConfig.GSON.toJson(json, writer);
			});
		}
	}

	@Override
	public String getConfigOptionName(ConfigOption<?> configOption) {
		return configOption.getFieldName();
	}

	@Override
	public ConfigOption<?> getConfigOptionByName(String fieldName) {
		return configState.configOptions.get(fieldName);
	}
	
	@Override
	public void toBuf(RegistryFriendlyByteBuf buf) {
		configState.toBuf(buf);
	}
	
	@Override
	public void fromBuf(RegistryFriendlyByteBuf buf) {
		configState.fromBuf(buf);
	}
	
	
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	@Nullable
	public static JsonElement deserialize(File file) {
		if (!file.exists()) {
			return null;
		}

		try (BufferedReader reader = Files.newReader(file, Charsets.UTF_8)) {
			return JSONUtil.fromJson(JSONUtil.GSON, reader, JsonElement.class, Strictness.LENIENT);
		}
		catch (Exception exception) {
			JojoMod.getLogger().error("Failed to load mod client settings", (Throwable) exception);
			return null;
		}
	}
	
	@Nullable
	public static JsonObject deserializeObj(File file) {
		JsonElement json = deserialize(file);
		return json != null ? json.getAsJsonObject() : null;
	}
	
	public static boolean serialize(File file, Consumer<BufferedWriter> serialize) {
		try (BufferedWriter writer = FileSystemUtil.newWriterMkDir(file, Charsets.UTF_8)) {
			serialize.accept(writer);
			return true;
		}
		catch (Exception exception) {
			JojoMod.getLogger().error("Failed to save mod client settings", (Throwable) exception);
			return false;
		}
	}
	
	
	public static String makeFilePrefix(String modId) {
		return modId.equals(JojoMod.MOD_ID) ? "" : (modId + "_");
	}
	
}
