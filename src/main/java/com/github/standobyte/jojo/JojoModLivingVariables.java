package com.github.standobyte.jojo;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class JojoModLivingVariables<T extends LivingEntity> extends JojoModEntityVariables<T> {
	public boolean isDyingBody = false;
	public Vec3 bleedingParticlesPos = null;
	
	public boolean foundAnArrow;
	public int findMoreArrowsTimer = -1;
	
	public JojoModLivingVariables(T entity) {
		super(entity);
	}
	
	@Override
	public void tick() {
		super.tick();
		bleedingParticlesPos = null;
		if (findMoreArrowsTimer >= 0) findMoreArrowsTimer--;
	}

	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {
		super.syncToTracking(trackingPlayer);
	}

	@Override
	public void syncToPlayer(ServerPlayer entityAsPlayer) {
		super.syncToPlayer(entityAsPlayer);
	}

	@Override
	protected void cloneData(JojoModEntityVariables<?> _newData, boolean wasDeath) {
		super.cloneData(_newData, wasDeath);
		JojoModLivingVariables<?> newData = (JojoModLivingVariables<?>) _newData;
		newData.foundAnArrow = this.foundAnArrow;
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		nbt.putBoolean("DyingBody", isDyingBody);
		nbt.putBoolean("FoundArrow", foundAnArrow);
		nbt.putInt("FindMoreArrowsTimer", findMoreArrowsTimer);
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		isDyingBody = nbt.getBoolean("DyingBody");
		foundAnArrow = nbt.getBoolean("FoundArrow");
		findMoreArrowsTimer = nbt.getInt("FindMoreArrowsTimer");
	}
	
	
	public static JojoModLivingVariables<?> get(LivingEntity entity) {
		return (JojoModLivingVariables<?>) JojoModEntityVariables.get(entity);
	}
	
	@Nullable
	public static JojoModLivingVariables<?> getIfPresent(LivingEntity entity) {
		return (JojoModLivingVariables<?>) JojoModEntityVariables.getIfPresent(entity);
	}
	
}
