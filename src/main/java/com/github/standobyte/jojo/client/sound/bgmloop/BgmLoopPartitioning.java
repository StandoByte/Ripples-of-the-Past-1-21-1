package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.util.JSONUtil;
import com.github.standobyte.jojo.util.java.OptionalFloat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
				JSONUtil.getFloatOr("intro", jsonObj, 0),
				JSONUtil.getFloatOr("loopStart", jsonObj, 0),
				JSONUtil.getFloatOptional("loopBack", jsonObj),
				JSONUtil.getFloatOptional("outro", jsonObj));
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
}
