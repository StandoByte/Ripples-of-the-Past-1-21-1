package com.github.standobyte.jojo.jojoimpl.hamon;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.jojoimpl.hamon.abilities.HamonOverdriveBeatAbility;
import com.github.standobyte.jojo.jojoimpl.hamon.abilities.HamonRebuffOverdriveAbility;
import com.github.standobyte.jojo.jojoimpl.hamon.abilities.HamonSunlightYellowOverdriveAbility;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HamonPowerType extends PlayerPowerType<HamonData> {

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> HAMON_BEAT = ABILITY_TYPES.register(
			"hamon_beat", key -> new AbilityType<>(key, HamonOverdriveBeatAbility::new));

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> SUNLIGHT_YELLOW_OVERDRIVE = ABILITY_TYPES.register(
			"sunlight_yellow_overdrive", key -> new AbilityType<>(key, HamonSunlightYellowOverdriveAbility::new));

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> REBUFF_OVERDRIVE = ABILITY_TYPES.register(
			"rebuff_overdrive", key -> new AbilityType<>(key, HamonRebuffOverdriveAbility::new));

	public static final DeferredHolder<PlayerPowerType<?>, HamonPowerType> HAMON = PLAYER_POWERS.register(
			"hamon", key -> new HamonPowerType(key, new MovesetBuilder()
					
					.addAbility("hamon_beat", HAMON_BEAT)
					.addAbility("sunlight_yellow_overdrive", SUNLIGHT_YELLOW_OVERDRIVE)
					.addAbility("rebuff_overdrive", REBUFF_OVERDRIVE)
					
					.makeControlScheme("default")
						.makeMovesetGroup("moveset_group.hamon.combat", new InputUseVanillaMapping("jojo_ripples.key.non_stand_mode"))
							.bind("hamon_beat", InputMethod.CLICK, InputKey.LMB)
							.bind("sunlight_yellow_overdrive", InputMethod.HOLD, InputKey.LMB)
							.bind("rebuff_overdrive", InputMethod.CLICK, InputKey.RMB)
					.finalizeControlScheme()
					));

	
	protected HamonPowerType(ResourceLocation registryKey, MovesetBuilder abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public HamonData newDataInstance() {
		return new HamonData();
	}

}
