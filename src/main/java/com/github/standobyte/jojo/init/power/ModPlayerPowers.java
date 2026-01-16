package com.github.standobyte.jojo.init.power;

import java.util.function.Supplier;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.github.standobyte.jojoimpl.powers.hamon.HamonPowerType;
import com.github.standobyte.jojoimpl.powers.pillarman.PillarmanPowerType;
import com.github.standobyte.jojoimpl.powers.vampirism.VampirismPowerType;
import com.github.standobyte.jojoimpl.powers.zombie.ZombiePowerType;

import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPlayerPowers {
	public static final DeferredRegister<PlayerPowerType<?>> PLAYER_POWERS = DeferredRegister.create(JojoRegistries.PLAYER_POWER_TYPES_REG, JojoMod.MOD_ID);
	
	public static final Supplier<HamonPowerType> HAMON = HamonPowerType.HAMON;
	public static final Supplier<VampirismPowerType> VAMPIRISM = VampirismPowerType.VAMPIRISM;
	public static final Supplier<ZombiePowerType> ZOMBIE = ZombiePowerType.ZOMBIE;
	public static final Supplier<PillarmanPowerType> PILLAR_MAN = PillarmanPowerType.PILLAR_MAN;
}
