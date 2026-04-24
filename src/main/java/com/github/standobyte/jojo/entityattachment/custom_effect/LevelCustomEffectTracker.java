package com.github.standobyte.jojo.entityattachment.custom_effect;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class LevelCustomEffectTracker {
	protected final Level level;
	protected Map<EntityCustomEffectType<?>, Int2ObjectMap<EntityCustomEffect>> trackedEffects = new HashMap<>();

	public LevelCustomEffectTracker(Level level) {
		this.level = level;
	}
	
	public void add(EntityCustomEffect effect) {
		if (!effect.isStopped()) {
			trackedEffects
			.computeIfAbsent(effect.effectType, __ -> new Int2ObjectArrayMap<>())
			.put(effect.getId(), effect);
		}
	}
	
	public void tickPost() {
		for (var effectsOfType : trackedEffects.values()) {
			var iter = effectsOfType.int2ObjectEntrySet().iterator();
			while (iter.hasNext()) {
				var effectEntry = iter.next();
				if (effectEntry.getValue().isStopped()) {
					iter.remove();
				}
			}
		}
	}
	
	public <T extends EntityCustomEffect> Collection<T> getEffectsOfType(EntityCustomEffectType<T> type) {
		var effectsOfType = trackedEffects.get(type);
		if (effectsOfType == null) return Collections.emptyList();
		return (ObjectCollection<T>) effectsOfType.values();
	}
	
	@Nullable
	public static LevelCustomEffectTracker get(Level level) {
		return ComponentUtil.getExistingDataOrNull(level, ModDataAttachmentTypes.CUSTOM_EFFECTS_ON_LEVEL);
	}
	
	
	@SubscribeEvent
	public static void onTick(LevelTickEvent.Post event) {
		Level level = event.getLevel();
		LevelCustomEffectTracker tracker = LevelCustomEffectTracker.get(level);
		if (tracker != null) {
			tracker.tickPost();
		}
	}
	
}
