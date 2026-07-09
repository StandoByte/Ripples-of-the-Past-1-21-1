package com.github.standobyte.jojo.customobjects;

import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public interface EntityWithStandSkin {
	@ApiStatus.Internal @Nullable StandSkinPath getStandSkinId();
	@ApiStatus.Internal void setStandSkinId(StandSkinPath skin);
	
	default void setStandSkinIdFrom(StandPower userPower) {
		setStandSkinId(StandSkinPath.from(userPower));
	}
	
	default ResourceLocation getStandType() {
		StandSkinPath skin = getStandSkinId();
		return skin != null ? skin.standType() : null;
	}
	
	default Optional<ResourceLocation> getStandSkin() {
		StandSkinPath skin = getStandSkinId();
		return skin != null ? skin.standSkin() : null;
	}

	public static record StandSkinPath(ResourceLocation standType, Optional<ResourceLocation> standSkin) {
		
		public static StandSkinPath from(StandPower standPower) {
			StandInstance stand = standPower != null ? standPower.getStandInstance().orElse(null) : null;
			return from(stand);
		}
		
		public static StandSkinPath from(StandInstance standInstance) {
			return standInstance != null ? new StandSkinPath(standInstance.getStandId(), standInstance.getSelectedSkin()) : null;
		}
		
		public static final StreamCodec<ByteBuf, StandSkinPath> STREAM_CODEC = StreamCodec.composite(
				ResourceLocation.STREAM_CODEC, StandSkinPath::standType,
				ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), StandSkinPath::standSkin,
				StandSkinPath::new);

		public static final Codec<StandSkinPath> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						ResourceLocation.CODEC.fieldOf("stand").forGetter(StandSkinPath::standType),
						ResourceLocation.CODEC.optionalFieldOf("skin").forGetter(StandSkinPath::standSkin))
				.apply(builder, StandSkinPath::new));
	}
}
