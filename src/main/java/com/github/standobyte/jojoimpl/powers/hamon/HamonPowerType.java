package com.github.standobyte.jojoimpl.powers.hamon;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.github.standobyte.jojoimpl.powers.hamon.abilities.HamonBreathAbility;
import com.github.standobyte.jojoimpl.powers.hamon.abilities.HamonOverdriveAbility;
import com.github.standobyte.jojoimpl.powers.hamon.abilities.HamonRebuffOverdriveAbility;
import com.github.standobyte.jojoimpl.powers.hamon.abilities.HamonSunlightYellowOverdriveAbility;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HamonPowerType extends PlayerPowerType<HamonData> {
	public static final int UI_COLOR = 0xFFFFFF00;

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> HAMON_BREATH = ABILITY_TYPES.register(
			"hamon_breath", key -> new AbilityType<>(key, HamonBreathAbility::new));

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> HAMON_OVERDRIVE = ABILITY_TYPES.register(
			"hamon_overdrive", key -> new AbilityType<>(key, HamonOverdriveAbility::new));

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> SUNLIGHT_YELLOW_OVERDRIVE = ABILITY_TYPES.register(
			"sunlight_yellow_overdrive", key -> new AbilityType<>(key, HamonSunlightYellowOverdriveAbility::new));

	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> REBUFF_OVERDRIVE = ABILITY_TYPES.register(
			"rebuff_overdrive", key -> new AbilityType<>(key, HamonRebuffOverdriveAbility::new));

	public static final DeferredHolder<PlayerPowerType<?>, HamonPowerType> HAMON = PLAYER_POWERS.register(
			"hamon", key -> new HamonPowerType(key, new MovesetBuilder()

					.addAbility("hamon_breath", HAMON_BREATH)
					.addAbility("hamon_overdrive", HAMON_OVERDRIVE)
					.addAbility("hamon_sunlight_yellow_overdrive", SUNLIGHT_YELLOW_OVERDRIVE)
					.addAbility("hamon_rebuff_overdrive", REBUFF_OVERDRIVE)
					
					.makeControlScheme("default")
						.makeMovesetGroup("moveset_group.hamon.combat", new InputUseVanillaMapping("jojo_ripples.key.non_stand_mode"))
							.bind("hamon_breath", InputMethod.HOLD, InputKey.MMB)
							.bind("hamon_overdrive", InputMethod.CLICK, InputKey.LMB)
							.bind("hamon_sunlight_yellow_overdrive", InputMethod.HOLD, InputKey.LMB)
							.bind("hamon_rebuff_overdrive", InputMethod.CLICK, InputKey.RMB)
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
