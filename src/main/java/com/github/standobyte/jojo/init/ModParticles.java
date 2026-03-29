package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {
	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, JojoMod.MOD_ID);


	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD = PARTICLES.register("blood", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_SPARK = PARTICLES.register("hamon_spark", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_SPARK_BLUE = PARTICLES.register("hamon_spark_blue", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_SPARK_YELLOW = PARTICLES.register("hamon_spark_yellow", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_SPARK_RED = PARTICLES.register("hamon_spark_red", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_SPARK_SILVER = PARTICLES.register("hamon_spark_silver", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_AURA = PARTICLES.register("hamon_aura", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_AURA_BLUE = PARTICLES.register("hamon_aura_blue", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_AURA_YELLOW = PARTICLES.register("hamon_aura_yellow", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_AURA_RED = PARTICLES.register("hamon_aura_red", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_AURA_SILVER = PARTICLES.register("hamon_aura_silver", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_AURA_GREEN = PARTICLES.register("hamon_aura_green", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAMON_AURA_RAINBOW = PARTICLES.register("hamon_aura_rainbow", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOILING_BLOOD_POP = PARTICLES.register("boiling_blood", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> METEORITE_VIRUS = PARTICLES.register("meteorite_virus", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MENACING = PARTICLES.register("menacing", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> KATAKANA_DO = PARTICLES.register("katakana_do", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUL_CLOUD = PARTICLES.register("soul_cloud", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AIR_STREAM = PARTICLES.register("air_stream", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CD_RESTORATION = PARTICLES.register("cd_restoration", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RPS_ROCK = PARTICLES.register("rps_rock", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RPS_PAPER = PARTICLES.register("rps_paper", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RPS_SCISSORS = PARTICLES.register("rps_scissors", () -> new SimpleParticleType(false));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SANDSTORM = PARTICLES.register("divine_sandstorm", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RIFT = PARTICLES.register("atmospheric_rift", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LIGHT_SPARK = PARTICLES.register("light_spark", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LIGHT_MODE_FLASH = PARTICLES.register("light_mode_flash", () -> new SimpleParticleType(false));

}
