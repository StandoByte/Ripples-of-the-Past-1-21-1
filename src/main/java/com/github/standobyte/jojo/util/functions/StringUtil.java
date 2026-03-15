package com.github.standobyte.jojo.util.functions;

import java.util.OptionalInt;

import com.mojang.datafixers.util.Pair;

public final class StringUtil {

	public static String trimEnding(String string, String... endings) {
		if (endings.length == 0) return string;
		for (int i = endings.length - 1; i >= 0; i--) {
			String ending = endings[i];
			if (string.endsWith(ending)) {
				string = substrBack(string, ending.length());
			}
			else {
				return string;
			}
		}
		return string;
	}
	
	// it's 4 AM, idk how to call this better
	public static String substrBack(String string, int characters) {
		return string.substring(0, string.length() - characters);
	}
	
	public static Pair<String, OptionalInt> splitIntAtTheEnd(String string) {
		if (string.isEmpty()) return Pair.of(string, OptionalInt.empty());
		int i;
		for (i = string.length(); i > 0 && Character.isDigit(string.charAt(i - 1)); i--) {}
		if (i == string.length()) return Pair.of(string, OptionalInt.empty());
		
		String substr = string.substring(0, i);
		int number = Integer.parseInt(string.substring(i));
		return Pair.of(substr, OptionalInt.of(number));
	}
	
}
