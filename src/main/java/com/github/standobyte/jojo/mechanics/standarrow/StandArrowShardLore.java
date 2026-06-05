package com.github.standobyte.jojo.mechanics.standarrow;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StandArrowShardLore(
		Optional<Component> arrowItemName, 
		Optional<Component> userCharacterName) {
	public static final Codec<StandArrowShardLore> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					ComponentSerialization.CODEC.optionalFieldOf("arrow_item").forGetter(StandArrowShardLore::arrowItemName),
					ComponentSerialization.CODEC.optionalFieldOf("user").forGetter(StandArrowShardLore::userCharacterName))
			.apply(builder, StandArrowShardLore::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, StandArrowShardLore> STREAM_CODEC = StreamCodec.composite(
			ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs::optional), StandArrowShardLore::arrowItemName,
			ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs::optional), StandArrowShardLore::userCharacterName,
			StandArrowShardLore::new);
	
	
	
	
	public static StandArrowShardLore empty() {
		return new StandArrowShardLore(Optional.empty(), Optional.empty());
	}
	
	public StandArrowShardLore withArrowItemName(Optional<Component> arrowItemName) {
		return new StandArrowShardLore(arrowItemName, this.userCharacterName);
	}
	
	public StandArrowShardLore withUserCharacterName(Optional<Component> userCharacterName) {
		return new StandArrowShardLore(this.arrowItemName, userCharacterName);
	}
	
}
