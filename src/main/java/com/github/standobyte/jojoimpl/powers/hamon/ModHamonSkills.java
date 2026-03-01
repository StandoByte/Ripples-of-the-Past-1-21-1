package com.github.standobyte.jojoimpl.powers.hamon;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;

import net.neoforged.neoforge.registries.DeferredRegister;

public class ModHamonSkills {
	public static final DeferredRegister<HamonSkill> HAMON_SKILLS = DeferredRegister.create(JojoRegistries.HAMON_SKILLS_REG, JojoMod.MOD_ID);
	public static final DeferredRegister<HamonTechnique> HAMON_CHARACTER_TECHNIQUES = DeferredRegister.create(JojoRegistries.HAMON_TECHNIQUES_REG, JojoMod.MOD_ID);

}
