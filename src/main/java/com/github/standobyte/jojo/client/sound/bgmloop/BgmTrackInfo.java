package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.JSONUtil;
import com.github.standobyte.jojo.util.java.OptionalFloat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.realmsclient.util.JsonUtils;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;

// TODO (bgm) guide on the bgmloop files
public record BgmTrackInfo(@Nullable BgmLoopPartitioning loop, Sound sound) {
    public static final FileToIdConverter LISTER = new FileToIdConverter("sounds", ".bgmloop.json");
	
	public static class BgmLoopPartitioning {
		public final Map<BgmPart, Partition> partition;
		
		public BgmLoopPartitioning(Map<BgmPart, Partition> partition) {
			this.partition = partition;
		}

		public enum BgmPart {
			INTRO,
			MAIN,
			OUTRO
		}

		public static record Partition(float start, OptionalFloat end) {}
		
		@Nullable
		public static BgmLoopPartitioning createLoop(Unbaked parsed) {
			if (!parsed.hasLoop) return null;
			
			if (parsed.loopBack.isPresent() && parsed.loopBack.getAsFloat() <= parsed.loopStart) {
				BgmTrackLoader.LOGGER.error("loopBack timestamp can't come earlier than loopStart! ({} < {})", 
						parsed.loopBack.getAsFloat(), parsed.loopStart);
				return null;
			}
			
			parsed.intro = toSecs(parsed.intro, parsed.bpm);
			parsed.loopStart = toSecs(parsed.loopStart, parsed.bpm);
			if (parsed.loopBack.isPresent()) parsed.loopBack = OptionalFloat.of(toSecs(parsed.loopBack.getAsFloat(), parsed.bpm));
			if (parsed.outro.isPresent()) parsed.outro = OptionalFloat.of(toSecs(parsed.outro.getAsFloat(), parsed.bpm));
			
			OptionalFloat introEnd;
			if (parsed.intro <= parsed.loopStart) 													introEnd = OptionalFloat.of(parsed.loopStart);
			else if (parsed.loopBack.isPresent() && parsed.intro < parsed.loopBack.getAsFloat()) 	introEnd = parsed.loopBack;
			else 																					introEnd = OptionalFloat.empty();
			
			Map<BgmPart, Partition> partition = new EnumMap<>(BgmPart.class);
			partition.put(BgmPart.INTRO, new Partition(parsed.intro, introEnd));
			partition.put(BgmPart.MAIN, new Partition(parsed.loopStart, parsed.loopBack));
			if (parsed.outro.isPresent()) {
				partition.put(BgmPart.OUTRO, new Partition(parsed.outro.getAsFloat(), OptionalFloat.empty()));
			}
			BgmLoopPartitioning value = new BgmLoopPartitioning(partition);
			
			return value;
		}
		
		public static float toSecs(float note, float bpm) {
			return note * 240 /*4 beats * 60 seconds*/ / bpm;
		}
	}
	
	
	
	public static class Unbaked {
		boolean hasLoop;
		float bpm = 240;
		float intro;
		float loopStart;
		OptionalFloat loopBack = OptionalFloat.empty();
		// TODO (bgm) optional fade out
		OptionalFloat outro = OptionalFloat.empty();
		
		int weight = 1;
		
		@Nullable List<ResourceLocation> trackIds;
		@Nullable List<ResourceLocation> standTypeIds;
		
		public static Unbaked fromJson(JsonObject json) {
			Unbaked obj = new Unbaked();
			JsonElement bpm = json.get("bpm");
			if (bpm != null) {
				obj.hasLoop = true;
				obj.bpm = bpm.getAsFloat();
				obj.intro = JsonParseHelper.getFloatOr("intro", json, 0, JsonParseHelper::parseFlStudioNote);
				obj.loopStart = JsonParseHelper.getFloatOr("loopStart", json, 0, JsonParseHelper::parseFlStudioNote);
				obj.loopBack = JsonParseHelper.getFloatOptional("loopBack", json, JsonParseHelper::parseFlStudioNote);
				obj.outro = JsonParseHelper.getFloatOptional("outro", json, JsonParseHelper::parseFlStudioNote);
			}
			else {
				obj.hasLoop = false;
			}
			
			obj.trackIds = JSONUtil.parseArrayOrSingleElement(json.get("track"), elem -> ResourceLocation.parse(elem.getAsString()));
			obj.standTypeIds = JSONUtil.parseArrayOrSingleElement(json.get("stand"), elem -> ResourceLocation.parse(elem.getAsString()));
			
			obj.weight = JsonUtils.getIntOr("weight", json, 1);
			
			return obj;
		}
	}



	public static class JsonParseHelper {
	
		public static float getFloatOr(String key, JsonObject json, float defaultValue, NoteParse parse) {
			JsonElement jsonelement = json.get(key);
			if (jsonelement != null) {
				return jsonelement.isJsonNull() ? defaultValue : parse.parse(jsonelement);
			} else {
				return defaultValue;
			}
		}
	
		@Nonnull
		public static OptionalFloat getFloatOptional(String key, JsonObject json, NoteParse parse) {
			JsonElement jsonelement = json.get(key);
			if (jsonelement != null) {
				return jsonelement.isJsonNull() ? OptionalFloat.empty() : OptionalFloat.of(parse.parse(jsonelement));
			} else {
				return OptionalFloat.empty();
			}
		}
		
		
		@FunctionalInterface
		public static interface NoteParse { float parse(JsonElement json); }
		
		public static float parseFlStudioNote(JsonElement json) {
			JsonPrimitive element = json.getAsJsonPrimitive();
			if (element.isNumber()) {
				return json.getAsInt() - 1;
			}
			else if (element.isString()) {
				String string = element.getAsString();
				String[] split = string.split(":");
				if (split.length > 3) {
					throw new IllegalArgumentException("Failed to parse FL Studio note (too many note subdivisions)");
				}
				int[] values = new int[] {
					Integer.parseInt(split[0]) - 1,
					split.length > 1 ? Integer.parseInt(split[1]) - 1 : 0,
					split.length > 2 ? Integer.parseInt(split[2]) : 0,
				};
				return values[0] + (float) values[1] / 16f + (float) values[2] / (16 * 24f);
			}
			throw new IllegalArgumentException("Failed to parse FL Studio note (not an integer number or string)");
		}
	}
}
