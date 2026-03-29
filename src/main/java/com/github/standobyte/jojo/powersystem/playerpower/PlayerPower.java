package com.github.standobyte.jojo.powersystem.playerpower;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.network.s2c.TrPowerTypePacket;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerPower extends Power<PlayerPower> {
	protected Optional<PlayerPowerType<?>> curPowerType = Optional.empty();

	public PlayerPower(LivingEntity user) {
		super(user);
	}

	@Override
	public PlayerPowerType<?> getPowerType() {
		return curPowerType.orElse(null);
	}
	
	public void setPowerType(@Nullable PlayerPowerType<?> type) {
		PlayerPowerType<?> old = getPowerType();
		if (old != type) {
			this.curPowerType = Optional.ofNullable(type);
			if (!user.level().isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrPowerTypePacket(user.getId(), type));
			}
		}
		onSetPowerType(old, type);
	}
	
	@Override
	public boolean hasPower() {
		return curPowerType.isPresent();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PlayerPowerType<D>, D extends PlayerPowerData> Optional<D> getCurTypeData(Supplier<T> matchCurrentType) {
		return this.curPowerType
				.filter(curType -> matchCurrentType != null && matchCurrentType.get() == curType)
				.map(type -> (D) getPowerTypeData(type));
	}
	
	@Override
	public PowerClass<PlayerPower> getPowerClass() {
		return PowerClass.PLAYER_POWER;
	}
	
	/**
	 * @deprecated Placeholder. Energy will be kept in PowerData subclasses (Hamon energy in HamonData, vampire energy in VampirismData)
	 */
	@Deprecated
	public void addEnergy(float energy) {}


	@Override
	public void syncToPlayer(ServerPlayer user) {
		PacketDistributor.sendToPlayer(user, new TrPowerTypePacket(user.getId(), getPowerType()));
		super.syncToPlayer(user);
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new TrPowerTypePacket(user.getId(), getPowerType()));
		super.syncToTracking(player);
	}
	
	@Override
	public void onPlayerCloneData(PlayerPower newEntityData, boolean wasDeath) {
		super.onPlayerCloneData(newEntityData, wasDeath);
	}
	
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		curPowerType.ifPresent(curType -> {
			nbt.putString("PowerType", curType.getId().toString());
		});
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		PlayerPowerType<?> powerType = JojoRegistries.PLAYER_POWER_TYPES_REG.get(
				ResourceLocation.parse(nbt.getString("PowerType")));
		this.curPowerType = Optional.ofNullable(powerType);
	}
	
	
	@Nullable
	public static PlayerPower get(LivingEntity entity) {
		return PowerClass.PLAYER_POWER.get(entity);
	}
	
	public static Optional<PlayerPower> getOptional(LivingEntity entity) {
		return PowerClass.PLAYER_POWER.getOptional(entity);
	}

	public static <T extends PlayerPowerType<D>, D extends PlayerPowerData> Optional<D> getPowerData(LivingEntity user, Supplier<T> specificType) {
		PlayerPower playerPower = get(user);
		return playerPower != null ? playerPower.getCurTypeData(specificType) : Optional.empty();
	}

}
