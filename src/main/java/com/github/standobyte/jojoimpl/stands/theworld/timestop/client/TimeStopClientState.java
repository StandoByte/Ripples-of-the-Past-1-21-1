package com.github.standobyte.jojoimpl.stands.theworld.timestop.client;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.level.TimeStopClientLevelTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class TimeStopClientState {
	public static float partialTick = 1;
	public static boolean isTimeStopped;
	public static boolean isCameraEntityFrozen;
	public static boolean canSeeInStoppedTime;
	public static TimeStopClientLevelTracker levelTimeStops = new TimeStopClientLevelTracker();
	
	public static boolean isEntityFrozen(Entity entity) {
		return TimeStopEffect.getIsFrozenInTime(entity);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void tick(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		Entity entity = mc.cameraEntity;
		if (entity == null) entity = mc.player;
		isTimeStopped = entity != null && TimeStopEffect.getIsInsideTimeStop(entity);
		isCameraEntityFrozen = isTimeStopped && TimeStopEffect.getIsFrozenInTime(entity);
		canSeeInStoppedTime = isTimeStopped && TimeStopEffect.getCanSeeInTimeStopVar(entity);
		
		if (levelTimeStops.dimension != null) {
			ResourceKey<Level> curDimension = mc.level != null ? mc.level.dimension() : null;
			if (curDimension == null || curDimension != levelTimeStops.dimension) {
				levelTimeStops.clear();
			}
		}
	}
	
	// XXX go through the list of vanilla particles to black list
	// https://minecraft.wiki/w/Particles#Types_of_particles
	public static boolean addParticleInStoppedTime(Particle particle) {
		Class<?> particleClass = particle.getClass();
		return particleClass == TerrainParticle.class;
	}
	
}
