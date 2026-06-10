package com.github.standobyte.jojo.adventure.character;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;

import net.minecraft.Util;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class CharacterSpecies {
	public static final Map<String, CharacterSpecies> SPECIES = Util.make(new HashMap<>(), map -> {
		map.computeIfAbsent("human", CharacterSpecies::new);
		map.computeIfAbsent("maybe_alien", CharacterSpecies::new);
		map.computeIfAbsent("pillar_man", CharacterSpecies::new);
		map.computeIfAbsent("rock_human", CharacterSpecies::new);
		map.computeIfAbsent("plankton", CharacterSpecies::new);
		// different animals species also go here
	});
	public static final CharacterSpecies HUMAN = fromName("human");
	
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
				boolean overtakesSpeciesName = powerType == ModPlayerPowers.VAMPIRISM.get()
						|| powerType == ModPlayerPowers.ZOMBIE.get();
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
	
}
