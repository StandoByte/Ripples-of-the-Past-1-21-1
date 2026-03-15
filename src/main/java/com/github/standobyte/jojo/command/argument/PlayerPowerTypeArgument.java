package com.github.standobyte.jojo.command.argument;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerPowerTypeArgument implements ArgumentType<PlayerPowerType<?>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("hamon", JojoMod.MOD_ID + ":hamon");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_TYPE = new DynamicCommandExceptionType(
            arg -> Component.translatableEscape("player_power.unknown", arg));

    public PlayerPowerTypeArgument(CommandBuildContext buildContext) {
    }

    public static PlayerPowerTypeArgument power(CommandBuildContext buildContext) {
        return new PlayerPowerTypeArgument(buildContext);
    }

    @Nullable
    public static PlayerPowerType<?> getPlayerPower(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, PlayerPowerType.class);
    }

    @Override
    public PlayerPowerType<?> parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation id = ModArgumentUtils.read(reader, JojoMod.MOD_ID);
        PlayerPowerType<?> powerType = PlayerPowerType.fromId(id);
        if (powerType == null) {
            throw ERROR_UNKNOWN_TYPE.createWithContext(reader, id);
        }
        return powerType;
    }
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ModArgumentUtils.suggestModResource(PlayerPowerType.getAllEnabledPlayerPowers().map(PlayerPowerType::getId), builder, JojoMod.MOD_ID);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
