package com.github.standobyte.jojo.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StandArgument implements ArgumentType<StandType> {
	private static final Collection<String> EXAMPLES = Arrays.asList("star_platinum", JojoMod.MOD_ID + ":the_world");
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_TYPE = new DynamicCommandExceptionType(
			arg -> Component.translatableEscape("stand.unknown", arg));
	
	public StandArgument(CommandBuildContext buildContext) {
	}
	
	public static StandArgument stand(CommandBuildContext buildContext) {
		return new StandArgument(buildContext);
	}

	@Nullable
	public static StandType getStand(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, StandType.class);
	}

	@Override
	public StandType parse(StringReader reader) throws CommandSyntaxException {
		ResourceLocation id = ModArgumentUtils.read(reader, JojoMod.MOD_ID);
		StandType standType = StandType.fromId(id);
		if (standType == null) {
			throw ERROR_UNKNOWN_TYPE.createWithContext(reader, id);
		}
		return standType;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return ModArgumentUtils.suggestModResource(StandType.getAllEnabledStands().map(StandType::getId), builder, JojoMod.MOD_ID);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
