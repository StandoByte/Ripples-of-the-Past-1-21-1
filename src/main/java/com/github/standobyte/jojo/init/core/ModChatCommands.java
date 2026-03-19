package com.github.standobyte.jojo.init.core;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.command.JojoPowerCommand;
import com.github.standobyte.jojo.core.command.PlayBgmCommand;
import com.github.standobyte.jojo.core.command.ResolveCommand;
import com.github.standobyte.jojo.core.command.StandCommand;
import com.github.standobyte.jojo.core.command.StandExpCommand;
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
		
		StandCommand.register(dispatcher, context);		// "stand"
		StandExpCommand.register(dispatcher, context);	// "stand_exp"
		ResolveCommand.register(dispatcher, context);	// "resolve"
		JojoPowerCommand.register(dispatcher, context);	// "power"
		PlayBgmCommand.register(dispatcher, context);	// "bgm"

		// "stand_skills unlock/unlock_all/reset"
		// "stand_disc give/random"
		// "power_hamon stat set/add/query"
		// "power_vampire energy"
		// "power_pillarman energy/mode/stage"
		// "rock_paper_scissors"
		
		// "commands_list"
	}

}
