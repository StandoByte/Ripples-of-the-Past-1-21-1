package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.util.objects_java.ReuseableStream;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TimeStopLevelTracker {
	protected final Level level;
	protected Int2ObjectMap<TimeStopEffect> activeEffects = new Int2ObjectArrayMap<>();
	protected IntSet stoppedInTime = new IntOpenHashSet();

	public TimeStopLevelTracker(Level level) {
		this.level = level;
	}
	
	public void add(TimeStopEffect effect) {
		if (!effect.isStopped()) {
			activeEffects.put(effect.getId(), effect);
			updateTimeStopped();
		}
	}
	
	public void remove(TimeStopEffect effect) {
		activeEffects.remove(effect.getId());
		updateTimeStopped();
	}
	
	public Iterable<TimeStopEffect> getEffects() {
		return activeEffects.values();
	}

	static MutableBoolean changed = new MutableBoolean();
	public void tickPost() {
		changed.setFalse();
		var iter = activeEffects.int2ObjectEntrySet().iterator();
		while (iter.hasNext()) {
			var effectEntry = iter.next();
			if (effectEntry.getValue().isStopped()) {
				iter.remove();
				changed.setTrue();
			}
		}
		
		if (changed.booleanValue()) {
			updateTimeStopped();
		}
	}
	
	protected void updateTimeStopped() {
		if (!level.isClientSide()) {
			Iterable<Entity> allEntities = ((ServerLevel) level).getAllEntities();
			var tsEffects = activeEffects.values();
			if (!tsEffects.isEmpty()) {
				ReuseableStream<TimeStopEffect> timeStops = new ReuseableStream<>(tsEffects.stream());
				for (Entity entity : allEntities) {
					boolean stoppedInTime = 
							!TimeStopEffect.canEntityTickInStoppedTime(entity)
							&& timeStops.getStream().anyMatch(timeStop -> timeStop.isInRange(entity.blockPosition()));
					boolean canSeeInStoppedTime = !stoppedInTime
							|| entity instanceof LivingEntity living && TimeStopEffect.canEntitySeeInStoppedTime(living);
					TimeStopEffect.setTimeStopState(entity, true, stoppedInTime, canSeeInStoppedTime);
				}
			}
			else {
				for (Entity entity : allEntities) {
					TimeStopEffect.setTimeStopState(entity, false, false, true);
				}
			}
		}
	}
	
	
	@Nullable
	public static TimeStopLevelTracker get(Level level) {
		return ComponentUtil.getExistingDataOrNull(level, ModDataAttachmentTypes.TIME_STOP_LEVEL_TRACKER);
	}
	
	
	@SubscribeEvent
	public static void onTick(LevelTickEvent.Post event) {
		Level level = event.getLevel();
		TimeStopLevelTracker tracker = TimeStopLevelTracker.get(level);
		if (tracker != null) {
			tracker.tickPost();
		}
	}
}
