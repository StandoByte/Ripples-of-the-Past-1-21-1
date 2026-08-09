package com.github.standobyte.jojo.client.sound.util;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class EntityBoundEventlessSound extends EventlessSound implements TickableSoundInstance {
	public Entity entity;
	private boolean stopped;

	public EntityBoundEventlessSound(Entity entity, Sound sound) {
		super(sound, entity.getSoundSource());
		this.entity = entity;
	}

	public EntityBoundEventlessSound(Entity entity, Sound sound, SoundSource source) {
		super(sound, source);
		this.entity = entity;
	}

	public EntityBoundEventlessSound(Entity entity, Sound sound, SoundSource source, Component subtitle) {
		super(sound, source, subtitle);
		this.entity = entity;
	}

	public EntityBoundEventlessSound(Entity entity, Sound sound, SoundSource source, Component subtitle, 
			float volume, float pitch, boolean looping, int delay, 
			Attenuation attenuation, boolean relative) {
		super(sound, source, subtitle, 
				volume, pitch, looping, delay, 
				attenuation, entity.getX(), entity.getY(), entity.getZ(), relative);
		this.entity = entity;
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}

	public void stop() {
		stopped = true;
		looping = false;
	}

	@Override
	public boolean canPlaySound() {
		return !entity.isSilent();
	}

	@Override
	public void tick() {
		if (entity.isRemoved()) {
			stop();
		}
		else {
			updatePosition();
		}
	}
	
	protected void updatePosition() {
		x = entity.getX();
		y = entity.getY();
		z = entity.getZ();
	}

}
