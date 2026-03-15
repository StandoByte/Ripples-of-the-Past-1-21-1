package com.github.standobyte.jojo.command.argument;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCommandArguments {
	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, JojoMod.MOD_ID);
	
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ?> STAND_ARG = ARGUMENT_TYPES.register("stand", 
			() -> ArgumentTypeInfos.registerByClass(StandArgument.class, SingletonArgumentInfo.contextAware(StandArgument::new)));

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ?> PLAYER_POWER_ARG = ARGUMENT_TYPES.register("player_power",
            () -> ArgumentTypeInfos.registerByClass(PlayerPowerTypeArgument.class, SingletonArgumentInfo.contextAware(PlayerPowerTypeArgument::new)));
}
