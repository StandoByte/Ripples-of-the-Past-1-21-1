package com.github.standobyte.jojo.client.entityanim.gecko;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.action.AnimActionPhase;
import com.github.standobyte.jojo.client.entityanim.molang.KeyframeQuery;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationChannel.Interpolation;
import net.minecraft.client.animation.Keyframe;

public class ParseGeckoAnims {
	
	// XXX parse generic BB format anims
	// "geckolib_format_version": 2
	public static RotpAnimDefinition parseAnim(JsonObject animJson) {
		
		// Animation metadata
		
		float lengthSecs = animJson.has("animation_length") ? animJson.get("animation_length").getAsFloat() : 0;
		RotpAnimDefinition.Builder builder = new RotpAnimDefinition.Builder(lengthSecs);

		boolean loop = false;
//		boolean holdOnLastFrame = false;
		JsonElement loopJson = animJson.get("loop");
		if (loopJson != null && loopJson.isJsonPrimitive()) {
			String loopMode = loopJson.getAsString();
			if ("hold_on_last_frame".equals(loopMode)) {
//				holdOnLastFrame = true;
			}
			else {
				loop = loopJson.getAsBoolean();
			}
		}
		if (loop) {
			builder.looping();
		}
		
		// Keyframes
		
		JsonObject boneAnims = animJson.getAsJsonObject("bones");
		if (boneAnims != null) {
			for (Map.Entry<String, JsonElement> bone : boneAnims.entrySet()) {
				String boneName = bone.getKey();
				JsonObject tfJson = bone.getValue().getAsJsonObject();
				parseKeyframes(builder, tfJson, "rotation", AnimationChannel.Targets.ROTATION, boneName);
				parseKeyframes(builder, tfJson, "position", AnimationChannel.Targets.POSITION, boneName);
				parseKeyframes(builder, tfJson, "scale", AnimationChannel.Targets.SCALE, boneName);
			}
		}

		// Effects -> Instructions
		
		JsonObject instructionsJson = animJson.getAsJsonObject("timeline");
		if (instructionsJson != null) {
			for (Map.Entry<String, JsonElement> keyframeEntry : instructionsJson.entrySet()) {
				float time = Float.parseFloat(keyframeEntry.getKey());
				JsonElement value = keyframeEntry.getValue();
				Iterable<JsonElement> instructions = value.isJsonArray() ? value.getAsJsonArray() : Collections.singleton(value);
				Map<String, String> assignmentMap = Streams.stream(instructions)
						.filter(json -> json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
						.map(JsonElement::getAsString)
						.map(instruction -> instruction.split("[ ]*=[ ]*"))
						.filter(assignment -> assignment.length == 2)
						.peek(assignment -> {
							if (assignment[1].endsWith(";")) {
								assignment[1] = assignment[1].substring(0, assignment[1].length() - 1);
							}
						})
						.collect(Collectors.toMap(assignment -> assignment[0], assignment -> assignment[1], 
								(u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); }, LinkedHashMap::new));
				while (!assignmentMap.isEmpty()) {
					Map.Entry<String, String> assignment = assignmentMap.entrySet().iterator().next();
					String field = assignment.getKey();
					String assignmentValue = assignment.getValue();

					switch (field) {
						case "phase" -> {
							ActionPhase phase = ActionPhase.valueOf(assignmentValue);
							AnimActionPhase animPhase = parseAnimPhase(phase, assignmentMap);
							builder.addActionPhaseKeyframe(animPhase, time);
						}
						case "loopBack" -> {
							builder.looping(Float.parseFloat(assignmentMap.get(field)));
						}
						default -> builder.addFieldValueKeyframe(field, assignmentValue, time);
					}
					
					assignmentMap.remove(assignment.getKey());
				}
			}
		}
		
		RotpAnimDefinition anim = builder.build();
		return anim;
	}
	
	private static void parseKeyframes(RotpAnimDefinition.Builder anim, JsonObject boneTfJson, 
			String targetName, AnimationChannel.Target target, String boneName) {
		JsonElement element = boneTfJson.get(targetName);
		if (element == null) return;
		Float2ObjectMap<KeyframeQuery> timeline = new Float2ObjectArrayMap<>();
		
		if (element.isJsonObject()) {
			JsonObject keyframesJson = element.getAsJsonObject();
			for (Map.Entry<String, JsonElement> rotationJson : keyframesJson.entrySet()) {
				float time;
				JsonElement rotation;
				try {
					time = Float.parseFloat(rotationJson.getKey());
					rotation = rotationJson.getValue();
				}
				catch (NumberFormatException singleKeyframeFormat) {
					time = 0;
					rotation = keyframesJson;
				}
				parseKeyframe(timeline, time, rotation);
			}
		}
		else {
			parseKeyframe(timeline, 0, element);
		}
		
		KeyframeQuery[] keyframeQueries = keyframesToArray(timeline, KeyframeQuery[]::new);
		Keyframe[] vanillaKeyframes = Arrays.stream(keyframeQueries)
				.map(KeyframeQuery::getKeyframe)
				.toArray(size -> new Keyframe[keyframeQueries.length]);
		anim.addAnimation(boneName, new AnimationChannel(target, vanillaKeyframes));
		for (var query : keyframeQueries) {
			anim.addExpressionQuery(query);
		}
	}
	
	private static void parseKeyframe(Float2ObjectMap<KeyframeQuery> keyframesTimeline, float time, JsonElement keyframeValue) {
		Optional<JsonObject> keyframeObj = keyframeValue.isJsonObject() ? Optional.of(keyframeValue.getAsJsonObject()) : Optional.empty();
		
		JsonArray rotVecJson = keyframeObj.map(keyframe -> {
			JsonElement rotVecJsonElem = keyframe.get("vector");
			if (rotVecJsonElem == null && keyframe.has("post")) rotVecJsonElem = keyframe.get("post").getAsJsonObject().get("vector");
			return rotVecJsonElem.getAsJsonArray();
		}).orElseGet(() -> keyframeValue.isJsonArray() ? keyframeValue.getAsJsonArray() : null);
		
		String easingName = keyframeObj.map(keyframe -> {
			if (keyframe.has("easing")) {
				return keyframe.get("easing").getAsString();
			}
			if (keyframe.has("lerp_mode")) {
				return keyframe.get("lerp_mode").getAsString();
			}
			return null;
		}).orElse("linear");
		double[] easingArgs = keyframeObj.map(keyframe -> keyframe.get("easingArgs"))
				.map(JsonElement::getAsJsonArray)
				.map(json -> {
					return StreamSupport.stream(json.spliterator(), false)
					.mapToDouble(JsonElement::getAsDouble)
					.toArray();
				})
				.orElse(new double[0]);
		
		KeyframeQuery rotVec = KeyframeQuery.parseJsonVec(rotVecJson);
		Interpolation lerp = MoarInterpolations.getLerpMode(easingName, easingArgs);
		keyframesTimeline.put(time, rotVec.withKeyframe(time, lerp));
	}
	
	public static <T> T[] keyframesToArray(Float2ObjectMap<T> parsedTimeline, IntFunction<T[]> arrayConstructor) {
		return parsedTimeline.float2ObjectEntrySet().stream()
				.sorted(Comparator.comparingDouble(e -> e.getFloatKey()))
				.map(e -> e.getValue())
				.toArray(arrayConstructor);
	}
	
	
	private static AnimActionPhase parseAnimPhase(ActionPhase phase, Map<String, String> assignmentMap) {
		if (assignmentMap.containsKey("phase.loopBack")) {
			try {
				float loopBackTo = Float.parseFloat(assignmentMap.get("phase.loopBack"));
				assignmentMap.remove("phase.loopBack");
				return AnimActionPhase.loopBack(phase, loopBackTo);
			}
			catch (NumberFormatException e) {}
		}
		
		AnimActionPhase.Mode mode = AnimActionPhase.Mode.FIT_PHASE_LENGTH;
		if ("true".equals(assignmentMap.get("phase.constantLength"))) {
			mode = AnimActionPhase.Mode.CONSTANT_LENGTH;
		}
		return new AnimActionPhase(phase, mode);
	}

}
