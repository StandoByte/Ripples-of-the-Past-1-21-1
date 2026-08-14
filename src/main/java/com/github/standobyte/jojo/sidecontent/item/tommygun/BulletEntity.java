package com.github.standobyte.jojo.sidecontent.item.tommygun;

import java.util.LinkedList;
import java.util.List;

import com.github.standobyte.jojo.customobjects.entity_projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.subsystems.target.ActionTarget.TargetType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BulletEntity extends ModdedProjectileEntity {
	public final List<Vec3> tracePos = new LinkedList<>();
	public Vec3 initialPos;

	public BulletEntity(LivingEntity shooter, Level level) {
		super(ModEntityTypes.BULLET.get(), shooter, level);
	}

	public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
		super(type, level);
	}

	@Override
	public int ticksLifespan() {
		return 40;
	}

	@Override
	public float getBaseDamage() {
		return 2.0F;
	}

	@Override
	protected float getMaxHardnessBreakable() {
		return 0.3F;
	}

	@Override
	public boolean standDamage() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (tickCount == 5) {
			setNoGravity(false);
		}
		if (level().isClientSide()) {
			Vec3 pos = position();
			boolean addPos = true;
			if (tracePos.size() > 1) {
				Vec3 lastPos = tracePos.get(tracePos.size() - 1);
				addPos &= pos.distanceToSqr(lastPos) >= 0.0625;
			}
			if (addPos) {
				tracePos.add(pos);
			}
		}
	}

	@Override
	protected double getGravityAcceleration() {
		return 1.5;
	}


	@Override
	public boolean hasDeflectedVisuals() {
		return true;
	}

	@Override
	public void setIsDeflected(Vec3 deflectVec, Vec3 deflectPos) {
		super.setIsDeflected(deflectVec, deflectPos);
		if (level().isClientSide()) {
			setDeltaMovement(deflectVec);
			tracePos.add(deflectPos);
		}
	}


	protected boolean blockDestroyed;
	@Override
	protected void afterBlockHit(BlockHitResult blockRayTraceResult, boolean blockDestroyed) {
		this.blockDestroyed = blockDestroyed;
		Level level = level();
		if (blockDestroyed) {
			if (!level.isClientSide()) {
				setDeltaMovement(getDeltaMovement().scale(0.9));
				checkHit();
			}
		}
		else {
			BlockPos blockPos = blockRayTraceResult.getBlockPos();
			BlockState blockState = level.getBlockState(blockPos);
			blockSound(blockPos, blockState);
		}
	}

	private void blockSound(BlockPos blockPos, BlockState blockState) {
		Level level = level();
		SoundType soundType = blockState.getSoundType(level, blockPos, this);
		//level.playSound(null, blockPos, soundType.getHitSound(), SoundCategory.BLOCKS, 1.0F, soundType.getPitch() * 0.5F);
	}

	@Override
	protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
		if (!(targetType == TargetType.BLOCK && blockDestroyed)) {
			super.breakProjectile(targetType, hitTarget);
		}
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
		super.readSpawnData(additionalData);
		initialPos = position();
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return true;
	}

}
