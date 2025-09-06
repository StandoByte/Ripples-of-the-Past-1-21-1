package com.github.standobyte.jojo.client.sound.util;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class EventlessSoundAccessor extends WeighedSoundEvents {
	private final Sound sound;
	private final Component subtitle;

	public EventlessSoundAccessor(ResourceLocation location, Component subtitle, Sound sound) {
		super(location, null);
		this.sound = sound;
		this.subtitle = subtitle;
	}

	@Override
	public int getWeight() {
		return sound.getWeight();
	}

	@Override
	public Sound getSound(RandomSource random) {
		return sound;
	}

	@Override
	public void addSound(Weighted<Sound> sound) {}

	@Override
	public Component getSubtitle() {
		return subtitle;
	}

	@Override
	public void preloadIfRequired(SoundEngine soundManager) {
		sound.preloadIfRequired(soundManager);
	}

}
