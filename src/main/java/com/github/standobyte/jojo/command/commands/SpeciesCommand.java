package com.github.standobyte.jojo.command.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.standobyte.jojo.adventure.character.CharacterPersonData;
import com.github.standobyte.jojo.adventure.character.CharacterSpecies;
import com.github.standobyte.jojo.command.MultipleTargetsCommandResult;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class SpeciesCommand {
	public static final MultipleTargetsCommandResult SET_SPECIES_MSG = new MultipleTargetsCommandResult(
			"rotp.commands.species");
	
	public static List<String> SPECIES_IN_COMMAND = new ArrayList<>();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerCommands(RegisterCommandsEvent event) {
		SPECIES_IN_COMMAND.add("human");
		SPECIES_IN_COMMAND.add("pillar_man");
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(
		Commands.literal(JojoMod.MOD_ID).then(
			Commands.literal("species")
				.requires(src -> src.hasPermission(2))
//				.then(
//				Commands.argument("targets", EntityArgument.entities())
//					.then(Commands.literal("human").executes(src -> setSpecies(src.getSource(), EntityArgument.getEntities(src, "targets"), "human")))
//					.then(Commands.literal("pillar_man").executes(src -> setSpecies(src.getSource(), EntityArgument.getEntities(src, "targets"), "pillar_man")))
//				)
				.then(
				addSpeciesList(Commands.argument("targets", EntityArgument.entities())
				))
			)
		);
		JojoCommandsCommand.addCommand("species");
	}
	
	static ArgumentBuilder<CommandSourceStack, ?> addSpeciesList(ArgumentBuilder<CommandSourceStack, ?> argument) {
		for (String species : SPECIES_IN_COMMAND) {
			argument.then(Commands.literal(species).executes(src -> setSpecies(src.getSource(), EntityArgument.getEntities(src, "targets"), species)));
		}
		return argument;
	}
	
	private static int setSpecies(CommandSourceStack src, Collection<? extends Entity> targets, String speciesName) throws CommandSyntaxException {
		speciesName = speciesName.toLowerCase();
		int i = 0;
		CharacterSpecies species = CharacterSpecies.fromName(speciesName);
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living) {
				CharacterPersonData characterData = CharacterPersonData.get(living);
				if (characterData != null) {
					characterData.__initializeCharacterSpecies(species);
					i++;
				}
			}
		}
		
		return SET_SPECIES_MSG.trySend(src, true, targets, i, speciesName);
	}
	
}
