package com.github.standobyte.jojo.client.sound.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmLoopPartitioning;
import com.github.standobyte.jojo.client.sound.bgmloop.PartitionedSoundBuffers;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;

/**
 * Everything sound-related that the mod creates is cached here, 
 * to clear/close everything at once when reloading/closing resources
 */
@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class SoundCache {
	public static Map<ResourceLocation, List<Weighted<Sound>>> soundEventSeparateSounds = new HashMap<>();
	public static Map<ResourceLocation, BgmLoopPartitioning> bgmLoopMeta = new HashMap<>();
	public static PartitionedSoundBuffers partitionedSoundBuffers;
	
	/**
     * If the specified key is not already associated with a value, 
     * attempts to compute its value using the given mapping function 
     * and enters it into this map.
	 */
	@Nullable
	public static <K, V> V computeIfKeyAbsent(Map<K, V> cache, K key, Function<K, V> mappingFunction) {
		Objects.requireNonNull(mappingFunction);
		if (cache.containsKey(key)) return cache.get(key);
		V value = mappingFunction.apply(key);
		cache.put(key, value);
		return value;
	}
	
	@SubscribeEvent
	public static void onResourceReload(SoundEngineLoadEvent event) {
		soundEventSeparateSounds.clear();
		bgmLoopMeta.clear();
		if (partitionedSoundBuffers != null) partitionedSoundBuffers.clear();
	}
	
	// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! call this on Minecraft#close
	// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! do i have to make a BgmLoopPlayer#close() too?
	public static void close() {
		if (partitionedSoundBuffers != null) partitionedSoundBuffers.clear();
	}
}
