package com.github.standobyte.jojo.mechanics.voiceline;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.sound.util.SoundUtil;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.functions.java.ListUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class VoiceLineClientSide {

	public static void play(Entity entity, Holder<SoundEvent> soundEvent, 
			Holder<StoryCharacter> character, @Nullable Holder<StoryPart> storyPart,
			@Nullable ResourceLocation standType, boolean canInterrupt, 
			SoundSource soundCategory, float volume, float pitch) {
		List<ClientVoiceLineDefinition> voiceLines = ClientVoiceLinesLoader.getInstance().getVoiceLine(
				soundEvent, character, 
				storyPart, standType).toList();
		if (!voiceLines.isEmpty()) {
			ClientVoiceLineDefinition voiceLine = ListUtil.getRandom(voiceLines);
			Vec3 pos = entity.getEyePosition();
			play(voiceLine, entity, canInterrupt, soundCategory, volume, pitch, pos.x, pos.y, pos.z);
		}
	}
	
	public static void play(ClientVoiceLineDefinition voiceLine, 
			Entity entity, boolean canInterrupt) {
		Vec3 pos = entity.getEyePosition();
		play(voiceLine, entity, canInterrupt,
				SoundSource.VOICE, 1, 1, pos.x, pos.y, pos.z);
	}
	
	public static void play(ClientVoiceLineDefinition voiceLine, 
			Entity entity, boolean canInterrupt, 
			SoundSource soundCategory, float volume, float pitch,
			double x, double y, double z) {
		List<ResourceLocation> sounds = voiceLine.sounds();
		if (sounds.isEmpty()) return;
		
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation soundLocation = ListUtil.getRandom(sounds);
		
		SoundInstance curPlayingVoiceLine = VoiceLineClientSoundTracker.getPlayingVoiceLineSound(entity);
		if (curPlayingVoiceLine != null) {
			if (canInterrupt) {
				mc.getSoundManager().stop(curPlayingVoiceLine);
			}
			else {
				return;
			}
		}
		
		SoundInstance soundInstance = SoundUtil.justPutTheSoundInTheBag(soundLocation, voiceLine.subtitle(),
				soundCategory, volume, pitch, SoundInstance.Attenuation.LINEAR, x, y, z);
		mc.getSoundManager().play(soundInstance);
		VoiceLineClientSoundTracker.setSound(entity, soundInstance);
	}
	
}
