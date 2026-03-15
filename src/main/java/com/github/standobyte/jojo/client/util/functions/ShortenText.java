package com.github.standobyte.jojo.client.util.functions;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

public class ShortenText {

	public static MutableComponent shortenedTranslatable(String translatableKey) {
		return Component.translatableWithFallback(translatableKey + ".shortened", translatableKey);
	}

	public static MutableComponent shortenedTranslatable(String translatableKey, Object... args) {
		return Component.translatableWithFallback(translatableKey + ".shortened", translatableKey, args);
	}

	public static Component shortenIfAble(Component translatable) {
		if (translatable.getContents() instanceof TranslatableContents contents) {
			String translatableKey = contents.getKey();
			return shortenedTranslatable(translatableKey, contents.getArgs());
		}
		return translatable;
	}
}
