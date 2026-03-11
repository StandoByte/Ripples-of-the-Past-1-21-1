package com.github.standobyte.jojo.client.entityanim.gecko;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.action.AnimActionPhase;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.AnimationChannelQuery;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.KeyframeQuery;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationChannel.Interpolation;
import net.minecraft.world.entity.HumanoidArm;

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
				
				Map<Integer, List<String[]>> instructionsParsed = Streams.stream(instructions)
						.filter(json -> json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
						.map(JsonElement::getAsString)
						.map(instruction -> instruction.split("[ ]*=[ ]*"))
						.filter(assignment -> assignment.length > 0)
						.peek(assignment -> {
							int lastI = assignment.length - 1;
							while (assignment[lastI].endsWith(";")) {
								assignment[lastI] = assignment[lastI].substring(0, assignment[lastI].length() - 1);
							}
						})
						.collect(Collectors.groupingBy(instruction -> instruction.length));

				List<String> singleWordInstructions = instructionsParsed.getOrDefault(1, Collections.emptyList()).stream()
						.map(array -> array[0])
						.toList();
				Map<String, String> assignmentMap = instructionsParsed.getOrDefault(2, Collections.emptyList()).stream()
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
							builder.looping(Float.parseFloat(assignmentValue));
						}
						case "mirror.default" -> {
							HumanoidArm side = Enum.valueOf(HumanoidArm.class, assignmentValue);
							builder.mirrorDefaultSide = side;
						}
						default -> builder.addFieldValueKeyframe(field, assignmentValue, time);
					}
					
					assignmentMap.remove(assignment.getKey());
				}
				
				for (String singleWord : singleWordInstructions) {
					switch (singleWord) {
						case "coolPoseHere" -> {
							builder.addCoolPoseTimestamp(time);
						}
						case "mirror.start" -> {
							builder.mirrorStart = time;
						}
						case "mirror.end" -> {
							builder.mirrorEnd = time;
						}
					}
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
		anim.addAnimation(boneName, new AnimationChannelQuery(target, keyframeQueries));
	}
	
	private static void parseKeyframe(Float2ObjectMap<KeyframeQuery> keyframesTimeline, float time, JsonElement keyframeValue) {
		Optional<JsonObject> keyframeObj = keyframeValue.isJsonObject() ? Optional.of(keyframeValue.getAsJsonObject()) : Optional.empty();
		
		JsonArray rotVecJson = keyframeObj.map(keyframe -> {
			JsonElement rotVecJsonElem = keyframe.get("vector"); // Geckolib format
			if (rotVecJsonElem == null && keyframe.has("post")) { // Bedrock format
				rotVecJsonElem = keyframe.get("post"); // idgaf about pre and post keyframes, be normal
				if (rotVecJsonElem.isJsonObject()) {
					rotVecJsonElem = rotVecJsonElem.getAsJsonObject().get("vector");
				}
			}
			return rotVecJsonElem.getAsJsonArray();
		}).orElseGet(() -> keyframeValue.isJsonArray() ? keyframeValue.getAsJsonArray() : null);
		
		String easingName = keyframeObj.map(keyframe -> {
			if (keyframe.has("easing")) { // Geckolib format
				return keyframe.get("easing").getAsString();
			}
			if (keyframe.has("lerp_mode")) { // Bedrock format
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
		
		Interpolation lerp = MoarInterpolations.getLerpMode(easingName, easingArgs);
		KeyframeQuery rotVec = KeyframeQuery.parseJsonVec(rotVecJson, time, lerp);
		keyframesTimeline.put(time, rotVec);
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
			assignmentMap.remove("phase.constantLength");
		}
		return new AnimActionPhase(phase, mode);
	}

}
