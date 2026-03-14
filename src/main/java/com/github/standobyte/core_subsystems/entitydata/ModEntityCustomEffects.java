package com.github.standobyte.core_subsystems.entitydata;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;

import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityCustomEffects {
	public static final DeferredRegister<EntityCustomEffectType<?>> CUSTOM_EFFECTS = DeferredRegister.create(JojoRegistries.ENTITY_CUSTOM_EFFECTS_REG, JojoMod.MOD_ID);
}
