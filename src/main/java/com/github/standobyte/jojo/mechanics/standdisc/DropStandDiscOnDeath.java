package com.github.standobyte.jojo.mechanics.standdisc;

import java.util.Optional;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class DropStandDiscOnDeath {

	@SubscribeEvent
	public static void addDrops(LivingDropsEvent event) {
		if (JojoMod.config.getCommon().dropStandAsDisc.getAsBoolean()) {
			LivingEntity entity = event.getEntity();
			StandPower standPower = StandPower.get(entity);
			if (standPower != null && standPower.hasPower()) {
				StandInstance standInstance = standPower.getStandInstance().get();
				standPower.setStandInstance(Optional.empty());
				ItemStack standDiscItem = StandDiscItem.withStand(standInstance);
				ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), standDiscItem);
				itemEntity.setDefaultPickUpDelay();
				event.getDrops().add(itemEntity);
			}
		}
	}
}
