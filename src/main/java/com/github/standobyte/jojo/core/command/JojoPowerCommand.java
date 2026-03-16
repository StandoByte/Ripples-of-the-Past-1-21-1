package com.github.standobyte.jojo.core.command;

import java.util.Collection;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.command.argument.PlayerPowerTypeArgument;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class JojoPowerCommand {
	public static final MultipleTargetsCommandResult GIVE_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.non_stand.give");
	public static final MultipleTargetsCommandResult QUERY_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.non_stand.query");
	public static final MultipleTargetsCommandResult REMOVE_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.non_stand.remove", QUERY_MSG.fail);

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
				if (power != null && !power.hasPower()) {
					power.setPowerType(powerType);
					i++;
				}
			}
		}
		
		return GIVE_MSG.trySend(src, true, targets, i, powerType.name.get());
	}
	
	private static int removePower(CommandSourceStack src, Collection<? extends Entity> targets) throws CommandSyntaxException {
		int i = 0;
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				PlayerPower power = PlayerPower.get(living);
				if (power != null && power.hasPower()) {
					power.setPowerType(null);
					i++;
				}
			}
		}
		
		return REMOVE_MSG.trySend(src, true, targets, i);
	}
}
