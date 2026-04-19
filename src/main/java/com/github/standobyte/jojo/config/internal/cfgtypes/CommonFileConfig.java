package com.github.standobyte.jojo.config.internal.cfgtypes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.function.Consumer;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.FileSystemUtil;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class CommonFileConfig<C3> extends CommonConfig<C3> {
	protected File file;

	public CommonFileConfig(C3 config, File mainDirectory, String modId) {
		super(config);
		
		String filePrefix = modId.equals(JojoMod.MOD_ID) ? "" : (modId + "_");
		this.file = new File(mainDirectory, "config/jojo_rotp/" + filePrefix + "settings.json");
	}
	
	public void loadFromFileSystem() {
		if (exists()) {
			CommonFileConfig.deserialize(file, reader -> {
				JsonObject json = JSONUtil.parse(reader);
				configState.fromJson(json);
			});
		}
	}
	
	public void saveToFileSystem() {
		if (exists()) {
			CommonFileConfig.serialize(file, writer -> {
				JsonObject json = configState.toJson();
				CommonFileConfig.GSON.toJson(json, writer);
			});
		}
	}
	
	
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static boolean deserialize(File file, Consumer<BufferedReader> deserialize) {
		if (!file.exists()) {
			return false;
		}

		try (BufferedReader reader = Files.newReader(file, Charsets.UTF_8)) {
			deserialize.accept(reader);
			return true;
		}
		catch (Exception exception) {
			JojoMod.getLogger().error("Failed to load mod client settings", (Throwable) exception);
			return false;
		}
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
	
}
