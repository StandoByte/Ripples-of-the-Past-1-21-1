package com.github.standobyte.jojo.client.sound.util;

import javax.annotation.Nullable;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.util.Lazy;

public class EventlessSound extends AbstractSoundInstance {
	protected static RandomSource random = RandomSource.create();
	protected Component subtitle;
	protected Lazy<? extends WeighedSoundEvents> accessor = Lazy.of(
			() -> new EventlessSoundAccessor(sound.getLocation(), subtitle, sound));

	public EventlessSound(Sound sound, SoundSource source) {
		this(sound, source, null);
	}

	public EventlessSound(Sound sound, SoundSource source, @Nullable Component subtitle) {
		super((sound != null ? sound : SoundManager.EMPTY_SOUND).getLocation(), source, random);
		this.sound = sound != null ? sound : SoundManager.EMPTY_SOUND;
		this.subtitle = subtitle;
	}

	public EventlessSound(Sound sound, SoundSource source, @Nullable Component subtitle,
			float volume, float pitch, boolean looping, int delay, 
			SoundInstance.Attenuation attenuation, double x, double y, double z, boolean relative) {
		super((sound != null ? sound : SoundManager.EMPTY_SOUND).getLocation(), source, random);
		this.sound = sound != null ? sound : SoundManager.EMPTY_SOUND;
		this.subtitle = subtitle;

		this.volume = volume;
		this.pitch = pitch;
		this.x = x;
		this.y = y;
		this.z = z;
		this.looping = looping;
		this.delay = delay;
		this.attenuation = attenuation;
		this.relative = relative;
	}

	@Override
	public ResourceLocation getLocation() {
		return sound.getLocation();
	}

	@Override
	public WeighedSoundEvents resolve(SoundManager soundManager) {
		return accessor.get();
	}

}

