package com.github.standobyte.jojo.client.sound.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;

/**
 * Everything sound-related that the mod creates is cached here, 
 * to clear/close everything at once when reloading/closing resources
 */
public class SoundCache {
	protected static SoundCache instance = new SoundCache();
	public static SoundCache getInstance() {
		return instance;
	}

	private Map<ResourceLocation, List<Weighted<Sound>>> soundEventSeparateSounds = new HashMap<>();
	

	/**
	 * All of the elements are guaranteed to be instances of Sound, 
	 * you can give null to {@link Weighted#getSound(RandomSource)} and it'll return the sound itself
	 */
	public List<Weighted<Sound>> getSoundFiles(SoundEvent soundEvent, SoundManager soundManager) {
		return SoundUtil.computeIfKeyAbsent(
				soundEventSeparateSounds, 
				soundEvent.getLocation(), 
				key -> SoundUtil.decomposeSounds(soundManager.getSoundEvent(key)));
	}
	
	public void onResourceReload(SoundEngineLoadEvent event) {
		soundEventSeparateSounds.clear();
	}
}
