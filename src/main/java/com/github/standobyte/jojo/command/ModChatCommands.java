package com.github.standobyte.jojo.command;

import com.github.standobyte.jojo.command.commands.JojoPowerCommand;
import com.github.standobyte.jojo.command.commands.PlayBgmCommand;
import com.github.standobyte.jojo.command.commands.ResolveCommand;
import com.github.standobyte.jojo.command.commands.StandCommand;
import com.github.standobyte.jojo.command.commands.StandExpCommand;
import com.github.standobyte.jojo.core.JojoMod;
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
