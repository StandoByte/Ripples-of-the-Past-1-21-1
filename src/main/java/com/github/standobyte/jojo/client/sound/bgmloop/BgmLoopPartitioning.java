package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.util.java.OptionalFloat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.resources.FileToIdConverter;

public record BgmLoopPartitioning(Map<BgmPart, Partition> partition) {
    public static final FileToIdConverter LISTER = new FileToIdConverter("sounds", ".bgmmeta");

	public enum BgmPart {
		INTRO,
		MAIN,
		OUTRO
	}
	
	public static record Partition(float start, OptionalFloat end) {}
	
	public static BgmLoopPartitioning fromResourceJson(JsonElement json) {
		JsonObject jsonObj = json.getAsJsonObject();
		return fromTimestamps(
				jsonObj.get("bpm").getAsFloat(), 
				getFloatOr("intro", jsonObj, 0, BgmLoopPartitioning::parseNote),
				getFloatOr("loopStart", jsonObj, 0, BgmLoopPartitioning::parseNote),
				getFloatOptional("loopBack", jsonObj, BgmLoopPartitioning::parseNote),
				getFloatOptional("outro", jsonObj, BgmLoopPartitioning::parseNote));
	}
	
	public static BgmLoopPartitioning fromTimestamps(float bpm, float intro, float loopStart, OptionalFloat loopBack, OptionalFloat outro) {
		if (loopBack.isPresent() && loopBack.getAsFloat() <= loopStart) 
			throw new IllegalArgumentException(String.format("loopBack timestamp can't come earlier than loopStart! (%f < %f)", loopBack.getAsFloat(), loopStart));
		
		intro = toSecs(intro, bpm);
		loopStart = toSecs(loopStart, bpm);
		if (loopBack.isPresent()) loopBack = OptionalFloat.of(toSecs(loopBack.getAsFloat(), bpm));
		if (outro.isPresent()) outro = OptionalFloat.of(toSecs(outro.getAsFloat(), bpm));
		
		OptionalFloat introEnd;
		if (intro <= loopStart) 											introEnd = OptionalFloat.of(loopStart);
		else if (loopBack.isPresent() && intro < loopBack.getAsFloat()) 	introEnd = loopBack;
		else 																introEnd = OptionalFloat.empty();
		
		Map<BgmPart, Partition> partition = new EnumMap<>(BgmPart.class);
		partition.put(BgmPart.INTRO, new Partition(intro, introEnd));
		partition.put(BgmPart.MAIN, new Partition(loopStart, loopBack));
		if (outro.isPresent()) {
			partition.put(BgmPart.OUTRO, new Partition(outro.getAsFloat(), OptionalFloat.empty()));
		}
		return new BgmLoopPartitioning(partition);
	}
	
	public static float toSecs(float note, float bpm) {
		return note * 240 /*4 beats * 60 seconds*/ / bpm;
	}
	
	// parsing stuff
	
	public static float parseNote(JsonElement json) {
		JsonPrimitive element = json.getAsJsonPrimitive();
		if (element.isNumber()) {
			return json.getAsFloat();
		}
		else if (element.isString()) {
			String string = element.getAsString();
			String[] split = string.split(":");
			if (split.length > 3) {
				throw new IllegalArgumentException("Failed to parse note (too many note subdivisions)");
			}
			int[] values = new int[] {
				Integer.parseInt(split[0]),
				split.length > 1 ? Integer.parseInt(split[1]) : 0,
				split.length > 2 ? Integer.parseInt(split[2]) : 0,
			};
			return values[0] + (float) values[1] / 16f + (float) values[2] / 24f;
		}
		throw new IllegalArgumentException("Failed to parse note (not a number or string)");
	}

	static float getFloatOr(String key, JsonObject json, float defaultValue, NoteParse parse) {
		JsonElement jsonelement = json.get(key);
		if (jsonelement != null) {
			return jsonelement.isJsonNull() ? defaultValue : parse.parse(jsonelement);
		} else {
			return defaultValue;
		}
	}

	static OptionalFloat getFloatOptional(String key, JsonObject json, NoteParse parse) {
		JsonElement jsonelement = json.get(key);
		if (jsonelement != null) {
			return jsonelement.isJsonNull() ? OptionalFloat.empty() : OptionalFloat.of(parse.parse(jsonelement));
		} else {
			return OptionalFloat.empty();
		}
	}
	
	@FunctionalInterface
	static interface NoteParse { float parse(JsonElement json); }
	
}
