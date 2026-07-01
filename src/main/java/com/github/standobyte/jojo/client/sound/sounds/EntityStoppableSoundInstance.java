package com.github.standobyte.jojo.client.sound.sounds;

import java.util.function.BooleanSupplier;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

/**
 * Like {@link net.minecraft.client.resources.sounds.EntityBoundSoundInstance}, but can also stop on a specific condition
 */
public class EntityStoppableSoundInstance extends AbstractTickableSoundInstance {
	protected Entity entity;
	protected BooleanSupplier stopWhen;
	public boolean ITS_FUCKING_STOPPED_ALREADY = false;

	public EntityStoppableSoundInstance(SoundEvent soundEvent, SoundSource source, 
			float volume, float pitch, Entity entity, long seed, BooleanSupplier stopWhen) {
		this(soundEvent, source, volume, pitch, false, entity, seed, stopWhen);
	}

	public EntityStoppableSoundInstance(SoundEvent soundEvent, SoundSource source, 
			float volume, float pitch, boolean looping, Entity entity, long seed, BooleanSupplier stopWhen) {
		super(soundEvent, source, RandomSource.create(seed));
		this.volume = volume;
		this.pitch = pitch;
		this.looping = looping;
		this.entity = entity;
		this.x = entity.getX();
		this.y = entity.getY();
		this.z = entity.getZ();
		this.stopWhen = stopWhen;
	}

	@Override
	public boolean canPlaySound() {
		return !this.entity.isSilent();
	}

	@Override
	public void tick() {
		if (entity.isRemoved() || ITS_FUCKING_STOPPED_ALREADY || stopWhen.getAsBoolean()) {
			ITS_FUCKING_STOPPED_ALREADY = true;
			this.stop();
		} else {
			this.x = entity.getX();
			this.y = entity.getY();
			this.z = entity.getZ();
		}
	}

}
