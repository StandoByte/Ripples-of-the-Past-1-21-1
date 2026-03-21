package com.github.standobyte.jojo.client.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.github.standobyte.jojo.util.java.FileSystemUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;

public class ClientModSettings {

	public static class Settings {
		public float standStatsTranslucency = 0.75F;
		public boolean standStatsInvertBnW = false;
//		public ChooseLifeformScreen.ViewMode viewModeGE = null;
//
//		public PositionConfig barsPosition = PositionConfig.TOP_LEFT;
//		public PositionConfig hotbarsPosition = PositionConfig.TOP_LEFT;
//		public HudTextRender hudTextRender = HudTextRender.FADE_OUT;
//		public boolean hudHotbarFold = true;
//		public boolean showLockedSlots = false;
		public boolean abilitySelectionWheel = true;

		public boolean resolveShaders = true;
//		public boolean timeStopAnimation = true;
		public boolean standMotionTilt = true;
//		public boolean poseOnLmbRmb = true;
//		public boolean autoResolveActivation = true;
//		public boolean standOutline = true;
		public boolean standAimMarker = false;
		public boolean standAura = false;
//
//		public boolean menacingParticles = true;
//		public boolean characterVoiceLines = true;
//
		public boolean toggleDisableHotbars = false;

		public boolean thirdPersonHamonAura = true;
		public boolean firstPersonHamonAura = true;
		public boolean hamonAuraBlur = false;

		public final PlayerClientBroadcastedSettings broadcasted = new PlayerClientBroadcastedSettings();
	}



	public static void edit(Consumer<Settings> edit, boolean broadcast) {
		getInstance().editSettings(edit, broadcast);
	}

	@Deprecated
	public void editSettings(Consumer<Settings> edit) {
		editSettings(edit, false);
	}

	public void editSettings(Consumer<Settings> edit, boolean broadcast) {
		edit.accept(settings);
		if (broadcast) {
			settings.broadcasted.broadcastToServer();
		}
		save();
	}

	public static Settings getSettingsReadOnly() {
		return getInstance().settings;
	}



	private static final Logger LOGGER = LogUtils.getLogger();
	public void load() {
		File path = optionsFile;
		if (!path.exists()) {
			return;
		}

		try (BufferedReader reader = Files.newReader(path, Charsets.UTF_8)) {
			Settings deserialized = gson.fromJson(reader, settings.getClass());
			this.settings = deserialized;
		}
		catch (Exception exception) {
			LOGGER.error("Failed to load mod client settings", (Throwable) exception);
		}
	}

	public void save() {
		try (BufferedWriter writer = FileSystemUtil.newWriterMkDir(optionsFile, Charsets.UTF_8)) {
			gson.toJson(settings, writer);
		}
		catch (Exception exception) {
			LOGGER.error("Failed to save mod client settings", (Throwable) exception);
		}
	}



	private static ClientModSettings instance;
	private final File optionsFile;
	private final Gson gson;
	private Settings settings = new Settings();

	public static void init(File optionsFile) {
		if (instance == null) {
			instance = new ClientModSettings(optionsFile);
		}
	}

	private ClientModSettings(File optionsFile) {
		this.optionsFile = optionsFile;
		this.gson = new GsonBuilder().setPrettyPrinting().create();
		load();
	}

	public static ClientModSettings getInstance() {
		return instance;
	}
}
