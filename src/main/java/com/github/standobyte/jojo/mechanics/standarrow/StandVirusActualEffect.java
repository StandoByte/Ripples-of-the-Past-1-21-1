package com.github.standobyte.jojo.mechanics.standarrow;

import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffect;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModEntityCustomEffects;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.syncheddata.SyncedDataHolderExtended;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StandVirusActualEffect extends EntityCustomEffect implements SyncedDataHolderExtended {
	public static final EntityDataAccessor<Integer> CONSUMED_LEVELS = SynchedEntityData.defineId(StandVirusActualEffect.class, EntityDataSerializers.INT);
	public boolean didDyingEntitySideEffect = false;

	public StandVirusActualEffect() {
		this(ModEntityCustomEffects.STAND_VIRUS.get());
	}

	public StandVirusActualEffect(EntityCustomEffectType<?> effectType) {
		super(effectType);
	}


	@Override
	public void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(CONSUMED_LEVELS, 0);
	}

	@Override
	public <T> void onSyncedDataUpdated(T oldValue, T newValue, EntityDataAccessor<T> dataAccessor) {
	}


	public int getXpLevelsTakenByArrow() {
		return synchedData.get(CONSUMED_LEVELS);
	}

	public int incXpLevelsTakenByArrow() {
		int levels = getXpLevelsTakenByArrow() + 1;
		synchedData.set(CONSUMED_LEVELS, levels);
		return levels;
	}

	public int getStandXpLevelsRequirement() {
		return 30;
	}


	@Override
	protected void start() {}

	@Override
	protected void tick() {
		if (!level.isClientSide() && tickCount % 10 == 0) {
			LivingEntity entity = (LivingEntity) this.entity;
			Holder<MobEffect> vanillaEffect = ModStatusEffects.STAND_VIRUS;

			if (!entity.hasEffect(vanillaEffect)) {
				remove();
				return;
			}
			StandPower power = StandPower.get(entity);
			if (power != null && power.hasPower()) {
				remove();
				return;
			}

			float damage = 4;
			boolean stopEffect = false;
			
			if (entity instanceof Player player) {
				boolean hasXpLevel = player.getAbilities().instabuild || player.experienceLevel > 0;
				if (hasXpLevel) {
					int standXpRequirements = this.getStandXpLevelsRequirement();
					if (this.incXpLevelsTakenByArrow() >= standXpRequirements) {
						stopEffectOnGaveStand = true;
						StandArrowItem.giveStand(level, entity);
					}
				}

				player.giveExperienceLevels(-1);
				if (hasXpLevel) {
					damage /= 10;
					if (damage > entity.getHealth()) {
						damage = Math.min(entity.getHealth() - 0.001f, 0.001f);
					}
				}
				else if (entity.getHealth() < 10) {
					doDyingEntitySideEffect();
				}
			}
			else if (entity.getHealth() <= damage) {
				stopEffectOnGaveStand = true;
				if (StandArrowItem.giveStand(level, entity)) {
					damage = 0;
				}
				else {
					doDyingEntitySideEffect();
				}
			}
			
			if (damage > 0) {
				entity.hurt(DamageUtil.make(level, ModDamageTypes.STAND_ARROW_VIRUS), damage);
			}
			if (stopEffect || stopEffectOnGaveStand) {
				entity.removeEffect(vanillaEffect);
				this.remove();
			}
		}
	}
	
	protected float damageAmount() {
		return 4;
	}

	protected boolean stopEffectOnGaveStand;
	@Override
	protected void stop() {
		if (!stopEffectOnGaveStand) {
			Level level = entity.level();
			if (!level.isClientSide() && entity.isAlive()) {
				StandArrowItem.giveStand(level, (LivingEntity) entity);
			}
		}
	}
	
	// TODO stand virus side effect for the entity that's about to die
	protected void doDyingEntitySideEffect() {
		if (!didDyingEntitySideEffect) {
			int effect = entity.getRandom().nextInt(6);
			// note: make the effects not harm the entity itself
			switch (effect) {
				case 0 -> {
					// ghast fireball explosion
				}
				case 1 -> {
					// harming / poison / wither cloud
				}
				case 2 -> {
					// evoker floor spikes
				}
				case 3 -> {
					// wind charges
				}
				case 4 -> {
					// warden sonic attack
				}
				case 5 -> {
					// freeze spikes
				}
			}
			didDyingEntitySideEffect = true;
		}
	}

	
	@Override
	protected void writeAdditionalSaveData(CompoundTag nbt) {
		nbt.putInt("LevelsConsumed", synchedData.get(CONSUMED_LEVELS));
		nbt.putBoolean("DidSideEffect", didDyingEntitySideEffect);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		synchedData.set(CONSUMED_LEVELS, nbt.getInt("LevelsConsumed"));
		didDyingEntitySideEffect = nbt.getBoolean("DidSideEffect");
	}

}
