package com.github.standobyte.jojoimpl.stands.theworld.timestop.level;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.util.objects_java.ReuseableStream;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopInstance;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopVFXPacket.TimeStopVFXState;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopClientState;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopInstancePacket;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TimeStopLevelTracker {
	protected final ServerLevel level;
	protected Int2ObjectMap<TimeStopEffect> activeEffects = new Int2ObjectArrayMap<>();

	public TimeStopLevelTracker(ServerLevel level) {
		this.level = level;
	}
	
	public void add(TimeStopEffect effect) {
		if (!effect.isStopped()) {
			activeEffects.put(effect.getId(), effect);
			updateTimeStopped();
			PacketDistributor.sendToPlayersInDimension(level, 
					TimeStopInstancePacket.add(effect));
		}
	}
	
	public void remove(TimeStopEffect effect) {
		activeEffects.remove(effect.getId());
		updateTimeStopped();
		PacketDistributor.sendToPlayersInDimension(level, 
				TimeStopInstancePacket.remove(effect.getId()));
	}
	
	public Collection<TimeStopEffect> getEffects() {
		return activeEffects.values();
	}

	static IntCollection changed = new IntArraySet(1);
	public void tickPost() {
		changed.clear();
		var iter = activeEffects.int2ObjectEntrySet().iterator();
		while (iter.hasNext()) {
			var effectEntry = iter.next();
			TimeStopEffect effect = effectEntry.getValue();
			if (effect.isStopped()) {
				iter.remove();
				changed.add(effectEntry.getIntKey());
			}
		}
		
		if (!changed.isEmpty()) {
			updateTimeStopped();
			PacketDistributor.sendToPlayersInDimension(level, 
					TimeStopInstancePacket.removeMultiple(changed));
		}
	}
	
	protected void updateTimeStopped() {
		if (!level.isClientSide()) {
			Iterable<Entity> allEntities = level.getAllEntities();
			var tsEffects = activeEffects.values();
			if (!tsEffects.isEmpty()) {
				ReuseableStream<TimeStopEffect> timeStops = new ReuseableStream<>(tsEffects.stream());
				for (Entity entity : allEntities) {
					updateEntityState(entity, timeStops);
				}
			}
			else {
				for (Entity entity : allEntities) {
					TimeStopEffect.setTimeStopState(entity, false, false, true);
				}
			}
		}
	}
	
	protected ReuseableStream<TimeStopEffect> getInstances() {
		return new ReuseableStream<>(activeEffects.values().stream());
	}
	
	protected void updateEntityState(Entity entity, ReuseableStream<TimeStopEffect> timeStops) {
		boolean isInTimeStopRange = timeStops.getStream().anyMatch(timeStop -> timeStop.isInRange(entity.blockPosition()));
		boolean stoppedInTime = isInTimeStopRange && !TimeStopEffect.canEntityTickInStoppedTime(entity);
		boolean canSeeInStoppedTime = !stoppedInTime
				|| entity instanceof LivingEntity living && TimeStopEffect.canEntitySeeInStoppedTime(living);
		TimeStopEffect.setTimeStopState(entity, isInTimeStopRange, stoppedInTime, canSeeInStoppedTime);
	}
	
	
	@Nullable
	public static TimeStopLevelTracker get(Level level) {
		return ComponentUtil.getExistingDataOrNull(level, ModDataAttachmentTypes.TIME_STOP_LEVEL_TRACKER);
	}
	
	
	@SubscribeEvent
	public static void onTick(LevelTickEvent.Post event) {
		Level level = event.getLevel();
		if (!level.isClientSide()) {
			TimeStopLevelTracker tracker = TimeStopLevelTracker.get(level);
			if (tracker != null) {
				tracker.tickPost();
			}
		}
	}
	
	@SubscribeEvent
	public static void onAddedToLevel(EntityJoinLevelEvent event) {
		Level level = event.getLevel();
		if (!level.isClientSide()) {
			TimeStopLevelTracker tracker = TimeStopLevelTracker.get(level);
			if (tracker != null && !tracker.activeEffects.isEmpty()) {
				Entity entity = event.getEntity();
				
				tracker.updateEntityState(entity, tracker.getInstances());
				
				if (entity instanceof ServerPlayer player) {
					TimeStopEffect earliestEffect = tracker.activeEffects.values().stream()
							.filter(effect -> !effect.isStopped())
							.max(Comparator.comparingInt(effect -> effect.tickCount))
							.orElse(null);
					if (earliestEffect != null) {
						PacketDistributor.sendToPlayer(player, 
								TimeStopInstancePacket.allOnLevelJoin(level.dimension(), tracker.activeEffects.values()), 
								earliestEffect.shaderPacket(TimeStopVFXState.ACTIVE)); // NO WAY THAT'S THE FIRST TIME THE VARARGS WERE USEFUL (no shade thrown, just a bit annoying to always remove the payloads argument after autocomplete, it is nice to have them though)
					}
				}
			}
		}
	}
	
	
	public static boolean hasATimeStop(Level level) {
		return timeStops(level).iterator().hasNext();
	}
	
	public static Stream<? extends TimeStopInstance> timeStops(Level level) {
		if (!level.isClientSide()) {
			TimeStopLevelTracker serverTracker = TimeStopLevelTracker.get(level);
			return serverTracker != null ? serverTracker.activeEffects.values().stream() : Stream.empty();
		}
		else {
			TimeStopClientLevelTracker clientTracker = TimeStopClientState.levelTimeStops;
			return clientTracker.activeEffects.values().stream();
		}
	}
	
}
