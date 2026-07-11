package com.github.standobyte.jojo.mechanics.voiceline;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.subsystems.StoryPart;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class VoiceLineClientSide {

	public static void play(Entity entity, Holder<SoundEvent> soundEvent, 
			Holder<StoryCharacter> character, @Nullable Holder<StoryPart> storyPart,
			@Nullable ResourceLocation standType, boolean canInterrupt, 
			SoundSource soundCategory, float volume, float pitch) {
		List<ClientVoiceLineDefinition> voiceLines = ClientVoiceLinesLoader.getInstance().getVoiceLine(
				soundEvent, character, 
				storyPart, standType).toList();
		for (ClientVoiceLineDefinition voiceLine : voiceLines) {
			for (ResourceLocation soundFile : voiceLine.sounds()) {
				JojoMod.LOGGER.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ {}", soundFile);
			}
		}
	}
}
