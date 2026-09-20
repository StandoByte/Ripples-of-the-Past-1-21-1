package com.github.standobyte.jojo.config.internal;

import java.util.Map;
import java.util.TreeMap;

import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.core.ModConfig;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;

public class ConfigsMap {
	private final Map<ConfigId, ModConfig<?, ?, ?>> allConfigs;
	private final Map<String, AllModConfigs> byModIdForClientUI;
	
	public ConfigsMap(Dist curEnvironment) {
		this.allConfigs = new TreeMap<>(ConfigId.COMPARATOR);
		this.byModIdForClientUI = curEnvironment == Dist.CLIENT ? new TreeMap<>() : null;
	}
	
	public void add(ModConfig<?, ?, ?> config) {
		ConfigId configId = config.configId();
		allConfigs.put(configId, config);
		if (byModIdForClientUI != null) {
			switch (configId.configClass()) {
				case MOD -> {
					String modId = configId.modOrStandId();
					byModIdForClientUI.computeIfAbsent(modId, __ -> new AllModConfigs()).modConfig = config;
				}
				case STAND -> {
					String standIdString = configId.modOrStandId();
					ResourceLocation standId = ResourceLocation.parse(standIdString);
					String modId = standId.getNamespace();
					byModIdForClientUI.computeIfAbsent(modId, __ -> new AllModConfigs()).standConfigs.put(standId, config);
				}
			}
		}
	}
	

	public ModConfig<?, ?, ?> get(ConfigId configId) {
		return allConfigs.get(configId);
	}
	
	public ModConfig<?, ?, ?> getStandConfig(ResourceLocation standId) {
		AllModConfigs modConfigs = byModIdForClientUI.get(standId.getNamespace());
		return modConfigs != null ? modConfigs.standConfigs.get(standId) : null;
	}
	
	public Iterable<ModConfig<?, ?, ?>> allConfigs() {
		return allConfigs.values();
	}
	
	
	public Iterable<Map.Entry<String, AllModConfigs>> allConfigurableMods() {
		return byModIdForClientUI.entrySet();
	}
	
	public AllModConfigs getAllConfigsFromMod(String modId) {
		return byModIdForClientUI.get(modId);
	}
	
	
	public static class AllModConfigs {
		public ModConfig<?, ?, ?> modConfig;
		public final Map<ResourceLocation, ModConfig<?, ?, ?>> standConfigs = new TreeMap<>();
	}
	
}
