package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.util.java.OptionalFloat;
import com.github.standobyte.jojo.util.java.WeightsList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.realmsclient.util.JsonUtils;

import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class BgmLoopPartitioning {
    public static final FileToIdConverter LISTER = new FileToIdConverter("sounds", ".bgmmeta");
	
    // TODO (bgm) guide on the bgmmeta files
	protected static class Unbaked {
		float bpm;
		float intro;
		float loopStart;
		
		OptionalFloat loopBack = OptionalFloat.empty();
		// TODO (bgm) optional fade out
		OptionalFloat outro = OptionalFloat.empty();
		
		int weight = 1;
		@Nullable ResourceLocation[] soundEvent;
		
		public static Unbaked fromJson(JsonObject json) {
			Unbaked obj = new Unbaked();
			obj.bpm = json.get("bpm").getAsFloat();
			obj.intro = getFloatOr("intro", json, 0, Unbaked::parseFlStudioNote);
			obj.loopStart = getFloatOr("loopStart", json, 0, Unbaked::parseFlStudioNote);
			obj.loopBack = getFloatOptional("loopBack", json, Unbaked::parseFlStudioNote);
			obj.outro = getFloatOptional("outro", json, Unbaked::parseFlStudioNote);
			
			JsonElement soundEvent = json.get("soundEvent");
			if (soundEvent != null) {
				if (soundEvent.isJsonArray()) {
					JsonArray soundEventsArray = soundEvent.getAsJsonArray();
					obj.soundEvent = new ResourceLocation[soundEventsArray.size()];
					int i = 0;
					for (JsonElement singleSoundEvent : soundEventsArray) {
						obj.soundEvent[i++] = ResourceLocation.parse(singleSoundEvent.getAsString());
					}
				}
				else {
					obj.soundEvent = new ResourceLocation[1];
					obj.soundEvent[0] = ResourceLocation.parse(soundEvent.getAsString());
				}
			}
			
			obj.weight = JsonUtils.getIntOr("weight", json, 1);
			
			return obj;
		}
		
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
		
		public BgmLoopPartitioning create() {
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
			BgmLoopPartitioning value = new BgmLoopPartitioning(partition);
			value.soundEvents = this.soundEvent;
			
			return value;
		}
	}
	
	public static float toSecs(float note, float bpm) {
		return note * 240 /*4 beats * 60 seconds*/ / bpm;
	}


	public enum BgmPart {
		INTRO,
		MAIN,
		OUTRO
	}

	public static record Partition(float start, OptionalFloat end) {}
	
	public final Map<BgmPart, Partition> partition;
	@Nullable public ResourceLocation[] soundEvents;
	
	public BgmLoopPartitioning(Map<BgmPart, Partition> partition) {
		this.partition = partition;
	}
	
	public boolean matchesSoundEvent(SoundEvent soundEvent) {
		return this.soundEvents == null || ArrayUtils.contains(this.soundEvents, soundEvent.getLocation());
	}
	
	// parsing stuff
	
	public static List<Weighted<BgmLoopPartitioning>> parseList(JsonElement json) {
		List<Weighted<BgmLoopPartitioning>> list = new ArrayList<>();
		if (json.isJsonArray()) {
			for (JsonElement element : json.getAsJsonArray()) {
				Unbaked parsed = Unbaked.fromJson(element.getAsJsonObject());
				if (parsed != null) {
					list.add(new WeightsList.WeightedEntry<>(parsed.create(), 1));
				}
			}
		}
		else {
			Unbaked parsed = Unbaked.fromJson(json.getAsJsonObject());
			if (parsed != null) {
				list.add(new WeightsList.WeightedEntry<>(parsed.create(), 1));
			}
		}
		return list;
	}

	static float getFloatOr(String key, JsonObject json, float defaultValue, NoteParse parse) {
		JsonElement jsonelement = json.get(key);
		if (jsonelement != null) {
			return jsonelement.isJsonNull() ? defaultValue : parse.parse(jsonelement);
		} else {
			return defaultValue;
		}
	}

	@Nonnull
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
