package com.github.standobyte.jojo.adventure.character;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerData;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojoimpl.powers.pillarman.PillarmanAttributeModifiers;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class CharacterSpecies {
	public static final Map<String, CharacterSpecies> SPECIES = new HashMap<>();
	
	public static final CharacterSpecies HUMAN = fromName("human");
	public static final CharacterSpecies MAYBE_ALIEN = fromName("maybe_alien");
	public static final CharacterSpecies PILLAR_MAN = fromName("pillar_man");
	public static final CharacterSpecies ROCK_HUMAN = fromName("rock_human");
	public static final CharacterSpecies PLANKTON = fromName("plankton");
	// different animal species also go here
	
	public final String name;
	public final Component nameTl;
	
	public CharacterSpecies(String name) {
		this.name = name;
		this.nameTl = Component.translatable("jojo_ripples.species." + name);
	}
	
	public Component getResultingName(LivingEntity entity, PlayerPower playerPower) {
		if (playerPower != null) {
			PowerData powerData = playerPower.getCurTypeData();
			if (powerData != null) {
				PowerType powerType = powerData.getPowerType();
				boolean overtakesSpeciesName = 
						powerType == ModPlayerPowers.VAMPIRISM.get()
						|| powerType == ModPlayerPowers.ZOMBIE.get()
						|| powerType == ModPlayerPowers.PILLAR_MAN.get();
				if (overtakesSpeciesName) {
					ResourceLocation id = powerType.getId();
					return Component.translatable("jojo_ripples.species.power_subtype." + id.getNamespace() + "." + id.getPath(), nameTl);
				}
			}
		}
		
		boolean isGhost = false;
		if (isGhost) {
			return Component.translatable("jojo_ripples.species.subtype.ghost", nameTl);
		}
		
		return nameTl;
	}
	
	
	public Tag toNBT() {
		return StringTag.valueOf(name);
	}
	
	public static CharacterSpecies fromNBT(Tag nbt) {
		String name = nbt.getAsString();
		return fromName(name);
	}
	
	public static CharacterSpecies fromName(String name) {
		CharacterSpecies v;
        return (((v = SPECIES.get(name)) != null) || SPECIES.containsKey(name))
            ? v
            : new CharacterSpecies(name);
	}
	
	
	public static CharacterSpecies replaceSpeciesIfIncompatible(CharacterSpecies nativeSpecies, 
			@Nullable PlayerPowerData playerPowerData, @Nullable StandType standType) {
//		if (standType != null && standType == ModStands.FOO_FIGHTERS.get()) {
//			return PLANKTON;
//		}
		if (playerPowerData != null) {
			PlayerPowerType<?> playerPowerType = playerPowerData.getPowerType();
			if (playerPowerType == ModPlayerPowers.PILLAR_MAN.get()) {
				return PILLAR_MAN;
			}
			if (playerPowerType != null && (nativeSpecies == PILLAR_MAN || nativeSpecies == PLANKTON)) {
				return HUMAN;
			}
		}
		return nativeSpecies;
	}
	
	public static void updateSpeciesAttributes(LivingEntity entity, 
			CharacterSpecies prevSpecies, CharacterSpecies species) {
		if (species == PILLAR_MAN) {
			for (var modifier : PillarmanAttributeModifiers.SPECIES_MODIFIERS) {
				modifier.addOrUpdateTransient(entity);
			}
		}
		else if (prevSpecies == PILLAR_MAN) {
			for (var modifier : PillarmanAttributeModifiers.SPECIES_MODIFIERS) {
				modifier.remove(entity);
			}
		}
	}
	
}
