package com.github.standobyte.jojo.mechanics.voiceline;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.Entity;

public class VoiceLineClientSoundTracker {
	private static SoundManager soundManager = Minecraft.getInstance().getSoundManager();
	private static Int2ObjectMap<SoundInstance> playingSounds = new Int2ObjectArrayMap<>();
	
	public static void tick() {
		if (!playingSounds.isEmpty()) {
			var iter = playingSounds.int2ObjectEntrySet().iterator();
			while (iter.hasNext()) {
				SoundInstance soundInstance = iter.next().getValue();
				if (!soundManager.isActive(soundInstance)) {
					iter.remove();
				}
			}
		}
	}
	
	@Nullable
	public static SoundInstance getPlayingVoiceLineSound(Entity entity) {
		return playingSounds.get(entity.getId());
	}
	
	public static void setSound(Entity entity, SoundInstance soundInstance) {
		playingSounds.put(entity.getId(), soundInstance);
	}
	
}
