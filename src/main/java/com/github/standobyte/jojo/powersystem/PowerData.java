package com.github.standobyte.jojo.powersystem;

import com.github.standobyte.jojo.core.packet.fromserver.TrPowerDataPacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class PowerData implements INBTSerializable<CompoundTag> {
	public abstract PowerClass<?> getPowerClass();
	public abstract void toBuf(FriendlyByteBuf buf, boolean isSentToTracking);
	public abstract void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking);
	
	public final void syncToPlayer(ServerPlayer user) {
		PacketDistributor.sendToPlayer(user, new TrPowerDataPacket(user.getId(), getPowerClass(), this, false));
	}
	
	public final void syncToTracking(LivingEntity user, ServerPlayer tracking) {
		PacketDistributor.sendToPlayer(tracking, new TrPowerDataPacket(user.getId(), getPowerClass(), this, true));
	}
	
	public final void syncToAllTracking(LivingEntity user) {
		PacketDistributor.sendToPlayersTrackingEntity(user, new TrPowerDataPacket(user.getId(), getPowerClass(), this, true));
	}
	
	
	public void syncOnUpdate(LivingEntity user) {
		if (!user.level().isClientSide()) {
			syncToAllTracking(user);
			if (user instanceof ServerPlayer player) {
				syncToPlayer(player);
			}
		}
	}
	
	
	public void onInit(PowerType powerType, Power<?> userPower) {}
	
	public boolean unlockSkill(Power<?> userPower, String skillName) {
		return false;
	}
	
}
