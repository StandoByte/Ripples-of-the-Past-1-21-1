package com.github.standobyte.jojo.config.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.ModConfigInterface;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class ModRotpConfigEvent extends Event implements IModBusEvent {
	public final Dist environment;
	public final Map<String, ModConfig<?, ?, ?>> configs = new HashMap<>();
	
	public ModRotpConfigEvent(Dist environment) {
		this.environment = environment;
	}

	public <C1, C2, C3> ModConfigInterface<C1, C2, C3> registerConfig(String modId, 
			@Nullable Supplier<C1> client, 
			@Nullable Supplier<C2> clientBroadcast, 
			@Nullable Supplier<C3> common) {
		ModConfig<C1, C2, C3> config = new ModConfig<>(environment, client, clientBroadcast, common, modId);
		configs.put(modId, config);
		return config;
	}
	
	public static class ConfigsPrep {
		public @Nullable Supplier<?> client;
		public @Nullable Supplier<?> clientBroadcast; 
		public @Nullable Supplier<?> common;
	}
}
