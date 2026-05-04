package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
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
		LivingEntity asLiving = entity instanceof LivingEntity living ? living : null;
		if (!entity.level().isClientSide()) {
			if (asLiving != null) {
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
		
		if (asLiving != null) {
			for (PowerClass<?> powerClass : PowerClass.values()) {
				Power<?> power = powerClass.get(asLiving);
				if (power != null) {
					power.alwaysTick();
				}
			}
		}
		
		if (entity instanceof TickEntityInTimeStop e) {
			e.tickInTimeStop();
		}
	}
	

	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
	public static class ClientEvents {
		
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void cancelMouseKeybinds(InteractionKeyMappingTriggered event) {
			Player player = Minecraft.getInstance().player;
			if (player != null && TimeStopEffect.getIsFrozenInTime(player)) {
				event.setCanceled(true);
				event.setSwingHand(false);
			}
		}
	}
	
}
