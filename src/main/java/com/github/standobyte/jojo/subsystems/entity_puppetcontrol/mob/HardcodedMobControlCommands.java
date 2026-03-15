package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.mob;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.mob.accessors.MeleeAttackGoalInvoker;
import com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.mob.accessors.MobInvoker;
import com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.mob.accessors.RangedAttackGoalAccessor;
import com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.mob.accessors.SkeletonAccessor;
import com.github.standobyte.jojo.util.constants.EntityEvents;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// XXX (mob controller) sync all status effects of the mob to the controller player
// XXX (mob controller) make witch not drink potions instinctively on its own
// XXX (mob controller) make mobs not avoid liquids instinctively
// XXX (mob controller) ...there's a lot of other stuff
public class HardcodedMobControlCommands {
	
	public enum CommandType {
		PRESS_LMB,
		RELEASE_LMB,
		PRESS_RMB,
		RELEASE_RMB,
		
		EMPTY_MAIN_HAND,
		SWAP_ITEMS,
		TOSS,
		PICK_SLOT,
	}
	
	
	/**
	 * Ticks the parts of the mob AI that are still supposed to tick even when the mob is being controlled by a player.
	 * I.e. piglin zombification, mob attack cooldowns, etc.
	 */
	public static void serverTickControlledMob(Mob mob, boolean isHoldingRMB) {
		switch (mob) {
			case AbstractPiglin piglin -> {
				// conversion to zombified version
				// which DOESN'T MAKE FUCKING SENSE to be a part of mob AI
				((MobInvoker) piglin).callCustomServerAiStep();
			}
			case Hoglin hoglin -> {
				// same
				((MobInvoker) hoglin).callCustomServerAiStep();
			}
			default -> {}
		}
		Set<WrappedGoal> availableAIGoals = mob.goalSelector.getAvailableGoals();
		for (WrappedGoal wrappedGoal : availableAIGoals) {
			Goal goal = wrappedGoal.getGoal();
			switch (goal) {
				case MeleeAttackGoal meleeAttack -> {
					MeleeAttackGoalInvoker despiteAllMyRage = (MeleeAttackGoalInvoker) meleeAttack;
					int cooldown = despiteAllMyRage.callGetTicksUntilNextAttack();
					despiteAllMyRage.setTicksUntilNextAttack(Math.max(cooldown - 1, 0));
				}
				case RangedAttackGoal rangedAttack -> {
					RangedAttackGoalAccessor imStillJustARatInACage = (RangedAttackGoalAccessor) rangedAttack;
					int cooldown = imStillJustARatInACage.getAttackTime();
					imStillJustARatInACage.setAttackTime(Math.max(cooldown - 1, 0));
				}
				default -> {}
			}
		}
		
		if (isHoldingRMB) {
			switch (mob) {
				case Witch witch -> {
					RangedAttackGoal rangedAttack = getMobAIGoal(mob, RangedAttackGoal.class);
					if (rangedAttack != null) {
						RangedAttackGoalAccessor CYKABLYAT = (RangedAttackGoalAccessor) rangedAttack;
						int cooldown = CYKABLYAT.getAttackTime();
						if (cooldown <= 0) {
							ItemStack heldItem = mob.getMainHandItem();
							if (!heldItem.isEmpty() && heldItem.getItem() == Items.SPLASH_POTION) {
								ThrownPotion thrownPotion = new ThrownPotion(mob.level(), mob);
								Vec3 lookVec = mob.getLookAngle();
								thrownPotion.setItem(heldItem.copy());
								thrownPotion.setXRot(thrownPotion.getXRot() - -20.0F);
								thrownPotion.shoot(lookVec.x, lookVec.y, lookVec.z, 0.75F, 8.0F);
								if (!mob.isSilent()) {
									mob.level()
									.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.WITCH_THROW, 
											mob.getSoundSource(), 1.0F, 0.8F + mob.getRandom().nextFloat() * 0.4F);
								}

								mob.level().addFreshEntity(thrownPotion);
								setAvgRangedAttackCD(CYKABLYAT);
							}
						}
					}
				}
				case SnowGolem snowGolem -> {
					RangedAttackGoal rangedAttack = getMobAIGoal(mob, RangedAttackGoal.class);
					if (rangedAttack != null) {
						RangedAttackGoalAccessor CYKABLYAT = (RangedAttackGoalAccessor) rangedAttack;
						int cooldown = CYKABLYAT.getAttackTime();
						if (cooldown <= 0) {
							Snowball snowball = new Snowball(mob.level(), mob);
							Vec3 lookVec = mob.getLookAngle();
							snowball.shoot(lookVec.x, lookVec.y, lookVec.z, 1.6F, 12.0F);
							mob.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
							mob.level().addFreshEntity(snowball);
							setAvgRangedAttackCD(CYKABLYAT);
						}
					}
				}
				default -> {}
			}
		}
	}

	public static void setAvgRangedAttackCD(RangedAttackGoalAccessor goal) { goal.setAttackTime((goal.getAttackIntervalMin() + goal.getAttackIntervalMax()) / 2); }
	
	public static Map<ResourceLocation, SoundEvent> ATTACK_SOUNDS = Util.make(new HashMap<>(), map -> {
		map.put(ResourceLocation.withDefaultNamespace("iron_golem"), SoundEvents.IRON_GOLEM_ATTACK);
		map.put(ResourceLocation.withDefaultNamespace("hoglin"), SoundEvents.HOGLIN_ATTACK);
		map.put(ResourceLocation.withDefaultNamespace("ravager"), SoundEvents.RAVAGER_ATTACK);
		map.put(ResourceLocation.withDefaultNamespace("zoglin"), SoundEvents.ZOGLIN_ATTACK);
	});
	
	public static void onHotbarPacket(Mob mob, CommandType commandType, int slot, HitResult target) {
		switch (commandType) {
			case PRESS_LMB -> {
				MeleeAttackGoal meleeAttack = getMobAIGoal(mob, MeleeAttackGoal.class);
				if (meleeAttack != null) {
					Entity targetEntity = target.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) target).getEntity() : null;
					LivingEntity targetLiving = targetEntity instanceof LivingEntity __ ? __ : null;
					MeleeAttackGoalInvoker despiteAllMyRage = (MeleeAttackGoalInvoker) meleeAttack;
					boolean canAttack = targetLiving != null && despiteAllMyRage.callCanPerformAttack(targetLiving);
					int cooldown = despiteAllMyRage.callGetTicksUntilNextAttack();
					if (canAttack) {
						// yes, it will do the check twice, but fox and polar bear override this method completely for some dumbass fucking reason
						// god i hate this game's source code
						despiteAllMyRage.callCheckAndPerformAttack(targetLiving);
					}
					else if (cooldown <= 0) {
						SoundEvent attackSound = ATTACK_SOUNDS.get(EntityType.getKey(mob.getType()));
						if (attackSound != null) {
							mob.makeSound(attackSound);
						}
						switch (mob) {
							case PolarBear polarBear -> {
								if (targetLiving != null && mob.distanceToSqr(targetLiving) < (double)((targetLiving.getBbWidth() + 3.0F) * (targetLiving.getBbWidth() + 3.0F))) {
									if (despiteAllMyRage.callIsTimeToAttack()) {
										polarBear.setStanding(false);
									}
									if (cooldown <= 10) {
										polarBear.setStanding(true);
									}
								} else {
									polarBear.setStanding(false);
								}
							}
							case Fox fox -> {}
							default -> {
								mob.swing(InteractionHand.MAIN_HAND);
							}
						}
						
						despiteAllMyRage.callResetAttackCooldown();
						mob.level().broadcastEntityEvent(mob, EntityEvents.MOB_ATTACK_ANIMATION);
					}
				}
			}
			case RELEASE_LMB -> {
				
			}
			case PRESS_RMB -> {
				for (InteractionHand hand : InteractionHand.values()) {
					ItemStack item = mob.getItemInHand(hand);
					if (!item.isEmpty()) {
						// XXX (mob controller) make witch not drink splash potions
						mob.startUsingItem(hand);
						if (mob.isUsingItem()) {
							break;
						}
					}
				}
				switch (mob) {
					case AbstractSkeleton skeleton -> {
						if (mob.isUsingItem()) {
							skeleton.setAggressive(true);
						}
					}
					default -> {}
				}
			}
			case RELEASE_RMB -> {
				switch (mob) {
					case AbstractSkeleton skeleton -> {
						// copypasted AbstractSkeleton#performRangedAttack(LivingEntity target, float velocity), but using the look vector
						// instead of a specific LivingEntity target because there is none
						for (InteractionHand hand : InteractionHand.values()) {
							ItemStack weapon = skeleton.getItemInHand(hand);
							if (!weapon.isEmpty() && weapon.getItem() instanceof BowItem) {
								int ticksBowUsed = skeleton.getTicksUsingItem();
								if (ticksBowUsed >= 20) {
									float velocity = BowItem.getPowerForTime(ticksBowUsed);
									ItemStack arrowItem = skeleton.getProjectile(weapon);
									AbstractArrow arrowEntity = ((SkeletonAccessor) skeleton).callGetArrow(arrowItem, velocity, weapon);
									if (weapon.getItem() instanceof ProjectileWeaponItem weaponItem) {
										arrowEntity = weaponItem.customArrow(arrowEntity, arrowItem, weapon);
									}
									Vec3 lookVec = skeleton.getLookAngle();
									arrowEntity.shoot(lookVec.x, lookVec.y, lookVec.z, 1.6F, (float)(14 - skeleton.level().getDifficulty().getId() * 4));
									skeleton.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (skeleton.getRandom().nextFloat() * 0.4F + 0.8F));
									skeleton.level().addFreshEntity(arrowEntity);
								}

								break;
							}
						}

						mob.stopUsingItem();
						skeleton.setAggressive(false);
					}
					default -> {
						if (mob.isUsingItem()) {
							mob.stopUsingItem();
						}
					}
				}
			}
			
			/* VERY careful with this one, this MUST NOT remove any player-made items that the mob might have picked up
			 * (either on its own or when controlled by a player) to not potentially enable griefing
			 */
			case EMPTY_MAIN_HAND -> {
				if (mob instanceof Witch) {
					clearHeldItem(mob, heldItem -> {
						Item item = heldItem.getItem();
						return item == Items.POTION || item == Items.SPLASH_POTION;
					});
				}
			}
			case SWAP_ITEMS -> {
				
			}
			case TOSS -> {
				
			}
			case PICK_SLOT -> {
				if (mob instanceof Witch) {
					ItemStack potionItem = getWitchPotionItem(slot);
					if (!potionItem.isEmpty()) {
						mob.setItemInHand(InteractionHand.MAIN_HAND, potionItem.copy());
					}
					else {
						clearHeldItem(mob, heldItem -> {
							Item item = heldItem.getItem();
							return item == Items.POTION || item == Items.SPLASH_POTION;
						});
					}
				}
			}
		}
	}
	
	protected static void clearHeldItem(LivingEntity entity, Predicate<ItemStack> condition) {
		ItemStack heldItem = entity.getMainHandItem();
		if (!heldItem.isEmpty() && condition.test(heldItem)) {
			entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends Goal> T getMobAIGoal(Mob mob, Class<T> goalClass) {
		Set<WrappedGoal> availableAIGoals = mob.goalSelector.getAvailableGoals();
		for (WrappedGoal wrappedGoal : availableAIGoals) {
			Goal goal = wrappedGoal.getGoal();
			if (goalClass.isAssignableFrom(goal.getClass())) {
				return (T) goal;
			}
		}
		return null;
	}


	public static enum WitchPotionMode { DRINK, SPLASH }
	public static ItemStack[] WITCH_DRINK_POTIONS = new ItemStack[] {
			PotionContents.createItemStack(Items.POTION, Potions.HEALING),
			PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE),
			PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS),
			PotionContents.createItemStack(Items.POTION, Potions.WATER_BREATHING),
	};
	public static ItemStack[] WITCH_SPLASH_POTIONS = new ItemStack[] {
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HARMING),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HEALING),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.REGENERATION),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SLOWNESS),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON),
			PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WEAKNESS),
	};
	
	@Nullable
	public static ItemStack[] getWitchPotions(@Nullable WitchPotionMode mode) {
		if (mode == null) return null;
		return switch (mode) {
			case SPLASH -> WITCH_SPLASH_POTIONS;
			case DRINK -> WITCH_DRINK_POTIONS;
		};
	}
	
	public static int getWitchPotionSlotNumber(WitchPotionMode mode, int hotbarSlot) {
		return mode == WitchPotionMode.SPLASH ? hotbarSlot : hotbarSlot + 9;
	}
	
	public static ItemStack getWitchPotionItem(int slotNumber) {
		ItemStack[] array;
		if (slotNumber < 9) {
			array = WITCH_SPLASH_POTIONS;
		}
		else {
			array = WITCH_DRINK_POTIONS;
			slotNumber -= 9;
		}
		if (slotNumber >= 0 && slotNumber < array.length) {
			return array[slotNumber];
		}
		return ItemStack.EMPTY;
	}
	
	
	public static interface KeepRMBState {
		boolean jojo_ripples$isHoldingRMB();
		void jojo_ripples$setIsHoldingRMB(boolean isHoldingRMB);
	}
	
}
