package com.github.standobyte.jojo.customobjects.explosion;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@SuppressWarnings("deprecation")
public class ExplosionDamageCalculatorWasAStupidIdeaJustSaying extends ExplosionDamageCalculator {
	public CustomExplosion explosion;

	public ExplosionDamageCalculatorWasAStupidIdeaJustSaying(CustomExplosion rotpCustomExplosion) {
		this.explosion = rotpCustomExplosion;
	}

	@Override
	public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid) {
		return this.explosion.getBlockExplosionResistance(reader, pos, state, fluid);
	}

	@Override
	public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
		return this.explosion.shouldBlockExplode(reader, pos, state, power);
	}

	@Override
	public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
		return this.explosion.shouldDamageEntity(entity);
	}

	@Override
	public float getKnockbackMultiplier(Entity entity) {
		return this.explosion.getKnockbackMultiplier(entity);
	}

	@Override
	public float getEntityDamageAmount(Explosion explosion, Entity entity) {
		return this.explosion.getEntityDamageAmount(entity);
	}
}
