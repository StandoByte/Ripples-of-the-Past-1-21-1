package com.github.standobyte.jojo.config.core;

import java.util.EnumMap;

import net.minecraft.network.chat.Component;

public class GameplayConfigOption<T> {
	public EnumMap<GameplayPreset, ConfigOption<T>> byPreset = new EnumMap<>(GameplayPreset.class);

	public GameplayConfigOption(ConfigOption<T> cfgDefault, ConfigOption<T> cfgCanon, ConfigOption<T> cfgPvp) {
		byPreset.put(GameplayPreset.DEFAULT, cfgDefault);
		byPreset.put(GameplayPreset.CANON, cfgCanon);
		byPreset.put(GameplayPreset.PVP, cfgPvp);
	}
	
	public enum GameplayPreset {
		DEFAULT,
		CANON,
		PVP;
		
		public final Component name;
		public final Component nameEdited;

		private GameplayPreset() {
			String key = "jojo_ripples.config.common_preset." + name().toLowerCase();
			this.name = Component.translatable(key);
			this.nameEdited = Component.translatable(key + ".edited");
		}
		
	}
	
}
