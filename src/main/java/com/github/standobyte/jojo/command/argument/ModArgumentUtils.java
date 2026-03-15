package com.github.standobyte.jojo.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModArgumentUtils {
    static CompletableFuture<Suggestions> suggestModResource(Stream<ResourceLocation> resourceLocations, SuggestionsBuilder builder, String defaultNamespace) {
        Iterable<ResourceLocation> resources = resourceLocations::iterator;
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);

        boolean flag = input.indexOf(':') > -1;

        for (ResourceLocation resLoc : resources) {
            if (flag) {
                String s = resLoc.toString();
                if (SharedSuggestionProvider.matchesSubStr(input, s)) {
                    builder.suggest(resLoc.toString());
                }
            } else if (SharedSuggestionProvider.matchesSubStr(input, resLoc.getNamespace())
                    || resLoc.getNamespace().equals(defaultNamespace) && SharedSuggestionProvider.matchesSubStr(input, resLoc.getPath())) {
                builder.suggest(resLoc.toString());
            }
        }

        return builder.buildFuture();
    }


    public static ResourceLocation read(StringReader reader, String defaultNamespace) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String location = readGreedy(reader);

        try {
            return parseResLoc(location, defaultNamespace);
        } catch (ResourceLocationException resourcelocationexception) {
            reader.setCursor(cursor);
            throw ResourceLocation.ERROR_INVALID.createWithContext(reader);
        }
    }

    private static String readGreedy(StringReader reader) {
        int cursor = reader.getCursor();

        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }

        return reader.getString().substring(cursor, reader.getCursor());
    }

    public static ResourceLocation parseResLoc(String location, String defaultNamespace) {
        return bySeparator(location, ':', defaultNamespace);
    }

    public static ResourceLocation bySeparator(String location, char seperator, String defaultNamespace) {
        int i = location.indexOf(seperator);
        if (i >= 0) {
            String path = location.substring(i + 1);
            if (i != 0) {
                String namespace = location.substring(0, i);
                return ResourceLocation.fromNamespaceAndPath(namespace, path);
            } else {
                return ResourceLocation.fromNamespaceAndPath(defaultNamespace, path);
            }
        } else {
            return ResourceLocation.fromNamespaceAndPath(defaultNamespace, location);
        }
    }
}
