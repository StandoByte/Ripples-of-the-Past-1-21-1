package com.github.standobyte.jojo.client.sound;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.sound.SoundInstanceWithStandSkin;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.customobjects.EntityWithStandSkin;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientsideSoundsHelper {

	
	/*
	 * We can actually reference our implementations of SoundInstance (for example, EntityStoppableSoundInstance) anywhere,
	 * unlike the vanilla classes (such as EntityBoundSoundInstance) that have @OnlyIn annotation and therefore are not present on dedicated server.
	 * We just can't reference the SoundInstance interface itself in this method's signature. because it is also only present on client.
	 * Of course we can still only call this on a logical client side (if Level#isClientSide() is true).
	 */
	public static void playNonVanillaClassSound(Object soundInstance) {
		Minecraft.getInstance().getSoundManager().play((SoundInstance) soundInstance);
	}
	
	public static void playSimpleSoundInstance(SoundEvent soundEvent, float volume, float pitch, 
			SoundSource soundSource, @Nullable Vec3 pos) {
		SimpleSoundInstance sound;
		if (pos != null) {
			sound = new SimpleSoundInstance(soundEvent.getLocation(),
					soundSource, pitch, volume,
					SoundInstance.createUnseededRandom(), false, 0,
					SoundInstance.Attenuation.NONE,
					pos.x, pos.y, pos.z, false);
		}
		else {
			sound = new SimpleSoundInstance(soundEvent.getLocation(),
					soundSource, pitch, volume,
					SoundInstance.createUnseededRandom(), false, 0,
					SoundInstance.Attenuation.NONE,
					0, 0, 0, true);
		}
		Minecraft.getInstance().getSoundManager().play(sound);
	}
	
	
	
	/**
	 * Call this right before calling {@link net.minecraft.client.sounds.SoundEngine#play(SoundInstance)}
	 * (or when it's abstracted behind something like ClientLevel#playLocalSound or SoundManager#play)
	 */
	public static SoundEvent withStandSkin(SoundEvent soundEvent, ResourceLocation standId, Optional<ResourceLocation> standSkin) {
		ClientsideSoundsHelper.standSkin_soundEvent = soundEvent;
		ClientsideSoundsHelper.standSkin_standId = standId;
		ClientsideSoundsHelper.standSkin_standSkin = standSkin;
		return soundEvent;
	}
	
	public static SoundEvent withStandSkin(SoundEvent soundEvent, EntityWithStandSkin standEntity) {
		return withStandSkin(soundEvent, standEntity.getStandType(), standEntity.getStandSkin());
	}
	
	public static SoundEvent withStandSkin(SoundEvent soundEvent, StandPower standPower) {
		if (standPower != null) {
			StandType standType = standPower.getPowerType();
			return withStandSkin(soundEvent, standType != null ? standType.getId() : null, standPower.getSelectedSkin());
		}
		else {
			return soundEvent;
		}
	}

	// Internal Stand skin handler section
	
	private static SoundEvent standSkin_soundEvent;
	private static ResourceLocation standSkin_standId;
	private static Optional<ResourceLocation> standSkin_standSkin;
	
	@SubscribeEvent
	public static void onSoundPlayed(PlaySoundEvent event) {
		if (standSkin_soundEvent != null) {
			SoundInstance sound = event.getSound();
			if (sound != null && standSkin_soundEvent.getLocation().equals(sound.getLocation())) {
				if (sound instanceof SoundInstanceWithStandSkin withSkin) {
					withSkin.jojo_ripples$setStandSkin(standSkin_standId, standSkin_standSkin);
				}
				standSkin_soundEvent = null;
				standSkin_standId = null;
				standSkin_standSkin = null;
			}
		}
	}

	@SubscribeEvent
	public static void resetJustInCase(ClientTickEvent.Post event) {
		if (standSkin_soundEvent != null) {
			standSkin_soundEvent = null;
			standSkin_standId = null;
			standSkin_standSkin = null;
		}
	}
}
