package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.mechanics.blinding.BlindingEffect;
import com.github.standobyte.jojo.mechanics.standmeteorite.StandVirusActualEffect;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityCustomEffects {
	public static final DeferredRegister<EntityCustomEffectType<?>> CUSTOM_EFFECTS = DeferredRegister.create(JojoRegistries.ENTITY_CUSTOM_EFFECTS_REG, JojoMod.MOD_ID);


	public static final DeferredHolder<EntityCustomEffectType<?>, EntityCustomEffectType<StandVirusActualEffect>> STAND_VIRUS = CUSTOM_EFFECTS.register(
			"stand_virus", key -> new EntityCustomEffectType<>(key, StandVirusActualEffect::new));

	public static final DeferredHolder<EntityCustomEffectType<?>, EntityCustomEffectType<BlindingEffect>> BLINDING_BLOOD_OR_SAND = CUSTOM_EFFECTS.register(
			"blinded", key -> new EntityCustomEffectType<>(key, BlindingEffect::new));
}
