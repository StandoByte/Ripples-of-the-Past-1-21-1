package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

// TODO (time stop) arrow 5 ticks inertia
@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TimeStopEventSubscriber {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void cancelEntityTick(EntityTickEvent.Pre event) {
		Entity entity = event.getEntity();
		boolean isStoppedInTime = TimeStopEffect.getIsFrozenInTime(entity);
		if (isStoppedInTime) {
			entity.tickCount--;
			event.setCanceled(true);
			tickInTimeStopAnyway(entity);
		}
	}
	
	public static void tickInTimeStopAnyway(Entity entity) {
		if (!entity.level().isClientSide()) {
			if (entity instanceof LivingEntity living) {
				if (entity.invulnerableTime > 0) {
					entity.invulnerableTime--;
				}
			}
			else if (entity instanceof ItemEntity itemEntity) {
				if (itemEntity.pickupDelay > 0 && itemEntity.pickupDelay != 32767) {
					--itemEntity.pickupDelay;
				}
			}
		}
		if (entity instanceof TickEntityInTimeStop e) {
			e.tickInTimeStop();
		}
	}
	
}
