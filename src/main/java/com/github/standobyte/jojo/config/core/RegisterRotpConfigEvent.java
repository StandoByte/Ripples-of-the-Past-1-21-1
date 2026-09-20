package com.github.standobyte.jojo.config.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.ModConfigInterface;
import com.github.standobyte.jojo.config.stand.StandCommonConfig;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.minecraft.core.Holder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class RegisterRotpConfigEvent extends Event implements IModBusEvent {
	public final Dist environment;
	public final Map<ConfigId, ModConfig<?, ?, ?>> configs = new HashMap<>();
	
	public RegisterRotpConfigEvent(Dist environment) {
		this.environment = environment;
	}

	public <C1, C2, C3> ModConfigInterface<C1, C2, C3> registerConfig(String modId, 
			@Nullable Supplier<C1> client, 
			@Nullable Supplier<C2> clientBroadcast, 
			@Nullable Supplier<C3> common) {
		ModConfig<C1, C2, C3> config = ModConfig.modFileConfig(environment, client, clientBroadcast, common, modId);
		configs.put(config.configId(), config);
		return config;
	}
	
	public void generateStandConfig(Holder<StandType> standType) {
		generateStandConfig(standType.value());
	}
	
	public void generateStandConfig(StandType standType) {
		ConfigId configId = new ConfigId(ConfigId.ConfigClass.STAND, standType.getId().toString());
		StandCommonConfig standCommonConfig = StandCommonConfig.makeConfig(environment, standType);
		ModConfig<?, ?, ?> standConfig = new ModConfig<>(environment, configId,
				null,
				standCommonConfig,
				null);
		configs.put(standConfig.configId(), standConfig);
	}
	
}
