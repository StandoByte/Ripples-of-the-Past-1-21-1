package com.github.standobyte.jojo.mechanics.voiceline;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.functions.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record ClientVoiceLineDefinition(
		List<ResourceLocation> sounds,
		@Nullable Component subtitle) {
	
	public static final Codec<Component> TRANSLATABLE_CODEC_COMPONENT = Codec.STRING.xmap(
			Component::translatable, 
			component -> "i ain't doing this");
	
	public static final Codec<ClientVoiceLineDefinition> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					CodecUtil.listOrSingleCodec(ResourceLocation.CODEC).fieldOf("sounds").forGetter(ClientVoiceLineDefinition::sounds),
					TRANSLATABLE_CODEC_COMPONENT.optionalFieldOf("subtitle", null).forGetter(ClientVoiceLineDefinition::subtitle))
			.apply(builder, ClientVoiceLineDefinition::new));
}
