package com.github.standobyte.jojoimpl.stands.theworld.timestop.client;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber
public class TimeStopClientState {
	public static float partialTick = 1;
	public static boolean isTimeStopped;
	public static boolean canSeeInStoppedTime;
	
	public static boolean isEntityFrozen(Entity entity) {
		return TimeStopEffect.getIsFrozenInTime(entity);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void tick(ClientTickEvent.Pre event) {
		Player player = Minecraft.getInstance().player;
		isTimeStopped = player != null && TimeStopEffect.getIsInsideTimeStop(player);
		canSeeInStoppedTime = isTimeStopped && TimeStopEffect.getCanSeeInTimeStopVar(player);
	}
	
	public static boolean addParticleInStoppedTime(Particle particle) {
		return true;
	}
	
}
