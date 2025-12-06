package com.github.standobyte.jojo.mechanics.itemtracking.internal;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ItemTrackerIdComponent(UUID uuid) {

	public static final Codec<ItemTrackerIdComponent> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					UUIDUtil.CODEC.fieldOf("uuid").forGetter(ItemTrackerIdComponent::uuid))
			.apply(builder, ItemTrackerIdComponent::new));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemTrackerIdComponent> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ItemTrackerIdComponent::uuid,
			ItemTrackerIdComponent::new);
}
