package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.functions.NBTUtil;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandAwakening {
	public AwakeningStage stage = AwakeningStage.FULL_CONTROL;
	public boolean hadStandBefore = false;
	@Nullable public Set<ResourceLocation> fatedFutureStands = null;
	@Nullable public Set<ResourceLocation> characterCanonStand = null;

	// methods meant for Survival playthrough

	public void setFatedStand(LivingEntity user, ResourceLocation... fatedStands) {
		if (!user.level().isClientSide() && !hadStandBefore) {
			this.fatedFutureStands = fatedStands.length > 0 ? Arrays.stream(fatedStands).collect(Collectors.toSet()) : null;
			sync(user);
		}
	}

	public void onGivenStandInSurvival(LivingEntity user, StandInstance stand, AwakeningStage startingStage) {
		if (!user.level().isClientSide() && stand != null && !hadStandBefore) {
			this.hadStandBefore = true;
			this.stage = startingStage;
			this.fatedFutureStands = null;
			this.characterCanonStand = Util.make(new HashSet<>(), set -> set.add(stand.getStandId()));
			this.sync(user);
		}
	}

	public void setCanonStand(LivingEntity user, ResourceLocation... canonStands) {
		if (!user.level().isClientSide()) {
			this.characterCanonStand = canonStands.length > 0 ? Arrays.stream(canonStands).collect(Collectors.toSet()) : null;
			sync(user);
		}
	}

	public void setStage(LivingEntity user, AwakeningStage stage) {
		if (!user.level().isClientSide()) {
			this.stage = stage;
			sync(user);
		}
	}


	public enum AwakeningStage {
		FULL_CONTROL,
		PARTIALLY_AWAKENED,
		AWAKENING_PASSIVE
	}
	
	public void sync(LivingEntity user) {
		if (!user.level().isClientSide() && user instanceof ServerPlayer player) syncToUser(player);
	}

	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		NBTUtil.putEnum(nbt, "Stage", stage);
		nbt.putBoolean("HadStand", hadStandBefore);
		
		if (characterCanonStand != null) {
			nbt.put("CanonStand", NBTUtil.toList(characterCanonStand, ResourceLocation.CODEC));
		}
		
		if (fatedFutureStands != null) {
			nbt.put("FatedStand", NBTUtil.toList(fatedFutureStands, ResourceLocation.CODEC));
		}
		
		return nbt;
	}

	public void deserializeNBT(CompoundTag nbt) {
		this.stage = NBTUtil.getEnum(nbt, "Stage", AwakeningStage.class);
		this.hadStandBefore = nbt.getBoolean("HadStand");
		
		Set<ResourceLocation> set = new HashSet<>();
		NBTUtil.fromList(nbt, "CanonStand", set, ResourceLocation.CODEC);
		if (!set.isEmpty()) {
			this.characterCanonStand = set;
			set = new HashSet<>();
		}
		
		NBTUtil.fromList(nbt, "FatedStand", set, ResourceLocation.CODEC);
		if (!set.isEmpty()) {
			this.fatedFutureStands = set;
		}
	}

	public void syncToUser(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new StandAwakeningDataPacket(this));
	}
}
