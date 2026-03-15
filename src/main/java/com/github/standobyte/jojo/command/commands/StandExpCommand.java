package com.github.standobyte.jojo.command.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
		source.sendSuccess(() -> Component.translatable("commands.standexp.query.success", 
				target.getDisplayName(), level, stand.getPowerType().name.get()), false);
		return level;
	}

	private static int addStandExp(CommandSourceStack source, Collection<? extends Entity> targets, int exp) throws CommandSyntaxException {
		Collection<StandPower> stands = getStands(targets);
		for (StandPower stand : stands) {
			StandTypePersistentData standData = stand.getCurTypeData();
			standData.setExp(standData.getExp() + exp, stand.getUser());
		}

		if (stands.size() == 1) {
			source.sendSuccess(() -> Component.translatable("commands.standexp.add.success.single", exp, targets.iterator().next().getDisplayName()), true);
		} else {
			source.sendSuccess(() -> Component.translatable("commands.standexp.add.success.multiple", exp, stands.size()), true);
		}

		return stands.size();
	}

	private static int setStandExp(CommandSourceStack source, Collection<? extends Entity> targets, int exp) throws CommandSyntaxException {
		Collection<StandPower> stands = getStands(targets);
		for (StandPower stand : stands) {
			StandTypePersistentData standData = stand.getCurTypeData();
			standData.setExp(exp, stand.getUser());
		}

		if (stands.size() == 1) {
			source.sendSuccess(() -> Component.translatable("commands.standexp.set.success.single", exp, targets.iterator().next().getDisplayName()), true);
		} else {
			source.sendSuccess(() -> Component.translatable("commands.standexp.set.success.multiple", exp, stands.size()), true);
		}

		return stands.size();
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
			if (targets.size() == 1) {
				throw StandCommand.NO_STAND_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
			}
			else {
				throw StandCommand.NO_STAND_MULTIPLE_EXCEPTION.create(targets.size());
			}
		}
		else {
			return stands;
		}
	}
}
