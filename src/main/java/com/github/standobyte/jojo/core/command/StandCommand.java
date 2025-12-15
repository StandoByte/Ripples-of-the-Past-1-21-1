package com.github.standobyte.jojo.core.command;

import java.util.Collection;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.command.argument.StandArgument;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class StandCommand {
	private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.give.failed"));
	private static final SimpleCommandExceptionType ERROR_CLEAR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.clear.everything.failed"));


	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(
		Commands.literal(JojoMod.MOD_ID).then(
			Commands.literal("stand")
				.requires(src -> src.hasPermission(2))
				
				.then(
				Commands.literal("give")
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(
						Commands.argument("stand", StandArgument.stand(context))
							.executes(
							src -> setStand(
								src.getSource(),
								EntityArgument.getEntities(src, "targets"),
								StandArgument.getStand(src, "stand")
								)
							)
						)
					)
				)
				.then(
				Commands.literal("remove")
					.executes(src -> removeStand(src.getSource(), ImmutableList.of(src.getSource().getEntityOrException())))
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.executes(src -> removeStand(src.getSource(), EntityArgument.getEntities(src, "targets")))
					)
				)
//				.then(
//				Commands.literal("fullclear")
//					.executes(src -> fullClear(src.getSource(), ImmutableList.of(src.getSource().getEntityOrException())))
//					.then(
//					Commands.argument("targets", EntityArgument.entities())
//						.executes(src -> fullClear(src.getSource(), EntityArgument.getEntities(src, "targets")))
//					)
//				)
			)
		);
		JojoCommandsCommand.addCommand("stand");
	}
	
	private static int setStand(CommandSourceStack src, Collection<? extends Entity> targets, StandType standType) throws CommandSyntaxException {
		int i = 0;
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				PowerClass.STAND.attachPower(living);
				StandPower stand = PowerClass.STAND.get(living);
				if (stand != null) {
					stand.setStand(standType);
					i++;
				}
			}
		}
		
		if (i == 0) {
			throw ERROR_GIVE_FAILED.create();
		} else {
			// TODO "/stand" command chat confirmation
			if (targets.size() == 1) {
				src.sendSuccess( () -> Component.translatable(
						""), 
						true);
			} else {
				src.sendSuccess( () -> Component.translatable(
						""), 
						true);
			}

			return i;
		}
	}
	
	private static int removeStand(CommandSourceStack src, Collection<? extends Entity> targets) throws CommandSyntaxException {
		int i = 0;
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				StandPower stand = StandPower.get(living);
				if (stand != null) {
					stand.setStand(null);
					i++;
				}
			}
		}
		
		if (i == 0) {
			throw ERROR_CLEAR_FAILED.create();
		} else {
			if (targets.size() == 1) {
				src.sendSuccess( () -> Component.translatable(
						""), 
						true);
			} else {
				src.sendSuccess( () -> Component.translatable(
						""), 
						true);
			}

			return i;
		}
	}
}
