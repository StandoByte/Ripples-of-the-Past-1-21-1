package com.github.standobyte.jojo.command.commands;

import java.util.Collection;

import com.github.standobyte.jojo.command.argument.PlayerPowerTypeArgument;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
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

public class JojoPowerCommand {
	private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.give.failed"));
	private static final SimpleCommandExceptionType ERROR_CLEAR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.clear.everything.failed"));


	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(
		Commands.literal(JojoMod.MOD_ID).then(
			Commands.literal("power")
				.requires(src -> src.hasPermission(2))
				
				.then(
				Commands.literal("give")
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(
						Commands.argument("player_power", PlayerPowerTypeArgument.power(context))
							.executes(
							src -> setPower(
								src.getSource(),
								EntityArgument.getEntities(src, "targets"),
                                PlayerPowerTypeArgument.getPlayerPower(src, "player_power")
								)
							)
						)
					)
				)
				.then(
				Commands.literal("remove")
					.executes(src -> removePower(src.getSource(), ImmutableList.of(src.getSource().getEntityOrException())))
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.executes(src -> removePower(src.getSource(), EntityArgument.getEntities(src, "targets")))
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
		JojoCommandsCommand.addCommand("power");
	}
	
	private static int setPower(CommandSourceStack src, Collection<? extends Entity> targets, PlayerPowerType<?> powerType) throws CommandSyntaxException {
		int i = 0;
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				PowerClass.PLAYER_POWER.attachPower(living);
				PlayerPower power = PowerClass.PLAYER_POWER.get(living);
				if (power != null) {
					power.setPowerType(powerType);
					i++;
				}
			}
		}
		
		if (i == 0) {
			throw ERROR_GIVE_FAILED.create();
		} else {
			// TODO "/power" command chat confirmation
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
	
	private static int removePower(CommandSourceStack src, Collection<? extends Entity> targets) throws CommandSyntaxException {
		int i = 0;
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				PlayerPower power = PlayerPower.get(living);
				if (power != null) {
					power.setPowerType(null);
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
