package com.github.standobyte.jojo.client.standskin.sound;

import javax.annotation.Nonnull;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.SampledFloat;

public class CustomPathSound extends Sound {
	protected ResourceLocation filePath;
	
	public CustomPathSound(Sound sound, ResourceLocation filePath) {
		super(sound.getLocation(), sound.getVolume(), sound.getPitch(), sound.getWeight(), sound.getType(), sound.shouldStream(), sound.shouldPreload(), sound.getAttenuationDistance());
		this.filePath = filePath != null ? filePath : sound.getPath();
	}
	
	public CustomPathSound(
			ResourceLocation location,
	        SampledFloat volume,
	        SampledFloat pitch,
	        int weight,
	        Sound.Type type,
	        boolean stream,
	        boolean preload,
	        int attenuationDistance,
			@Nonnull ResourceLocation filePath) {
		super(location, volume, pitch, weight, type, stream, preload, attenuationDistance);
		this.filePath = filePath;
	}

	@Override
	public ResourceLocation getPath() {
		return filePath;
	}

	@Override
	public String toString() {
		return "Sound[" + this.getLocation() + "] at path: + " + this.getPath();
	}
	
}
