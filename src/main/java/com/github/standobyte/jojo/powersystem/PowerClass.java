package com.github.standobyte.jojo.powersystem;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public abstract class PowerClass<P extends Power<P>> {
	public static final PowerClass<StandPower> STAND = new PowerClass<>(
			"stand", 0, ModDataAttachmentTypes.STAND_POWER) {

		@Override
		public PowerType getPowerType(ResourceLocation powerTypeId) {
			return StandType.fromId(powerTypeId);
		}

	};
	public static final PowerClass<PlayerPower> PLAYER_POWER = new PowerClass<>(
			"player", 1, ModDataAttachmentTypes.PLAYER_POWER) {

		@Override
		public PowerType getPowerType(ResourceLocation powerTypeId) {
			return JojoRegistries.PLAYER_POWER_TYPES_REG.get(powerTypeId);
		}

	};
	public static final PowerClass<?>[] VALUES = { STAND, PLAYER_POWER };

	private final String name;
	private final int ordinal;
	private final Supplier<AttachmentType<P>> dataAttachment;

	private PowerClass(String name, int ordinal, Supplier<AttachmentType<P>> dataAttachment) {
		this.name = name;
		this.ordinal = ordinal;
		this.dataAttachment = dataAttachment;
	}
	
	@Nullable
	public P get(LivingEntity entity) {
		return entity != null && entity.hasData(dataAttachment) ? entity.getData(dataAttachment) : null;
	}

	public P attachGet(LivingEntity entity) {
		return entity.getData(dataAttachment);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public P cast(Power<?> power) {
		return power != null && power.getPowerClass() == this ? (P) power : null;
	}
	
	public Optional<P> getOptional(LivingEntity entity) {
		return entity != null ? entity.getExistingData(dataAttachment) : Optional.empty();
	}
	
	
	public boolean attachPower(LivingEntity entity) {
		if (!entity.hasData(dataAttachment)) {
			P dataCreated = entity.getData(dataAttachment);
			return dataCreated != null;
		}
		return false;
	}
	
	public static <P extends Power<P>> P _tryAttach(IAttachmentHolder obj, Function<LivingEntity, P> constructor) {
		if (obj instanceof LivingEntity entity) {
			return constructor.apply(entity);
		}
		throw new IllegalArgumentException();
	}

	
	public abstract PowerType getPowerType(ResourceLocation powerTypeId);
	

	public static PowerClass<?>[] values() {
		return VALUES;
	}
	
	public int ordinal() {
		return ordinal;
	}
	
	public static PowerClass<?> fromName(String name) {
		return switch (name) {
			case "stand" -> PowerClass.STAND;
			case "player" -> PowerClass.PLAYER_POWER;
			default -> null;
		};
	}
	
	
	public static final StreamCodec<ByteBuf, PowerClass<?>> NETWORK_CODEC = new StreamCodec<>() {

		@Override
		public void encode(ByteBuf buffer, PowerClass<?> value) {
			ByteBufCodecs.VAR_INT.encode(buffer, value.ordinal);
		}

		@Override
		public PowerClass<?> decode(ByteBuf buffer) {
			int id = ByteBufCodecs.VAR_INT.decode(buffer);
			return id >= 0 && id < VALUES.length ? VALUES[id] : null;
		}
		
	};
	
	
	@Override
	public String toString() {
		return name;
	}
}
