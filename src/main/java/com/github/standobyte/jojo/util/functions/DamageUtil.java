package com.github.standobyte.jojo.util.functions;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModDamageTypes;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

public class DamageUtil {
	
	public static Holder<DamageType> type(Level level, ResourceKey<DamageType> resourceKey) {
//		return level.damageSources().damageTypes.getOrThrow(resourceKey);
		return level.damageSources().damageTypes.getHolderOrThrow(resourceKey); // i ain't typin' allat
	}
	
	public static DamageSource make(Level level, ResourceKey<DamageType> damageType) { return make(level, damageType, null, null, null); }
	public static DamageSource make(Level level, ResourceKey<DamageType> damageType, Entity entity) { return make(level, damageType, entity, entity, null); }
	public static DamageSource make(Level level, ResourceKey<DamageType> damageType, Entity directEntity, Entity causingEntity) { return make(level, damageType, directEntity, causingEntity, null); }
	public static DamageSource make(Level level, ResourceKey<DamageType> damageType, Vec3 sourcePosition) { return make(level, damageType, null, null, sourcePosition); }
	
	public static DamageSource make(Level level, ResourceKey<DamageType> damageType, 
			@Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 sourcePosition) {
		Holder<DamageType> type = type(level, damageType);
		return new DamageSource(type, directEntity, causingEntity, sourcePosition);
	}

	public static boolean dealDamageAndSetOnFire(Entity entity, Predicate<Entity> hurtEntity, int fireTicks, boolean canSetStandOnFire) {
		int prevFireTicks = entity.getRemainingFireTicks();
		if (fireTicks <= 0 || fireTicks <= prevFireTicks) {
			return hurtEntity.test(entity);
		}
		
		setOnFire(entity, fireTicks, canSetStandOnFire);
		boolean dealtDamage = hurtEntity.test(entity);
		if (!dealtDamage) {
			entity.setRemainingFireTicks(prevFireTicks);
		}
		return dealtDamage;
	}

	// TODO canSetStandOnFire parameter
	public static void setOnFire(Entity entity, int fireTicks, boolean canSetStandOnFire) {
//		if (canSetStandOnFire && entity instanceof StandEntity standEntity) {
//			standEntity.setFireFromStand(fireSeconds);
//		}
//		else {
			entity.igniteForTicks(fireTicks);
//		}
	}

	/**
	 * @deprecated Turns out, there is now a vanilla damage type tag for it now (minecraft:bypasses_cooldown). 
	 * Though tags are a bit inconvenient because you have to keep them in mind when making attacks, but eh, probably better for compatibility.
	 */
	@Deprecated
	public static boolean hurtThroughInvulTicks(Entity target, DamageSource dmgSource, float dmgAmount) {
		if (target.level().isClientSide()) return false;
		
		int invulTime = target.invulnerableTime;
		target.invulnerableTime = 0;
		LivingEntity targetLiving = target instanceof LivingEntity ? (LivingEntity) target : null;
		float lastHurt = targetLiving != null ? targetLiving.lastHurt : 0;
		
		boolean dealtDamage = target.hurt(dmgSource, dmgAmount);

		target.invulnerableTime = invulTime;
		if (targetLiving != null) {
			targetLiving.lastHurt = lastHurt;
		}
		return dealtDamage;
	}

	public static boolean canHurtStands(DamageSource dmgSource) {
		return dmgSource.is(ModDamageTypes.CAN_HURT_STANDS);
	}

	public static boolean isNotFriendlyFire(LivingEntity attacker, LivingEntity target) {
		if (attacker.is(target)) {
			return false;
		}
		// they added some dogshit to this method after 1.16.5
//		if (!attacker.canAttack(target)) {
//			return false;
//		}
		if (attacker instanceof TamableAnimal tameable && tameable.isOwnedBy(target)) return false;

		PlayerTeam team1 = attacker.getTeam();
		PlayerTeam team2 = target.getTeam();
		if (team1 != null && team1.isAlliedTo(team2) && !team1.isAllowFriendlyFire()) {
			return false;
		}

		return true;
	}

	public static float addArmorPiercing(float damage, float armorPiercing, @Nullable LivingEntity armoredTarget, DamageSource dmgSource) {
		if (armoredTarget != null && armorPiercing > 0) {
			float armor = (float) armoredTarget.getArmorValue();
			if (armor > 0) {
				float toughness = (float) armoredTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
				armorPiercing = Mth.clamp(armorPiercing, 0, 1);
				float damagePierced = Mth.lerp(armorPiercing, CombatRules.getDamageAfterAbsorb(armoredTarget, damage, dmgSource, armor, toughness), damage);
				damage = inverseArmorProtectionDamage(damagePierced, armor, toughness);
			}
		}
		return damage;
	}

	public static float inverseArmorProtectionDamage(float damageAfterAbsorb, float armor, float toughness) {
		float f = armor / 25 - 1;
		float f2 = 25 * (1 + toughness / 8);
		return Mth.clamp(
				f2 * (f + (float) Math.sqrt(f * f + 2 * damageAfterAbsorb / f2)), 
				damageAfterAbsorb / (1 - armor / 125), 
				5 * damageAfterAbsorb);
	}
	
	/**
	 * Calculates the amount of damage the given entity will take, considering the armor, resistance effect, enchantments, and stuff like that.
	 * Copies vanilla calculations, and does not take the Neoforge damage event listeners into account - we are not actually dealing any damage here, so some other mods can get confused.
	 */
	public static DamageContainer damageEntityWillTake(LivingEntity player, DamageSource dmgSource, float dmgAmount, boolean bypassShield) {
		DamageContainer damage = new DamageContainer(dmgSource, dmgAmount);
		
		// Check for illegal damage amount argument
		
		if (Float.isNaN(dmgAmount) || Float.isInfinite(dmgAmount)) {
			damage.setNewDamage(Float.MAX_VALUE);
			return damage;
		}
		if (dmgAmount <= 0) {
			damage.setNewDamage(Math.max(damage.getNewDamage(), 0));
			return damage;
		}
		
		// Other conditions in LivingEntity#hurtServer

		ServerLevel level = (ServerLevel) player.level();
		if (player.isInvulnerableTo(/*level, */dmgSource) || player.isDeadOrDying()
				|| dmgSource.is(DamageTypeTags.IS_FIRE) && player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
			damage.setNewDamage(0);
			return damage;
		}
		
		// Difficulty scaling

		if (player instanceof Player actuallyPlayer) {
			damage.setNewDamage(Math.max(0, dmgSource.type().scaling().getScalingFunction().scaleDamage(
					dmgSource, actuallyPlayer, damage.getNewDamage(), level.getDifficulty())));
			if (damage.getNewDamage() == 0) return damage;
		}
		
		// Shield check

		if (!bypassShield && player.isDamageSourceBlocked(dmgSource)) {
			LivingShieldBlockEvent ev = new LivingShieldBlockEvent(player, damage, true);
			damage.setBlockedDamage(ev); // why not :P
			if (damage.getNewDamage() <= 0) {
				damage.setNewDamage(Math.max(damage.getNewDamage(), 0));
				return damage;
			}
		}
		
		// Tags

		if (dmgSource.is(DamageTypeTags.IS_FREEZING) && player.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
			damage.setNewDamage(damage.getNewDamage() * 5);
		}
		if (dmgSource.is(DamageTypeTags.DAMAGES_HELMET) && !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
			damage.setNewDamage(damage.getNewDamage() * 0.75f);
		}
		
		// I-frames

		if (player.invulnerableTime > 10 && !dmgSource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
			if (damage.getNewDamage() <= player.lastHurt) {
				damage.setNewDamage(0);
				return damage;
			}

			damage.setReduction(DamageContainer.Reduction.INVULNERABILITY, player.lastHurt);
		}
		
		// Armor
		
		if (!dmgSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
			float damageWithArmor = CombatRules.getDamageAfterAbsorb(player, 
					damage.getNewDamage(), dmgSource, 
					(float) player.getArmorValue(), (float) player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
			damage.setReduction(DamageContainer.Reduction.ARMOR, 
					damage.getNewDamage() - damageWithArmor);
		}
		
		if (!dmgSource.is(DamageTypeTags.BYPASSES_EFFECTS)) {
			
			// Resistance effect
			
			if (player.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !dmgSource.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
				int resistanceLvl = player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier();
				float damageWithResistance = Math.max(damage.getNewDamage() * (25 - (resistanceLvl + 1) * 5) / 25F, 0);
				if (damageWithResistance < damage.getNewDamage()) {
					damage.setReduction(DamageContainer.Reduction.MOB_EFFECTS, 
							damage.getNewDamage() - damageWithResistance);
				}
			}

			// Armor enchantments

			if (damage.getNewDamage() >= 0 && !dmgSource.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
				float protReduction = EnchantmentHelper.getDamageProtection(level, player, dmgSource);
				if (protReduction > 0) {
					damage.setReduction(DamageContainer.Reduction.ENCHANTMENTS, 
							damage.getNewDamage() - CombatRules.getDamageAfterMagicAbsorb(damage.getNewDamage(), protReduction));
				}
			}
		}

		damage.setNewDamage(Math.max(damage.getNewDamage(), 0));
		return damage;
	}

//	public static void disableShield(PlayerEntity target, float chance) {
//		if (!target.level.isClientSide() && target.getRandom().nextFloat() < chance) {
//			target.getCooldowns().addCooldown(target.getUseItem().getItem(), 100);
//			target.stopUsingItem();
//			target.level.broadcastEntityEvent(target, (byte) 30);
//		}
//	}
//
//	public static boolean isMeleeAttack(DamageSource dmgSource) {
//		return getMeleeAttacker(dmgSource) != null;
//	}
//
//	@Nullable
//	public static LivingEntity getMeleeAttacker(DamageSource dmgSource) {
//		if (dmgSource.getEntity() != null && dmgSource.getDirectEntity() != null
//				&& dmgSource.getEntity().is(dmgSource.getDirectEntity()) && dmgSource.getEntity() instanceof LivingEntity) {
//			return (LivingEntity) dmgSource.getEntity();
//		}
//		return null;
//	}
//
//	public static void knockback(LivingEntity target, float strength, float yRotDeg) {
//		target.knockback(strength, 
//				(double) Mth.sin(yRotDeg * MathUtil.DEG_TO_RAD), 
//				(double) (-Mth.cos(yRotDeg * MathUtil.DEG_TO_RAD)));
//	}
//
//	public static void upwardsKnockback(LivingEntity target, float strength) {
//		strength *= (1.0F - (float) target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
//		if (strength != 0) {
//			target.setDeltaMovement(target.getDeltaMovement().add(0, strength, 0));
//		}
//
//		if (target instanceof StandEntity) {
//			LivingEntity standUser = ((StandEntity) target).getUser();
//			if (standUser != null && !standUser.is(target)) {
//				upwardsKnockback(standUser, strength);
//			}
//		}
//	}
//
//	public static void knockback3d(LivingEntity target, float strength, float xRot, float yRot) {
//		Vec3 knockbackVec = Vec3.directionFromRotation(xRot, yRot);
//		LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(target, strength, knockbackVec.x, knockbackVec.z);
//		boolean addVertical = true;
//		if (event.isCanceled()) {
//			addVertical = target.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.didStackKnockbackInstead).orElse(false);
//		}
//		if (!addVertical) {
//			return;
//		}
//
//		strength = event.getStrength();
//		knockbackVec = new Vec3(event.getRatioX(), knockbackVec.y, event.getRatioZ()).normalize();
//		strength *= (1.0F - (float) target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
//
//		if (strength != 0) {
//			target.setDeltaMovement(target.getDeltaMovement().add(knockbackVec.scale(strength)));
//		}
//
//		if (target instanceof StandEntity) {
//			LivingEntity standUser = ((StandEntity) target).getUser();
//			if (standUser != null && !standUser.is(target)) {
//				upwardsKnockback(standUser, (float) knockbackVec.y * strength);
//			}
//		}
//	}
//
//	public static void applyKnockbackStack(LivingEntity target, float pStrength, double pRatioX, double pRatioZ) {
//		pStrength *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
//		if (pStrength > 0) {
//			target.hasImpulse = true;
//			Vec3 speedCur = target.getDeltaMovement();
//			Vec3 knockback = (new Vec3(pRatioX, 0.0D, pRatioZ)).normalize().scale(pStrength);
//			target.setDeltaMovement(
//					speedCur.x - knockback.x, 
//					Math.min(0.4D, speedCur.y + (double)pStrength), 
//					speedCur.z - knockback.z);
//		}
//
//		target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
//			util.didStackKnockbackInstead = true;
//		});
//	}
//
//	public static boolean isShieldBlockAngle(LivingEntity target, DamageSource damageSource) {
//		Vec3 damagePos = damageSource.getSourcePosition();
//		if (damagePos != null) {
//			Vec3 targetViewVec = target.getViewVector(1.0F);
//			Vec3 vecToTarget = damagePos.vectorTo(target.position());
//			vecToTarget = vecToTarget.normalize();
//			vecToTarget = new Vec3(vecToTarget.x, 0, vecToTarget.z); // it's not normalized anymore though?
//			if (vecToTarget.dot(targetViewVec) < 0.0D) {
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	public static void suffocateTick(LivingEntity entity, float speed) {
//		if (entity.canBreatheUnderwater() || entity instanceof PlayerEntity && JojoModUtil.isUndeadOrVampiric((PlayerEntity) entity)
//				|| JojoModUtil.isDyingBody(entity) || entity instanceof IronGolemEntity) return;
//
//		if (entity.getAirSupply() > 0) {
//			Optional<HamonData> hamonOptional = INonStandPower.getNonStandPowerOptional(entity).resolve().flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
//			if (hamonOptional.isPresent()) {
//				HamonData hamon = hamonOptional.get();
//				speed /= 1 + hamonOptional.get().getBreathingLevel() * 0.04F;
//				hamon.suffocateTick(speed);
//			}
//
//			int airReduction = MathUtil.fractionRandomInc((double) entity.getMaxAirSupply() * Mth.clamp(speed, 0.0, 1.0)) + 4;
//			entity.setAirSupply(Math.max(entity.getAirSupply() - airReduction, -18));
//		}
//		else {
//			entity.hurt(SUFFOCATION, 1F);
//		}
//	}
//
//	public static float getDamageWithoutHeldItem(@Nullable LivingEntity entity) {
//		if (entity == null) {
//			return (float) Attributes.ATTACK_DAMAGE.getDefaultValue();
//		}
//		ItemStack heldItem = entity.getMainHandItem();
//		if (!heldItem.isEmpty()) {
//			Multimap<Attribute, AttributeModifier> itemModifiers = heldItem.getAttributeModifiers(EquipmentSlotType.MAINHAND);
//			if (itemModifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
//				ModifiableAttributeInstance attackDamageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
//				Collection<AttributeModifier> attackDamageModifiers = itemModifiers.get(Attributes.ATTACK_DAMAGE);
//
//				double damage = MCUtil.calcValueWithoutModifiers(attackDamageAttribute, 
//						attackDamageModifiers.stream().map(AttributeModifier::getId));
//				return (float) damage;
//			}
//		}
//		return (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
//	}
	
}
