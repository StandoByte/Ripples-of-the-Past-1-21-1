package com.github.standobyte.jojo.core.command;

import java.util.Collection;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;

public class ResolveCommand {
	public static final MultipleTargetsCommandResult RESET_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.resolve.reset");

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(
		Commands.literal(JojoMod.MOD_ID).then(
			Commands.literal("resolve")
				.requires(src -> src.hasPermission(2))
				
				.then(
				Commands.literal("reset")
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.executes(
						src -> resetResolve(
							src.getSource(),
							EntityArgument.getEntities(src, "targets")
							)
						)
					)
				)
			)
		);
		JojoCommandsCommand.addCommand("resolve");
	}

	private static int resetResolve(CommandSourceStack source, Collection<? extends Entity> targets) throws CommandSyntaxException {
		Collection<StandPower> stands = StandCommand.getStands(targets);
		for (StandPower stand : stands) {
			stand.resolveCounter.reset(stand.getUser());
		}

		return RESET_MSG.trySend(source, true, targets, stands.size());
	}
}
