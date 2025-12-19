package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.mc.StatusEffectUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;

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
//		friendlyFire = true;
	}
	
	// TODO ! (CD heal) gray out the ability if you're not aiming at a correct target

	public static class HealingAction extends EntityActionInstance {
		public boolean wasHealingLastTick;
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
		
		// TODO ! (CD heal) pose only when it has a target (start counting animation ticks from there)
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		

		@Override
		public void actionTick() {
			ActionTarget aimTarget = LivingComponentAction.getAim(performer).getTarget();
			StandEntity standEntity = performer instanceof StandEntity s ? s : null;

			if (aimTarget.getType() == TargetType.ENTITY) {
				standRotationTarget = aimTarget;
				aimAs = AimingEntity.STAND;
			}
			else {
				standRotationTarget = ActionTarget.EMPTY;
				aimAs = AimingEntity.CAMERA_ENTITY;
			}
			
			HealResult healingResult = restoreTarget(aimTarget, standEntity);

			if (healingResult.isHealing) {
				if (!wasHealingLastTick) {
					_setStandOffset(standEntity, new Vec3(0, standEntity.Y_OFFSET, 1.5), 
							StandOffsetFromUser.Rotations.HEAD_XY, false);
//					standEntity.offsetFromUser.syncToTracking();
					if (healingResult.barrageVisuals) {
						// TODO ! (CD heal) barrage visuals
					}
					// TODO ! (CD heal) healing sound
				}
			}
			else {
				if (wasHealingLastTick) {
					if (standEntity != null) {
						standEntity.offsetFromUser.resetToIdle();
//						standEntity.offsetFromUser.syncToTracking();
					}
					// TODO ! (CD heal) stop the sound
				}
			}
			this.wasHealingLastTick = healingResult.isHealing;
			
			userWalkSpeed = healingResult.isHealing ? 0.6f : 1;
		}

		public HealResult restoreTarget(ActionTarget target, StandEntity crazyDiamond) {
			HealResult result = HealResult.clearGetObj();
			LivingEntity user = getPowerUser();
			result.barrageVisuals = user != null && ModStatusEffects.isInResolveEffect(user);
			
			if (target.getType() == TargetType.ENTITY) {
				Entity targetEntity = target.getEntity();
				if (targetEntity != null) {
					Level level = targetEntity.level();
					
					if (targetEntity == performer || targetEntity == user) {
						if (user != null && level.isClientSide() && user == ClientProxy.getClientPlayer()) {
							ClientProxy.setOverlayMessage(ConditionCheck.message("cd_heal_self"), false);
						}
						return result;
					}

					if (targetEntity instanceof LivingEntity targetLiving) {
						return healLivingEntity(level, targetLiving, crazyDiamond);
					}

					else if (targetEntity instanceof ModEntityWithHealth toHeal) {
						if (toHeal.getHealth() < toHeal.getMaxHealth()) {
							if (!level.isClientSide()) {
								toHeal.setHealth(toHeal.getHealth() + toHeal.getMaxHealth() / 40 * (float) healSpeedWithConfig(crazyDiamond));
							}
							addParticlesAround(targetEntity);
							result.isHealing = true;
							return result;
						}
					}

					else if (targetEntity instanceof Boat toHeal) {
						if (toHeal.getDamage() > 0) {
							if (!level.isClientSide()) {
								toHeal.setDamage(Math.max(toHeal.getDamage() - (float) healSpeedWithConfig(crazyDiamond), 0));
							}
							addParticlesAround(targetEntity);
							result.isHealing = true;
							return result;
						}
					}
				}
			}
			return result;
		}
		
		public static class HealResult {
			static HealResult instance = new HealResult();
			
			public static HealResult clearGetObj() {
				instance.isHealing = false;
				instance.barrageVisuals = false;
				return instance;
			}
			
			public boolean isHealing;
			public boolean barrageVisuals;
		}

		public HealResult healLivingEntity(Level level, LivingEntity entity, StandEntity crazyDiamond) {
			HealResult result = HealResult.instance;
			
			LivingEntity toHeal = StandUtil.getStandUser(entity);
			// FIXME (1.16.5) disable it if the target is a dead body already
			if (entity.deathTime > 0) {
				// boolean resolveEffect = standEntity.getUser() != null && ModStatusEffects.isInResolveEffect(standEntity.getUser());
				// if (!resolveEffect && entity.deathTime > 1 || entity.deathTime > 15) {
				// 	return false;
				// }
				if (entity.deathTime > 15) {
					return result;
				}

				toHeal.deathTime = Math.max(toHeal.deathTime - 2, 0);
				entity.deathTime = toHeal.deathTime;
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

			if (result.isHealing) {
				addParticlesAround(toHeal);
				if (toHeal != entity) {
					addParticlesAround(entity);
				}
				return result;
			}

			return result;
		}

		// TODO ! (CD heal) healSpeed config
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
			int particlesCount = Math.max(Mth.ceil(entity.getBbWidth() * (entity.getBbHeight() * 2 * entity.getBbHeight())), 1);
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
