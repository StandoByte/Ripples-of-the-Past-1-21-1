package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TimeStopEventSubscriber {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void cancelEntityTick(EntityTickEvent.Pre event) {
		Entity entity = event.getEntity();
		boolean isStoppedInTime = TimeStopEffect.getIsFrozenInTime(entity);
		if (isStoppedInTime) {
			event.setCanceled(true);
		}
	}
}
