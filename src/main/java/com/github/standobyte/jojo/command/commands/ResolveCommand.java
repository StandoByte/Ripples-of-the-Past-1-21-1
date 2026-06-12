package com.github.standobyte.jojo.command.commands;

import java.util.Collection;

import com.github.standobyte.jojo.command.MultipleTargetsCommandResult;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.resolve.ResolveCounter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

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
		int i = 0;
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				ResolveCounter resolve = ResolveCounter.getExisting(living);
				if (resolve != null) {
					resolve.reset(living);
					i++;
				}
			}
		}

		return RESET_MSG.trySend(source, true, targets, i);
	}
}
