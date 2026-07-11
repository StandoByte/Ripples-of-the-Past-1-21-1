package com.github.standobyte.jojo.mechanics.voiceline;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.subsystems.StoryPart;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class VoiceLineServerSide {

	public static void play(LivingEntity entity, Holder<SoundEvent> soundEvent) {
		play(entity, soundEvent, 200, false);
	}

	public static void play(LivingEntity entity, Holder<SoundEvent> soundEvent, 
			int delay, boolean canInterrupt) {
		if (entity.level().isClientSide()) return;
		
		EntityClothesInventory clothes = EntityClothesInventory.getExisting(entity);
		if (clothes != null) {
			Holder<StoryCharacter> character = clothes.getCharacter();
			if (character != null) {
				@Nullable Holder<StoryPart> storyPart = clothes.getStoryPart();
				@Nullable ResourceLocation standType = StandPower.getOptional(entity)
						.map(Power::getPowerType).map(PowerType::getId).orElse(null);
				
				PlayVoiceLinePacket packet = new PlayVoiceLinePacket(entity.getId(), soundEvent,
						character, storyPart,
						standType, canInterrupt,
						SoundSource.VOICE, 1, 1);
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
			}
		}
	}
}
