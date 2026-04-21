package com.github.standobyte.jojo.adventure.npc;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.entity_playerwrapper.EntityAsPlayerWrapper;
import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class NpcPlayerActions {

	public static void emulatePlayerAttack(LivingEntity attacker, Entity target) {
		Player asPlayer = attacker instanceof Player player ? player : attacker instanceof EntityAsPlayerWrapper playerWrapper ? playerWrapper.asPlayer() : null;
		Level level = attacker.level();
		ServerLevel serverLevel = level instanceof ServerLevel __ ? __ : null;
		//if (asPlayer != null && !CommonHooks.onPlayerAttackTarget(asPlayer, target)) return;
		
		if (!target.isAttackable() || target.skipAttackInteraction(attacker)) return;
		
		float dmgAmount = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
		ItemStack weaponItem = attacker.getWeaponItem();
		if (attacker.isAutoSpinAttack()) {
			dmgAmount = attacker.autoSpinAttackDmg;
			if (attacker.autoSpinAttackItemStack != null) {
				weaponItem = attacker.autoSpinAttackItemStack;
			}
		}
		
		DamageSource dmgSource = attacker.damageSources().source(DamageTypes.PLAYER_ATTACK, attacker);
		float enchOnlyDmgAmount = getEnchantedDamage(target, dmgAmount, weaponItem, dmgSource) - dmgAmount;
		float attackStrScale = asPlayer != null ? asPlayer.getAttackStrengthScale(0.5F) : 1;
		dmgAmount *= 0.2F + attackStrScale * attackStrScale * 0.8F;
		enchOnlyDmgAmount *= attackStrScale;
		if (target.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE)
				&& target instanceof Projectile projectile
				&& projectile.deflect(ProjectileDeflection.AIM_DEFLECT, attacker, attacker, true)) {
			attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), 
					SoundEvents.PLAYER_ATTACK_NODAMAGE, attacker.getSoundSource());
			return;
		}

		if (dmgAmount > 0.0F || enchOnlyDmgAmount > 0.0F) {
			boolean fullStrength = attackStrScale > 0.9F;
			boolean extraKnockback;
			if (attacker.isSprinting() && fullStrength) {
				level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), 
						SoundEvents.PLAYER_ATTACK_KNOCKBACK, attacker.getSoundSource(), 1.0F, 1.0F);
				extraKnockback = true;
			} else {
				extraKnockback = false;
			}

			dmgAmount += weaponItem.getItem().getAttackDamageBonus(target, dmgAmount, dmgSource);
			boolean crit = fullStrength
					&& attacker.fallDistance > 0.0F
					&& !attacker.onGround()
					&& !attacker.onClimbable()
					&& !attacker.isInWater()
					&& !attacker.hasEffect(MobEffects.BLINDNESS)
					&& !attacker.isPassenger()
					&& target instanceof LivingEntity
					&& !attacker.isSprinting();
			float damageMultiplier = crit ? 1.5F : 1.0F;
			boolean critBlocksSweep = crit;
			//var critEvent = CommonHooks.fireCriticalHit(attacker, target, crit, damageMultiplier);
			//crit = critEvent.isCriticalHit();
			//damageMultiplier = critEvent.getDamageMultiplier();
			//critBlocksSweep &= critEvent.disableSweep();
			if (crit) {
				dmgAmount *= damageMultiplier;
			}

			float dmgAmountFinal = dmgAmount + enchOnlyDmgAmount;
			boolean isSweeping = false;
			double walkDist = attacker.walkDist - attacker.walkDistO;
			if (fullStrength && !critBlocksSweep && !extraKnockback && attacker.onGround() && walkDist < attacker.getSpeed()) {
				ItemStack mainHandItem = attacker.getItemInHand(InteractionHand.MAIN_HAND);
				isSweeping = mainHandItem.canPerformAction(ItemAbilities.SWORD_SWEEP);
			}

			//var sweepEvent = CommonHooks.fireSweepAttack(attacker, target, isSweeping);
			//isSweeping = sweepEvent.isSweeping();

			float prevHealth = 0.0F;
			if (target instanceof LivingEntity livingentity) {
				prevHealth = livingentity.getHealth();
			}

			Vec3 vec3 = target.getDeltaMovement();
			boolean hurt = target.hurt(dmgSource, dmgAmountFinal);
			if (hurt) {
				// can't use AT on LivingEntity#getKnockback, because other mods may override it with protected access and crash
				float knockback = getKnockback(attacker, target, weaponItem, dmgSource) + (extraKnockback ? 1.0F : 0.0F);
				if (knockback > 0.0F) {
					if (target instanceof LivingEntity targetLiving) {
						targetLiving.knockback(
								knockback * 0.5,
								Mth.sin(attacker.getYRot() * MathUtil.DEG_TO_RAD),
								-Mth.cos(attacker.getYRot() * MathUtil.DEG_TO_RAD));
					} else {
						target.push(
								-Mth.sin(attacker.getYRot() * MathUtil.DEG_TO_RAD) * knockback * 0.5F,
								0.1,
								Mth.cos(attacker.getYRot() * MathUtil.DEG_TO_RAD) * knockback * 0.5F);
					}

					attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
					attacker.setSprinting(false);
				}

				if (isSweeping) {
					float sweepBaseDmg = 1.0F + (float)attacker.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * dmgAmount;
					double entityReachSq = Mth.square(attacker.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
					AABB sweepHitbox = target.getBoundingBox().inflate(1, 0.25, 1);
					if (asPlayer != null) {
						sweepHitbox = weaponItem.getSweepHitBox(asPlayer, target);
					}

					for (LivingEntity sweepTarget : level.getEntitiesOfClass(LivingEntity.class, sweepHitbox)) {
						if (sweepTarget != attacker
								&& sweepTarget != target
								&& !attacker.isAlliedTo(sweepTarget)
								&& (!(sweepTarget instanceof ArmorStand) || !((ArmorStand)sweepTarget).isMarker())
								&& attacker.distanceToSqr(sweepTarget) < entityReachSq) {
							float sweepDmg = getEnchantedDamage(sweepTarget, sweepBaseDmg, weaponItem, dmgSource) * attackStrScale;
							sweepTarget.knockback(
									0.4,
									Mth.sin(attacker.getYRot() * MathUtil.DEG_TO_RAD),
									-Mth.cos(attacker.getYRot() * MathUtil.DEG_TO_RAD));
							sweepTarget.hurt(dmgSource, sweepDmg);
							if (serverLevel != null) {
								EnchantmentHelper.doPostAttackEffects(serverLevel, sweepTarget, dmgSource);
							}
						}
					}

					level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), 
							SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F);
					
					if (serverLevel != null) {
						double d0 = -Mth.sin(attacker.getYRot() * MathUtil.DEG_TO_RAD);
						double d1 = Mth.cos(attacker.getYRot() * MathUtil.DEG_TO_RAD);
						serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, 
								attacker.getX() + d0, attacker.getY(0.5), attacker.getZ() + d1, 0, d0, 0.0, d1, 0.0);
					}
				}

				if (target instanceof ServerPlayer serverPlayerTarget && target.hurtMarked) {
					serverPlayerTarget.connection.send(new ClientboundSetEntityMotionPacket(target));
					target.hurtMarked = false;
					target.setDeltaMovement(vec3);
				}

				if (crit) {
					level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), 
							SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
					if (serverLevel != null) {
						serverLevel.getChunkSource().broadcastAndSend(attacker, 
								new ClientboundAnimatePacket(target, ClientboundAnimatePacket.CRITICAL_HIT));
					}
				}

				if (!crit && !isSweeping) {
					if (fullStrength) {
						level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), 
								SoundEvents.PLAYER_ATTACK_STRONG, attacker.getSoundSource(), 1.0F, 1.0F);
					} else {
						level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), 
								SoundEvents.PLAYER_ATTACK_WEAK, attacker.getSoundSource(), 1.0F, 1.0F);
					}
				}

				if (enchOnlyDmgAmount > 0.0F) {
					if (serverLevel != null) {
						serverLevel.getChunkSource().broadcastAndSend(attacker, 
								new ClientboundAnimatePacket(target, ClientboundAnimatePacket.MAGIC_CRITICAL_HIT));
					}
				}

				attacker.setLastHurtMob(target);
				Entity targetMain = target;
				if (target instanceof PartEntity enderdDragonPart) {
					targetMain = enderdDragonPart.getParent();
				}

				if (targetMain instanceof LivingEntity targetLiving) {
					if (serverLevel != null && !weaponItem.isEmpty()) {
						boolean itemReducesDurabilityOnHit = false;
						ItemStack originalItem = weaponItem.copy();
						
						if (weaponItem.getItem().hurtEnemy(weaponItem, targetLiving, attacker)) {
							//attacker.awardStat(Stats.ITEM_USED.get(weaponItem.getItem()));
							itemReducesDurabilityOnHit = true;
						}

						EnchantmentHelper.doPostAttackEffects(serverLevel, target, dmgSource);
						
						if (itemReducesDurabilityOnHit) {
							weaponItem.getItem().postHurtEnemy(weaponItem, targetLiving, attacker);
						}

						if (weaponItem.isEmpty()) {
							InteractionHand hand = weaponItem == attacker.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
							if (asPlayer != null) {
								EventHooks.onPlayerDestroyItem(asPlayer, originalItem, hand);
							}
							attacker.setItemInHand(hand, ItemStack.EMPTY);
						}
					}
					
					float healthDiff = prevHealth - targetLiving.getHealth();
					//attacker.awardStat(Stats.DAMAGE_DEALT, Math.round(healthDiff * 10.0F));
					if (healthDiff > 2.0F) {
						if (serverLevel != null) {
							int dmgParticles = (int)(healthDiff * 0.5);
							serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, 
									target.getX(), target.getY(0.5), target.getZ(), dmgParticles, 0.1, 0.0, 0.1, 0.2);
						}
					}
				}

				if (asPlayer != null) {
					asPlayer.causeFoodExhaustion(0.1F);
				}
			} else {
				level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), 
						SoundEvents.PLAYER_ATTACK_NODAMAGE, attacker.getSoundSource(), 1.0F, 1.0F);
			}
		}
		if (asPlayer != null) {
			asPlayer.resetAttackStrengthTicker();
		}
	}
	
	public static float getEnchantedDamage(Entity target, float damage, ItemStack weaponItem, DamageSource damageSource) {
		if (target.level() instanceof ServerLevel serverLevel) {
			damage = EnchantmentHelper.modifyDamage(serverLevel, weaponItem, target, damageSource, damage);
		}
		return damage;
	}
	
	public static float getKnockback(LivingEntity attacker, Entity target, ItemStack weaponItem, DamageSource damageSource) {
		float knockback = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
		if (attacker.level() instanceof ServerLevel serverLevel) {
			knockback = EnchantmentHelper.modifyKnockback(serverLevel, weaponItem, target, damageSource, knockback);
		}
		return knockback;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void makeMobsDropXp(LivingIncomingDamageEvent event) {
		LivingEntity target = event.getEntity();
		DamageSource dmgSource = event.getContainer().getSource();
		if (dmgSource.getEntity() instanceof PowerUserMobEntity npc) {
			target.setLastHurtByPlayer(npc.asPlayer());
		}
	}
	
}
