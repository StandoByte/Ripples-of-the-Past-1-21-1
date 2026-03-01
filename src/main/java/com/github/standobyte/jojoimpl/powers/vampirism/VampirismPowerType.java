package com.github.standobyte.jojoimpl.powers.vampirism;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class VampirismPowerType extends PlayerPowerType<VampirismData> {

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> VAMPIRE_BLOOD_DRAIN = ABILITY_TYPES.register(
			"vampire_blood_drain", key -> new AbilityType<>(key, Ability::new));

	public static final DeferredHolder<PlayerPowerType<?>, VampirismPowerType> VAMPIRISM = PLAYER_POWERS.register(
			"vampirism", key -> new VampirismPowerType(key, new MovesetBuilder()
					.addAbility("blooddrain", VAMPIRE_BLOOD_DRAIN)));

	
	protected VampirismPowerType(ResourceLocation registryKey, MovesetBuilder abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public VampirismData newDataInstance() {
		return new VampirismData();
	}

}
