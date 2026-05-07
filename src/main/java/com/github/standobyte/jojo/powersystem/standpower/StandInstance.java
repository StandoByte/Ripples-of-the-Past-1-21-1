package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.text.StandSkinComponent;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class StandInstance {
	private final Either<StandType, ResourceLocation> standType;
	private Optional<ResourceLocation> skin = Optional.empty();
	protected String nameTlKey;
	
	@Nullable
	public static StandInstance fromExistingStandId(ResourceLocation standId) {
		StandType standType = StandType.fromId(standId);
		return standType != null ? new StandInstance(standType) : null;
	}
	
	protected static StandInstance fromStandId(ResourceLocation standId) {
		StandType stand = StandType.fromId(standId);
		return new StandInstance(stand != null ? Either.left(stand) : Either.right(standId));
	}
	
	public StandInstance(@Nonnull StandType standType) {
		this(Either.left(standType));
	}
	
	protected StandInstance(Either<StandType, ResourceLocation> standType) {
		this.standType = standType;
		this.nameTlKey = StandType.makeTlKey(getStandId());
	}

	@Nullable
	public StandType getStandType() {
		return standType.left().filter(StandType::isEnabled).orElse(null);
	}
	
	public boolean standExists() {
		return standType.left().filter(StandType::isEnabled).isPresent();
	}
	
	public ResourceLocation getStandId() {
		return standType.map(StandType::getId, Function.identity());
	}
	
	
	@ApiStatus.Internal
	public void setCustomSkin(Optional<ResourceLocation> skin) {
		Objects.requireNonNull(skin);
		this.skin = skin;
	}
	
	public Optional<ResourceLocation> getSelectedSkin() {
		return skin;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(getStandId(), skin);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof StandInstance other
				&& this.getStandId().equals(other.getStandId())
				&& this.skin.equals(other.skin);
	}
	
	public StandInstance copy() {
		StandInstance stand = new StandInstance(this.standType);
		stand.setCustomSkin(this.skin);
		return stand;
	}
	
	
	@Nullable
	public Component getStandName() {
		MutableComponent name = StandSkinComponent.translatable(this, nameTlKey);
		StandType stand = getStandType();
		if (stand == null) {
			return name.withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH);
		}
		return name;
	}
	
	
	public static final Codec<StandInstance> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					ResourceLocation.CODEC.fieldOf("stand_type").forGetter(StandInstance::getStandId),
					ResourceLocation.CODEC.optionalFieldOf("skin").forGetter(StandInstance::getSelectedSkin))
			.apply(builder, 
					(ResourceLocation standId, Optional<ResourceLocation> standSkin) -> {
						StandInstance stand = StandInstance.fromStandId(standId);
						stand.setCustomSkin(standSkin);
						return stand;
					}));
	
	
	/**
	 * Because of datapack Stands, if the Stand type query were to happen right at the moment of packet decoding, 
	 * it would be too early, when the datapack packet is not handled yet,
	 * so we need an intermediate class that lazily creates the Stand instance
	 */
	public static class NetworkData {
		private final ResourceLocation standTypeId;
		private final Optional<ResourceLocation> skin;
		private StandInstance standInstance;
		
		public static NetworkData wrap(@Nonnull StandInstance standInstance) {
			NetworkData data = new NetworkData(standInstance.getStandId(), standInstance.skin);
			data.standInstance = standInstance;
			return data;
		}
		
		public NetworkData(ResourceLocation standTypeId, Optional<ResourceLocation> skin) {
			this.standTypeId = standTypeId;
			this.skin = skin;
		}
		
		public static void encode(FriendlyByteBuf buffer, StandInstance instance) {
			NETWORK_CODEC.encode(buffer, wrap(instance));
		}
		
		public static final StreamCodec<FriendlyByteBuf, StandInstance.NetworkData> NETWORK_CODEC = StreamCodec.composite(
				ResourceLocation.STREAM_CODEC, instance -> instance.standTypeId,
				ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), instance -> instance.skin,
				StandInstance.NetworkData::new);
		
		public StandInstance get() {
			if (this.standInstance == null) {
				this.standInstance = StandInstance.fromStandId(standTypeId);
				this.standInstance.skin = this.skin;
			}
			return this.standInstance;
		}
	}

}
