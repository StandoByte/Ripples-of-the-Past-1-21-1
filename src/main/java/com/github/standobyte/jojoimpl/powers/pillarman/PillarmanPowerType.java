package com.github.standobyte.jojoimpl.powers.pillarman;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

// TODO tie to "pillar_man" CharacterSpecies
public class PillarmanPowerType extends PlayerPowerType<PillarmanData> {

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> PILLAR_MAN_ABSORPTION = ABILITY_TYPES.register(
			"pillar_man_absorption", key -> new AbilityType<>(key, Ability::new));

	public static final DeferredHolder<PlayerPowerType<?>, PillarmanPowerType> PILLAR_MAN = PLAYER_POWERS.register(
			"pillar_man", key -> new PillarmanPowerType(key, new MovesetBuilder()
					.addAbility("absorb", PILLAR_MAN_ABSORPTION)));

	
	protected PillarmanPowerType(ResourceLocation registryKey, MovesetBuilder abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public PillarmanData newDataInstance() {
		return new PillarmanData();
	}

}
