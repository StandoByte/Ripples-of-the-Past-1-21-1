package com.github.standobyte.jojo.command.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.standobyte.jojo.command.MultipleTargetsCommandResult;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class StandExpCommand {
	public static final MultipleTargetsCommandResult SET_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.standexp.set", StandCommand.QUERY_MSG.fail);
	public static final MultipleTargetsCommandResult ADD_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.standexp.add", StandCommand.QUERY_MSG.fail);

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(
		Commands.literal(JojoMod.MOD_ID).then(
			Commands.literal("stand_exp")
				.requires(src -> src.hasPermission(2))
				
				.then(
				Commands.literal("add")
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(
						Commands.argument("amount", IntegerArgumentType.integer())
							.executes(
							src -> addStandExp(
								src.getSource(),
								EntityArgument.getEntities(src, "targets"),
								IntegerArgumentType.getInteger(src, "amount")
								)
							)
						)
					)
				)
				.then(
				Commands.literal("set")
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(
						Commands.argument("amount", IntegerArgumentType.integer(0))
							.executes(
							src -> setStandExp(
								src.getSource(),
								EntityArgument.getEntities(src, "targets"),
								IntegerArgumentType.getInteger(src, "amount")
								)
							)
						)
					)
				)
				.then(
				Commands.literal("query")
					.then(
					Commands.argument("target", EntityArgument.entity())
						.executes(
						src -> getStandExp(
							src.getSource(),
							EntityArgument.getEntity(src, "target")
							)
						)
					)
				)
			)
		);
		JojoCommandsCommand.addCommand("stand_exp");
	}

	private static int getStandExp(CommandSourceStack source, Entity target) throws CommandSyntaxException {
		StandPower stand = getStands(Util.make(new ArrayList<>(), list -> list.add(target))).iterator().next();
		int level = stand.getCurTypeData().getExp();
		source.sendSuccess(() -> Component.translatable("rotp.commands.standexp.query.success", 
				target.getDisplayName(), level, stand.getPowerType().name.get()), false);
		return level;
	}

	private static int addStandExp(CommandSourceStack source, Collection<? extends Entity> targets, int exp) throws CommandSyntaxException {
		Collection<StandPower> stands = getStands(targets);
		for (StandPower stand : stands) {
			StandTypePersistentData standData = stand.getCurTypeData();
			standData.addExp(exp, stand.getUser());
		}

		return ADD_MSG.trySend(source, true, targets, stands.size(), exp);
	}

	private static int setStandExp(CommandSourceStack source, Collection<? extends Entity> targets, int exp) throws CommandSyntaxException {
		Collection<StandPower> stands = getStands(targets);
		for (StandPower stand : stands) {
			StandTypePersistentData standData = stand.getCurTypeData();
			standData.setExp(exp, stand);
		}

		return SET_MSG.trySend(source, true, targets, stands.size(), exp);
	}

	private static Collection<StandPower> getStands(Collection<? extends Entity> targets) throws CommandSyntaxException {
		List<StandPower> stands = new ArrayList<>();
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				StandPower stand = StandPower.get(living);
				if (stand != null && stand.hasPower()) {
					stands.add(stand);
				}
			}
		}
		if (stands.isEmpty()) {
			throw StandCommand.QUERY_MSG.fail.create(targets);
		}
		else {
			return stands;
		}
	}
}
