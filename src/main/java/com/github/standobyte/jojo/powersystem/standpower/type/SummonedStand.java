package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public interface SummonedStand {
	void setUserAndPower(LivingEntity user, StandPower power);
	default void syncTo(ServerPlayer trackingPlayer, LivingEntity user) {}
	void tickStand(LivingEntity user, StandPower userStand);
	@Nullable StandEntity getStandEntity();
	default void setSelectedSkin(Optional<ResourceLocation> skin) {}
	
	public static class BlankSummonedStand implements SummonedStand {
		protected LivingEntity user;
		protected StandPower power;
		
		@Override
		public void setUserAndPower(LivingEntity user, StandPower power) {
			this.user = user;
			this.power = power;
		}

		@Override
		public void tickStand(LivingEntity user, StandPower userStand) {}
		
		@Override
		public StandEntity getStandEntity() {
			return null;
		}
		
	}
}
