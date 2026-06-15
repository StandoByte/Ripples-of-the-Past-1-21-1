package com.github.standobyte.jojo.command.commands;

import java.util.Collection;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.command.MultipleTargetsCommandResult;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojoimpl.powers.pillarman.PillarmanData;
import com.github.standobyte.jojoimpl.powers.pillarman.PillarmanMode;
import com.github.standobyte.jojoimpl.powers.pillarman.PillarmanStage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class PillarmanCommand {
	public static final MultipleTargetsCommandResult SET_STAGE_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.pillarman.stage");
	public static final MultipleTargetsCommandResult SET_MODE_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.pillarman.mode");

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(
		Commands.literal(JojoMod.MOD_ID).then(
			Commands.literal("pillarman")
				.requires(src -> src.hasPermission(2))
				
				.then(
				Commands.literal("stage")
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(Commands.literal("1").executes(src -> setStage(src.getSource(), EntityArgument.getEntities(src, "targets"), PillarmanStage.SANTANA)))
						.then(Commands.literal("2").executes(src -> setStage(src.getSource(), EntityArgument.getEntities(src, "targets"), PillarmanStage.MODE_USER)))
						.then(Commands.literal("3").executes(src -> setStage(src.getSource(), EntityArgument.getEntities(src, "targets"), PillarmanStage.AJA_BUFF)))
						.then(Commands.literal("4").executes(src -> setStage(src.getSource(), EntityArgument.getEntities(src, "targets"), PillarmanStage.ULTIMATE_THING)))
					)
				)
				.then(
				Commands.literal("mode")
					.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(Commands.literal("light").executes(src -> setMode(src.getSource(), EntityArgument.getEntities(src, "targets"), PillarmanMode.LIGHT)))
						.then(Commands.literal("heat").executes(src -> setMode(src.getSource(), EntityArgument.getEntities(src, "targets"), PillarmanMode.HEAT)))
						.then(Commands.literal("wind").executes(src -> setMode(src.getSource(), EntityArgument.getEntities(src, "targets"), PillarmanMode.WIND)))
						.then(Commands.literal("none").executes(src -> setMode(src.getSource(), EntityArgument.getEntities(src, "targets"), null)))
					)
				)
			)
		);
		JojoCommandsCommand.addCommand("pillarman");
	}
	
	private static int setStage(CommandSourceStack src, Collection<? extends Entity> targets, PillarmanStage stage) throws CommandSyntaxException {
		int i = 0;
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				PlayerPower power = PlayerPower.get(living);
				if (power != null) {
					PowerData powerData = power.getCurTypeData();
					if (powerData != null && powerData.getPowerType() == ModPlayerPowers.PILLAR_MAN.get()) {
						PillarmanData pillarman = (PillarmanData) powerData;
						pillarman.setStage(stage, living);
						i++;
					}
				}
			}
		}
		
		return SET_STAGE_MSG.trySend(src, true, targets, i, stage.name().toLowerCase());
	}
	
	private static int setMode(CommandSourceStack src, Collection<? extends Entity> targets, @Nullable PillarmanMode mode) throws CommandSyntaxException {
		int i = 0;
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				PlayerPower power = PlayerPower.get(living);
				if (power != null) {
					PowerData powerData = power.getCurTypeData();
					if (powerData != null && powerData.getPowerType() == ModPlayerPowers.PILLAR_MAN.get()) {
						PillarmanData pillarman = (PillarmanData) powerData;
						pillarman.setMode(mode, living);
						i++;
					}
				}
			}
		}

		return SET_MODE_MSG.trySend(src, true, targets, i, mode != null ? mode.name().toLowerCase() : "none");
	}
	
}
