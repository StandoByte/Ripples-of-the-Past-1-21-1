package com.github.standobyte.jojo.init.core;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.command.JojoPowerCommand;
import com.github.standobyte.jojo.core.command.PlayBgmCommand;
import com.github.standobyte.jojo.core.command.StandCommand;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ModChatCommands {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		CommandBuildContext context = event.getBuildContext();
		
		StandCommand.register(dispatcher, context);
		JojoPowerCommand.register(dispatcher, context);
		PlayBgmCommand.register(dispatcher, context);
	}

}
