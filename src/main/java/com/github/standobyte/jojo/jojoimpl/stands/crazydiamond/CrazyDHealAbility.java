package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import org.spongepowered.include.com.google.common.base.Objects;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.core.ModEntityDataSerializers;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.syncdata.SyncedDataHolderExtended;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.StandUtil.StandAndUserEntity;
import com.github.standobyte.jojo.util.mc.StatusEffectUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CrazyDHealAbility extends StandEntityAbility {
	public float healSpeed;

	public CrazyDHealAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, HealingAction::new);
		setButtonHoldPhase(ActionPhase.PERFORM);
		healSpeed = 1;
		// TODO ! (CD heal) friendly fire
		// TODO ! (CD heal) aim at dying entities
//		friendlyFire = true;
	}
	
	// TODO ! (CD heal) gray out the ability if you're not aiming at a correct target

	public static class HealingAction extends EntityActionInstance implements SyncedDataHolderExtended {
		public BleedingTimer bleedingTimer;

		public HealingAction(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				setPhaseStart(ActionPhase.RECOVERY);
				syncPhaseChanges();
			}
		}
		
		// TODO !! (CD heal) pose only when it has a target (start counting animation ticks from there)
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		

		@Override
		public void actionTick() {
			Level level = level();
			
			HealResult curHealing;
			if (!level.isClientSide()) {
				ActionTarget aimTarget = LivingComponentAction.getAim(performer).getTarget();
				StandEntity standEntity = performer instanceof StandEntity s ? s : null;
				curHealing = restoreTarget(aimTarget, standEntity);
				setSynchedData(HEAL_RESULT, curHealing);
			}
			
			else {
				curHealing = getSynchedData(HEAL_RESULT);
				if (curHealing.isHealing && curHealing.target.getType() == TargetType.ENTITY) {
					Entity targetEntity = curHealing.target.getEntity();
					if (targetEntity != null) {
						if (targetEntity instanceof LivingEntity targetLiving) {
							StandAndUserEntity standAndUser = StandUtil.getStandAndUser(targetLiving);
							
							if (standAndUser.standUser != null) 
								addParticlesAround(standAndUser.standUser);
							if (standAndUser.standEntity != null) 
								addParticlesAround(standAndUser.standEntity);
							
							if (curHealing.deathTime != HealResult.NO_DEATH_TIME_CHANGE) {
								if (standAndUser.standUser != null) standAndUser.standUser.deathTime = curHealing.deathTime;
								if (standAndUser.standEntity != null) standAndUser.standEntity.deathTime = curHealing.deathTime;
							}
						}
						else {
							addParticlesAround(targetEntity);
						}
					}
				}
			}
			userWalkSpeed = curHealing.isHealing ? 0.6f : 1;
		}
		
		public void onHealResultUpdated(HealResult old, HealResult cur) {
			Level level = level();
			cur.target.resolveEntityId(level);
			
			if (old == null || cur.isHealing != old.isHealing) {
				StandEntity standEntity = performer instanceof StandEntity __ ? __ : null;
				if (cur.isHealing) {
					if (standEntity != null) {
						_setStandOffset(standEntity, new Vec3(0, standEntity.Y_OFFSET, 1.5), 
								StandOffsetFromUser.Rotations.HEAD_XY, false);
					}
					if (cur.barrageVisuals) {
						// TODO !! (CD heal) barrage visuals
					}
					if (level.isClientSide() && standEntity != null) {
						Entity targetEntity = cur.target.getEntity();
						if (targetEntity != null) {
							ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
									ModSoundEvents.CRAZY_DIAMOND_FIX_STARTED.get(), standEntity), 
									standEntity.getSoundSource(), 1, 1, targetEntity, level));
							
							ClientsideSoundsHelper.playNonVanillaClassSound(new EntityStoppableSoundInstance(ClientsideSoundsHelper.withStandSkin(
									ModSoundEvents.CRAZY_DIAMOND_FIX_LOOP.get(), standEntity), 
									standEntity.getSoundSource(), 1, 1, targetEntity, level.random.nextLong(), 
									() -> this.isOver() || this.phase != ActionPhase.PERFORM || !this.getSynchedData(HEAL_RESULT).isHealing));
						}
					}
				}
				else if (old == null || old.isHealing) {
					if (standEntity != null) {
						standEntity.offsetFromUser.resetToIdle();
					}
					if (level.isClientSide() && standEntity != null) {
						Entity targetEntity = old != null ? old.target.getEntity() : null;
						if (targetEntity != null) {
							ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
									ModSoundEvents.CRAZY_DIAMOND_FIX_ENDED.get(), standEntity), 
									standEntity.getSoundSource(), 1, 1, targetEntity, level));
						}
					}
				}
			}

			if (old == null || !cur.target.equals(old.target)) {
				if (cur.target.getType() == TargetType.ENTITY) {
					standRotationTarget = cur.target;
					aimAs = AimingEntity.STAND;
					
					Entity targetEntity = cur.target.getEntity();
					LivingEntity user = getPowerUser();
					if (user == targetEntity && user != null && user.level().isClientSide() && user == ClientProxy.getClientPlayer()) {
						ClientProxy.setOverlayMessage(ConditionCheck.message("cd_heal_self"), false);
					}
				}
				else {
					standRotationTarget = ActionTarget.EMPTY;
					aimAs = AimingEntity.CAMERA_ENTITY;
				}
			}
		}

		public HealResult restoreTarget(ActionTarget target, StandEntity crazyDiamond) {
			HealResult result = new HealResult();
			result.target = target;
			LivingEntity user = getPowerUser();
			result.barrageVisuals = user != null && ModStatusEffects.isInResolveEffect(user);
			
			if (target.getType() == TargetType.ENTITY) {
				Entity targetEntity = target.getEntity();
				if (targetEntity != null) {
					Level level = targetEntity.level();
					
					if (targetEntity == performer || targetEntity == user) {
						return result;
					}

					if (targetEntity instanceof LivingEntity targetLiving) {
						return healLivingEntity(level, targetLiving, crazyDiamond, result);
					}

					else if (targetEntity instanceof ModEntityWithHealth toHeal) {
						if (toHeal.getHealth() < toHeal.getMaxHealth()) {
							if (!level.isClientSide()) {
								toHeal.setHealth(toHeal.getHealth() + toHeal.getMaxHealth() / 40 * (float) healSpeedWithConfig(crazyDiamond));
							}
							result.isHealing = true;
							return result;
						}
					}

					else if (targetEntity instanceof Boat toHeal) {
						if (toHeal.getDamage() > 0) {
							if (!level.isClientSide()) {
								toHeal.setDamage(Math.max(toHeal.getDamage() - (float) healSpeedWithConfig(crazyDiamond), 0));
							}
							result.isHealing = true;
							return result;
						}
					}
				}
			}
			return result;
		}

		public HealResult healLivingEntity(Level level, LivingEntity entity, StandEntity crazyDiamond, HealResult result) {
			LivingEntity toHeal = StandUtil.getStandUser(entity);
			// FIXME (1.16.5) disable it if the target is a dead body already
			if (entity.deathTime > 0) {
				// boolean resolveEffect = standEntity.getUser() != null && ModStatusEffects.isInResolveEffect(standEntity.getUser());
				// if (!resolveEffect && entity.deathTime > 1 || entity.deathTime > 15) {
				// 	return false;
				// }

				toHeal.deathTime = Math.max(toHeal.deathTime - 2, 0);
				result.deathTime = entity.deathTime;
				
				if (!level.isClientSide() && toHeal.deathTime <= 0 && toHeal.getHealth() <= 0) {
					toHeal.setHealth(0.001F);
					JojoModUtil.onLivingResurrect(toHeal);
				}
				result.isHealing = true;
				result.barrageVisuals = true;
			}
			else {
				float healingSpeed = (float) healSpeedWithConfig(crazyDiamond);
				float health = toHeal.getHealth();
				float maxHealth = toHeal.getMaxHealth();
				
				if (toHeal.getHealth() < toHeal.getMaxHealth()) {
					result.isHealing = true;
					result.barrageVisuals |= health < maxHealth * 0.5f;
					if (!level.isClientSide()) {
						toHeal.setHealth(health + 0.5F * healingSpeed);
					}
				}
				
				MobEffectInstance bleeding = toHeal.getEffect(ModStatusEffects.BLEEDING);
				if (bleeding != null) {
					result.isHealing = true;
					result.barrageVisuals |= bleeding.getAmplifier() > 1;
					if (!level.isClientSide()) {
						int reduceBleedingTime = (int) (20 / healingSpeed);
						int reduceBleeding = MathUtil.fractionRandomInc(healingSpeed * 4);
						if (bleedingTimer == null) bleedingTimer = new BleedingTimer();
						// reduce bleeding by 5~6 ticks
						StatusEffectUtil.reduceEffect(toHeal, ModStatusEffects.BLEEDING, reduceBleeding, 0);
						if (bleedingTimer.tick(reduceBleedingTime) && bleeding.getAmplifier() > 0) {
							// once every 14 ticks, reduce bleeding level by 1, unless it's already at the lowest level
							StatusEffectUtil.reduceEffect(toHeal, ModStatusEffects.BLEEDING, 0, 1);
						}
					}
				}
			}

			return result;
		}


		public static final EntityDataAccessor<HealResult> HEAL_RESULT = SynchedEntityData.defineId(HealingAction.class, ModEntityDataSerializers.CD_HEAL_RESULT.get());
		public static class HealResult {
			public ActionTarget target;
			public boolean isHealing;
			public boolean barrageVisuals;
			public int deathTime;
			
			public static final int NO_DEATH_TIME_CHANGE = 67;
			
			public HealResult() {
				this(ActionTarget.EMPTY, false, false, NO_DEATH_TIME_CHANGE);
			}
			
			public HealResult(ActionTarget target, boolean isHealing, boolean barrageVisuals, int deathTime) {
				this.target = target;
				this.isHealing = isHealing;
				this.barrageVisuals = barrageVisuals;
				this.deathTime = deathTime;
			}
			
			@Override
			public boolean equals(Object obj) {
				if (obj.getClass() == HealResult.class) {
					HealResult other = (HealResult) obj;
					return this.target.equals(other.target) 
							&& this.isHealing == other.isHealing
							&& this.barrageVisuals == other.barrageVisuals
							&& this.deathTime == other.deathTime;
				}
				return false;
			}
			
			@Override
			public int hashCode() {
				return Objects.hashCode(target, isHealing, barrageVisuals, deathTime);
			}
			
			public static final StreamCodec<? super RegistryFriendlyByteBuf, HealResult> STREAM_CODEC = StreamCodec.composite(
					ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID, heal -> heal.target, 
					ByteBufCodecs.BOOL, heal -> heal.isHealing, 
					ByteBufCodecs.BOOL, heal -> heal.barrageVisuals, 
					ByteBufCodecs.VAR_INT, heal -> heal.deathTime, 
					HealResult::new);

			public HealResult copy() {
				return new HealResult(this.target.copy(), this.isHealing, this.barrageVisuals, this.deathTime);
			}
		}
		
		@Override
		public void defineSynchedData(SynchedEntityData.Builder builder) {
			builder.define(HEAL_RESULT, new HealResult());
		}
		
		@Override
		public <T> void onSyncedDataUpdated(T oldValue, T newValue, EntityDataAccessor<T> dataKey) {
			if (dataKey == HEAL_RESULT) {
				onHealResultUpdated((HealResult) oldValue, (HealResult) newValue);
			}
		}
		
		

		// TODO (CD heal) healSpeed config
		protected double healSpeedWithConfig(StandEntity standEntity) {
			return crazyDRestorationSpeed(standEntity)/* * healSpeed*/;
		}

	}

	public static double crazyDRestorationSpeed(StandEntity standEntity) {
		return standEntity.getAttackSpeed() * 0.05F + 0.55;
	}

	public static void addParticlesAround(Entity entity) {
		Level level = entity.level();
		if (level.isClientSide() && ClientGlobals.canSeeStands) {
			int particlesCount = Math.max(Mth.ceil(0.5f * entity.getBbWidth() * (entity.getBbHeight() * 2 * entity.getBbHeight())), 1);
			for (int i = 0; i < particlesCount; i++) {
				level.addParticle(ModParticles.CD_RESTORATION.get(), entity.getRandomX(1), entity.getRandomY(), entity.getRandomZ(1), 0, 0, 0);
			}
		}
	}

	public static class BleedingTimer {
		public int timer;

		public BleedingTimer() {
			this.timer = 0;
		}

		public boolean tick(int reduceEffectTime) {
			if (timer++ >= reduceEffectTime) {
				timer = 0;
				return true;
			}
			return false;
		}
	}

}
