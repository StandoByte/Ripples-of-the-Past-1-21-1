package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.objects_java.OptionalFloat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.realmsclient.util.JsonUtils;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;

// TODO (bgm) guide on the bgmloop files
public record BgmTrackInfo(@Nullable BgmLoopPartitioning loop, Sound sound) {
	
	public static class BgmLoopPartitioning {
		public final Map<BgmPart, Partition> partition;
		
		public BgmLoopPartitioning(Map<BgmPart, Partition> partition) {
			this.partition = partition;
		}

		public enum BgmPart {
			INTRO,
			MAIN_LOOP,
			OUTRO
		}

		public static record Partition(float start, OptionalFloat end) {}
		
		@Nullable
		public static BgmLoopPartitioning createLoop(Unbaked parsed) {
			if (!parsed.hasLoop) return null;
			
			if (parsed.loopStart.isPresent() && parsed.loopBack.isPresent() && parsed.loopBack.getAsFloat() <= parsed.loopStart.getAsFloat()) {
				BgmEngine.LOGGER.error("loopBack timestamp can't come earlier than loopStart! ({} < {})", 
						parsed.loopBack.getAsFloat(), parsed.loopStart);
				return null;
			}
			
			parsed.intro = toSecs(parsed.intro, parsed.bpm);
			parsed.loopStart = parsed.loopStart.map(time -> toSecs(time, parsed.bpm));
			parsed.loopBack = parsed.loopBack.map(time -> toSecs(time, parsed.bpm));
			parsed.outro = parsed.outro.map(time -> toSecs(time, parsed.bpm));
			
			OptionalFloat introEnd;
			if (parsed.loopStart.isPresent() && parsed.intro <= parsed.loopStart.getAsFloat()) 		introEnd = parsed.loopStart;
			else if (parsed.loopBack.isPresent() && parsed.intro < parsed.loopBack.getAsFloat()) 	introEnd = parsed.loopBack;
			else 																					introEnd = parsed.outro;
			
			Map<BgmPart, Partition> partition = new EnumMap<>(BgmPart.class);
			partition.put(BgmPart.INTRO, new Partition(parsed.intro, introEnd));
			if (parsed.loopStart.isPresent()) {
				partition.put(BgmPart.MAIN_LOOP, new Partition(parsed.loopStart.getAsFloat(), parsed.loopBack));
			}
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
		final ResourceLocation audio;
		boolean hasLoop;
		float bpm = 240;
		float intro;
		OptionalFloat loopStart = OptionalFloat.empty();
		OptionalFloat loopBack = OptionalFloat.empty();
		OptionalFloat outro = OptionalFloat.empty();
		
		int weight = 1;
		
		public Unbaked(ResourceLocation audio) {
			this.audio = audio;
		}
		

		public static void fromJson(JsonElement jsonElement, List<Unbaked> destination) {
			if (jsonElement.isJsonArray()) {
				JsonArray jsonArray = jsonElement.getAsJsonArray();
				for (JsonElement element : jsonArray) {
					fromJsonObj(element.getAsJsonObject(), destination);
				}
			}
			else {
				fromJsonObj(jsonElement.getAsJsonObject(), destination);
			}
		}
		
		public static void fromJsonObj(JsonObject json, List<Unbaked> destination) {
			ResourceLocation track = ResourceLocation.parse(json.get("track").getAsString());
			int weight = JsonUtils.getIntOr("weight", json, 1);
			
			OptionalFloat bpmElement = JsonParseHelper.getFloatOptional("bpm", json, JsonElement::getAsFloat);
			boolean hasLoop = bpmElement.isPresent();
			if (hasLoop) {
				float bpm = bpmElement.getAsFloat();
				float shift = JsonParseHelper.getFloatOr("shift", json, JsonParseHelper::parseFlStudioNote, 0);
				
				List<Float> introTimestamps = JSONUtil.parseArrayOrSingleElement(json.get("intro"), JsonParseHelper::parseFlStudioNote);
				if (introTimestamps == null) introTimestamps = Collections.singletonList(shift);
				
				OptionalFloat loopStart = JsonParseHelper.getFloatOptional("loopStart", json, JsonParseHelper::parseFlStudioNote);
				OptionalFloat loopBack = JsonParseHelper.getFloatOptional("loopBack", json, JsonParseHelper::parseFlStudioNote);
				
				OptionalFloat outro = JsonParseHelper.getFloatOptional("outro", json, JsonParseHelper::parseFlStudioNote);
				for (Float intro : introTimestamps) {
					Unbaked obj = new Unbaked(track);
					obj.hasLoop = true;
					obj.weight = weight;
					obj.bpm = bpm;
					obj.intro = Math.max(intro - shift, 0);
					obj.loopStart = loopStart.map(timestamp -> timestamp - shift);
					obj.loopBack = loopBack.map(timestamp -> timestamp - shift);
					obj.outro = outro.map(timestamp -> timestamp - shift);
					destination.add(obj);
				}
			}
			else {
				Unbaked obj = new Unbaked(track);
				obj.hasLoop = false;
				obj.weight = weight;
				destination.add(obj);
			}
		}
		
	}



	public static class JsonParseHelper {
	
		public static float getFloatOr(String key, JsonObject json, NoteParse parse, float defaultValue) {
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
					Integer.parseInt(split[0]),
					split.length > 1 ? Integer.parseInt(split[1]) : 1,
					split.length > 2 ? Integer.parseInt(split[2]) : 0,
				};
				
				boolean negative = values[0] < 0;
				if (negative) values[0] = -values[0];
				
				values[0] -= 1;
				values[1] -= 1;
				
				float time = values[0] + (float) values[1] / 16f + (float) values[2] / (16 * 24f);
				if (negative) time = -time;
				return time;
			}
			throw new IllegalArgumentException("Failed to parse FL Studio note (not an integer number or string)");
		}
	}
}
