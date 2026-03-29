package com.github.standobyte.jojo.powersystem.standpower;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.resolve.ResolveCounter;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;
import com.github.standobyte.jojo.util.functions.IsEntityBoss;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class GainStandExpOnEnemyDefeat {

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		LivingEntity target = event.getEntity();
		Level level = target.level();
		if (!level.isClientSide()) {
			DamageSource dmgSource = event.getSource();
			Entity attacker = dmgSource.getEntity();
			if (attacker instanceof StandEntity standEntity && ResolveCounter.attackingTargetGivesResolve(target)) {
				StandPower userPower = standEntity.getUserPower();
				if (userPower != null) {
					LivingEntity user = userPower.getUser();
					StandTypePersistentData standData = userPower.getCurTypeData();
					
					// exp based on the target's max hp
					float exp = target.getMaxHealth() / 20;
					
					// more exp from boss mobs
					if (target instanceof Mob mob && IsEntityBoss.check(mob)) {
						exp *= 20;
						if (target.getType() == EntityType.ENDER_DRAGON
								&& !((ServerLevel) level).getServer().getWorldData().endDragonFightData().previouslyKilled()) {
							// 600 exp from ender dragon the first time it's defeated
							exp *= 3;
						}
					}
					
					// multiplier from unique characters
					else if (target.getType() == EntityType.PLAYER && standData.defeatedCharacters.add(target.getUUID())) {
						exp *= 5;
					}

					// multiplier from unique stands
					StandPower targetStand = StandPower.get(target);
					if (targetStand != null && targetStand.hasPower()) {
						ResourceLocation targetStandType = targetStand.getPowerType().getId();
						if (standData.defeatedStands.add(targetStandType)) {
							exp *= 20;
						}
					}

					// multiplier from characters with powers
					PlayerPower targetPower = PlayerPower.get(target);
					if (targetPower != null && targetPower.hasPower()) {
						exp *= 10;
					}
					
					standData.addExp(exp, user);
				}
			}
		}
	}
}
