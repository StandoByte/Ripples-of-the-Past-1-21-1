package com.github.standobyte.jojo.powersystem.standpower.entity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StandStatFormulas {

	public static float getHeavyAttackDamage(double strength) {
		float damage = Math.max((float) strength, 1F) * 0.75f;
		return damage;
	}
	
	public static float getHeavyAttackWindup(double speed, float finisherMeter) {
		float f = Math.max(30 - (float) speed * 0.75f, 0);
		float min = f / 3;
		float max = f * 2 / 3;
		finisherMeter = Math.min(finisherMeter / 2, 1);
		return Mth.lerp(finisherMeter, max, min);
	}



	public static float getLightAttackDamage(double strength) {
		float damage = Math.max((float) strength * 0.25F, 0.0001F);
		return damage;
	}

//	public static float getLightAttackWindup(double speed, float finisherMeter, float guardCounter, boolean firstPunch) {
//		float val = (24 - (float) speed) / 4;
//		if (val <= 0) return 0;
//
//		if (val > 2) {
//			val = Math.max(val * (1.0F - finisherMeter * 0.4F), 2);
//		}
//		val *= (1F - guardCounter);
//
//		if (firstPunch) {
//			val /= 2;
//		}
//
//		return val;
//	}



	public static float getBarrageHitDamage(double strength) {
		float damage = 0.04F + (float) strength * 0.01F;
		return damage;
	}

	public static float getBarrageHitsPerSecond(double speed) {
		return Math.max((float) speed * 8.0f - 20.0f, 0);
	}

	public static float getBarrageMaxDuration(double durability) {
		return 20 + ((float) durability * 5.0f);
	}



	public static float getPhysicalResistance(double durability, double strength, float blocked, float damageDealt) {
		double x = (durability * 2 + strength * 1) / 3;
		double resistance = x / (x + 4); // simplified `1 - 1 / (x / 4 + 1)`
		double dmgCoeff = 1;

		if (blocked > 0) {
			dmgCoeff -= 0.8 * blocked;
			double furtherReductionCap = durability / 2;

			if (damageDealt < furtherReductionCap) {
				dmgCoeff *= damageDealt / furtherReductionCap;
			}
		}

//		double config = JojoModConfig.getCommonConfigInstance(false).standResistanceMultiplier.get();
//		if (config > 1) {
//			dmgCoeff /= config;
//		}

		resistance += (1 - resistance) * Mth.clamp(1 - dmgCoeff, 0, 1);
		return (float) resistance;
	}

	public static float getBlockingKnockbackMult(double durability) {
		return Mth.clamp((float) Math.pow(2, 1 - durability / 4), 0, 1);
	}

	public static float getStaminaMultiplier(double durability) {
		return 1 + (float) durability / 16;
	}
	

	public static float getGuardStaminaCost(float incomingDamage) {
		return (float) Math.pow(incomingDamage, 2) / 2;
	}

	public static int getGuardBreakTicks(double durability) {
		return Math.max(240 - (int) (durability * 10), 80);
	}

	public static float getMaxBarrageParryTickDamage(double durability) {
		return Math.max(((float) durability - 4F) * 0.125F, 0);
	}
	

	public static float getLeapStrength(double strength) {
		return (float) Math.min(strength, 40) / 5F;
	}

	public static float getLeapChargeTime(double speed) {
		float pokaHz = 0;
		return pokaHz;
	}

	public static int leapCooldown(double movementSpeed) {
		return dashCooldown(movementSpeed) * 2 + 5;
	}

	public static int dashCooldown(double movementSpeed) {
		return Math.max((int) (30 - movementSpeed * 25), 2);
	}

	public static double getMovementSpeed(double speed) {
		return 0.1 + speed * 0.05;
	}
	

	public static float rangeStrengthFactor(double rangeEffective, double rangeMax, double distance) {
		if (distance <= rangeEffective || rangeEffective >= rangeMax) {
			return 1F;
		}
		float f = (float) ((rangeMax - rangeEffective) / (2 * rangeEffective - rangeMax - distance));
		return Math.max(f * f, 0.25f);
	}
	
	// * 8.5618
	public static float projectileVelocityScaling(double strength, float velocity) {
		if (strength > 8) {
			float multiplier = ((float) strength - 8) / 12f + 1;
			velocity *= multiplier;
		}
		return velocity;
	}
	
	public static float projectileInaccuracyScaling(double precision, float inaccuracy) {
		// 8 - 1; 12 - 2/3; 16 - 1/3; 20 - 0
		float inaccuracyMultiplier = (float) Math.max((-precision / 12.0 + 5.0 / 3.0), 0);
		return inaccuracy * inaccuracyMultiplier;
	}

//	public float getProjectileInaccuracy(double precision, float inaccuracyBase) {
//		return Math.max((inaccuracyBase + 1) * 8 / Math.max((float) precision, 4) - 1, 0);
//	}
//
//	public static double projectileFireRateScaling(StandEntity standEntity, StandPower standPower) {
//		return standEntity.getAttackSpeed() / standPower.getPowerType().getStandStats().speed();
//	}
	
	
	public static BlockMiningTier[] miningTiers = new BlockMiningTier[] {
			BlockMiningTier.EMPTY_ARMS, 
			new BlockMiningTier.VanillaTierWrapper(Tiers.WOOD), 
			new BlockMiningTier.VanillaTierWrapper(Tiers.STONE), 
			new BlockMiningTier.VanillaTierWrapper(Tiers.IRON), 
			new BlockMiningTier.VanillaTierWrapper(Tiers.DIAMOND), 
			new BlockMiningTier.VanillaTierWrapper(Tiers.NETHERITE),
			BlockMiningTier.ANY_BLOCK
	};
	
	@Nullable
	public static BlockMiningTier getStandHarvestLevel(double strength) {
		/* 2 - none
		 * 5 - wood
		 * 8 - stone
		 * 11 - iron
		 * 14 - diamond
		 * 17 - netherite
		 * 20 - netherite+
		 */
		int tier = (int) (strength - 2) / 3;
		return miningTiers[Mth.clamp(tier, 0, miningTiers.length - 1)];
	}
	
	public static interface BlockMiningTier {
		boolean canMine(BlockState blockState);
		
		public static final BlockMiningTier EMPTY_ARMS = new BlockMiningTier.EmptyArms();
		public static final BlockMiningTier ANY_BLOCK = new BlockMiningTier.Any();
		
		public static class EmptyArms implements BlockMiningTier {
			@Override public boolean canMine(BlockState blockState) {
				return !blockState.requiresCorrectToolForDrops();
			}
		}
		
		public static record VanillaTierWrapper(Tier tier) implements BlockMiningTier {
			@Override public boolean canMine(BlockState blockState) {
				return !(blockState.requiresCorrectToolForDrops() && blockState.is(tier.getIncorrectBlocksForDrops()));
			}
		}
		
		public static class Any implements BlockMiningTier {
			@Override public boolean canMine(BlockState blockState) {
				return true;
			}
		}
	}
	
	public static float getBlockHardness(double strength, BlockState blockState, Level level, BlockPos blockPos) {
		return getBlockHardness(getStandHarvestLevel(strength), blockState, level, blockPos);
	}
	
	public static float getBlockHardness(@Nullable BlockMiningTier harvestTier, BlockState blockState, Level level, BlockPos blockPos) {
		float hardness = blockState.getDestroySpeed(level, blockPos);
		if (hardness < 0) {
			return -1;
		}
		
		if (harvestTier != null) {
			boolean canMineOnTier = harvestTier.canMine(blockState);
			
			if (canMineOnTier) {
				hardness *= 0.3f;
			}
		}
		
		return hardness;
	}
	
	public static float getBarrageBlockMiningEfficiency(double strength, double speed) {
		float multSpeed = getBarrageHitsPerSecond(speed) / 8;
		float multStrength = strength > 17 ? (float) (strength - 16) * 2 : 1;
		return multStrength * multSpeed;
	}
	
	public static boolean canGrabBlock(double strength, BlockState blockState, Level level, BlockPos blockPos) {
		float hardness = blockState.getDestroySpeed(level, blockPos);
		if (hardness < 0) {
			return false;
		}
		BlockMiningTier harvestTier = getStandHarvestLevel(strength);
		boolean canMineOnTier = harvestTier.canMine(blockState);
		return canMineOnTier;
	}
	
}
