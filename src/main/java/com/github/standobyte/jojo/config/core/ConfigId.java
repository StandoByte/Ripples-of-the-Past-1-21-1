package com.github.standobyte.jojo.config.core;

import java.util.Comparator;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record ConfigId(ConfigClass configClass, String modOrStandId) {
	
	public enum ConfigClass {
		MOD,
		STAND
	}
	
	public Component getConfigTitle() {
		return switch (configClass) {
			case MOD -> Component.translatable(modOrStandId + ".config.title");
			case STAND -> Component.literal(modOrStandId.toString());
		};
	}
	
	public static final StreamCodec<FriendlyByteBuf, ConfigId> STREAM_CODEC = StreamCodec.composite(
			NeoForgeStreamCodecs.enumCodec(ConfigClass.class), ConfigId::configClass, 
			ByteBufCodecs.STRING_UTF8, ConfigId::modOrStandId, 
			ConfigId::new);
	
	public static final Comparator<ConfigId> COMPARATOR = Comparator
			.comparing(ConfigId::modOrStandId)
			.thenComparingInt(id -> id.configClass().ordinal());
	
}
