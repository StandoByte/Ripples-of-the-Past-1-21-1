package com.github.standobyte.jojo;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.entityattachment.SynchronizablePlayerData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class JojoModLivingVariables implements INBTSerializable<CompoundTag>, TickingEntityData, SynchronizablePlayerData {
	protected final LivingEntity entity;
	
	public boolean isDyingBody = false;
	public Vec3 bleedingParticlesPos = null;
	
	public boolean foundAnArrow;
	public int findMoreArrowsTimer = -1;
	
	public JojoModLivingVariables(LivingEntity entity) {
		this.entity = entity;
	}
	
	public void tick() {
		bleedingParticlesPos = null;
		if (findMoreArrowsTimer >= 0) findMoreArrowsTimer--;
	}

	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {
	}

	@Override
	public void syncToPlayer(ServerPlayer entityAsPlayer) {
	}

	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		JojoModLivingVariables newData = get(newPlayer);
		newData.foundAnArrow = this.foundAnArrow;
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("DyingBody", isDyingBody);
		nbt.putBoolean("FoundArrow", foundAnArrow);
		nbt.putInt("FindMoreArrowsTimer", findMoreArrowsTimer);
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		isDyingBody = nbt.getBoolean("DyingBody");
		foundAnArrow = nbt.getBoolean("FoundArrow");
		findMoreArrowsTimer = nbt.getInt("FindMoreArrowsTimer");
	}
	
	
	public static JojoModLivingVariables get(LivingEntity entity) {
		return entity.getData(ModDataAttachmentTypes.LIVING_VARS);
	}
	
	@Nullable
	public static JojoModLivingVariables getIfPresent(LivingEntity entity) {
		return ComponentUtil.getExistingDataOrNull(entity, ModDataAttachmentTypes.LIVING_VARS);
	}
	
}
