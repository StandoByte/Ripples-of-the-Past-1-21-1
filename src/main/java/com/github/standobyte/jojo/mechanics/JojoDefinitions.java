package com.github.standobyte.jojo.mechanics;

import com.github.standobyte.jojo.JojoModLivingVariables;
import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;

public class JojoDefinitions {

	public static boolean isUndeadOrVampiric(LivingEntity entity) {
		if (entity.getType().is(EntityTypeTags.UNDEAD)) {
			return true;
		}
		if (entity instanceof Player player) {
			return isPlayerJojoVampiric(player);
		}
		return false;
	}

//	/** 
//	 * You don't have to call this, it's just a condition to change the PlayerEntity's getMobType() to CreatureAttribute.UNDEAD via a mixin
//	 */
//	public static boolean playerUndeadAttribute(LivingEntity player) {
//		return INonStandPower.getNonStandPowerOptional(player).map(power -> {
//			NonStandPowerType<?> powerType = power.getType();
//			return powerType == ModPowers.VAMPIRISM.get() || powerType == ModPowers.ZOMBIE.get();
//		}).orElse(false);
//	}

	/**
	 * Is treated differently from the conventional vanilla "undead"
	 */
	public static boolean isPlayerJojoVampiric(Player player) {
		PlayerPower power = PlayerPower.get(player);
		if (power != null) {
			PlayerPowerType<?> powerType = power.getPowerType();
			return powerType == ModPlayerPowers.VAMPIRISM.get() || powerType == ModPlayerPowers.PILLAR_MAN.get();
		}
		return false;
	}

//	public static boolean isAffectedByHamon(LivingEntity entity) {
//		if (ModTags.NO_HAMON_DAMAGE.contains(entity.getType())) {
//			return false;
//		}
//		if (ModTags.HAMON_DAMAGE.contains(entity.getType())) {
//			return true;
//		}
//		return JojoModUtil.isUndeadOrVampiric(entity) || OptionalDependencyHelper.vampirism().isEntityVampire(entity);
//	}

	public static boolean canBleed(LivingEntity entity) {
		if (entity.getType().is(EntityTypeTags.UNDEAD)) {
			return entity instanceof Player
					|| entity instanceof PowerUserMobEntity
					|| entity instanceof Zombie && !(entity instanceof Husk)
					|| entity instanceof Zoglin
					|| entity instanceof ZombieHorse;
		}
		if (JojoModLivingVariables.get(entity).isDyingBody) {
			return false;
		}
		return entity instanceof Player
				|| entity instanceof PowerUserMobEntity
				|| entity instanceof AgeableMob
				|| entity instanceof Npc
				|| entity instanceof AbstractIllager
				|| entity instanceof WaterAnimal;
	}
	
	public static boolean isDyingBody(LivingEntity entity) {
		return false;
	}
	
}
