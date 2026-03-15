package com.github.standobyte.jojo.client.entityanim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.molang.animelement.AnimationChannelQuery;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.IAnimationChannel;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.KeyframeQuery;
import com.github.standobyte.jojo.util.functions.FloatUtils;

import net.minecraft.Util;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.world.entity.HumanoidArm;

public record AnimationMirror(HumanoidArm defaultSide, float start, float end) {
	
	public static void doMirroringOnAnimSet(Map<String, List<RotpAnimDefinition>> anims) {
		Map<String, List<RotpAnimDefinition>> toAdd = null;
		
		var animEntryIter = anims.entrySet().iterator();
		while (animEntryIter.hasNext()) {
			var animEntry = animEntryIter.next();
			List<RotpAnimDefinition> enumeratedAnims = animEntry.getValue();
			
			boolean hasMirrored = false;
			for (int i = 0; i < enumeratedAnims.size() && !hasMirrored; i++) {
				RotpAnimDefinition anim = enumeratedAnims.get(i);
				hasMirrored = anim.animationMirror != null;
			}
			
			if (hasMirrored) {
				String animName = animEntry.getKey();
				animEntryIter.remove();
				
				List<RotpAnimDefinition> leftSideAnims = new ArrayList<>(enumeratedAnims.size());
				List<RotpAnimDefinition> rightSideAnims = new ArrayList<>(enumeratedAnims.size());
				
				for (int i = 0; i < enumeratedAnims.size(); i++) {
					RotpAnimDefinition anim = enumeratedAnims.get(i);
					if (anim.animationMirror != null) {
						AnimationMirror mirroring = anim.animationMirror;
						switch (mirroring.defaultSide) {
							case LEFT -> {
								leftSideAnims.add(anim);
								rightSideAnims.add(anim.copyWithAnim(mirror(anim.boneAnimations, mirroring.start, mirroring.end)));
							}
							case RIGHT -> {
								leftSideAnims.add(anim.copyWithAnim(mirror(anim.boneAnimations, mirroring.start, mirroring.end)));
								rightSideAnims.add(anim);
							}
						}
					}
					else {
						leftSideAnims.add(anim);
						rightSideAnims.add(anim);
					}
				}
				
				if (toAdd == null) toAdd = new HashMap<>();
				toAdd.put(animName + "_left", leftSideAnims);
				toAdd.put(animName + "_right", rightSideAnims);
			}
		}
		
		if (toAdd != null) anims.putAll(toAdd);
	}


	public static Map<String, List<IAnimationChannel>> mirror(Map<String, List<IAnimationChannel>> keyframes, float startInclusive, float endInclusive) {
		Map<String, Map<AnimationChannel.Target, List<KeyframeQuery>>> mirrored = new HashMap<>();

		for (var modelPartEntry : keyframes.entrySet()) {
			String partName = modelPartEntry.getKey();
			String partNameOpposite = 
					partName.startsWith("left_") ? 
							"right_" + partName.substring(5) : 
					partName.startsWith("right_") ? 
							"left_" + partName.substring(6) : 
					partName;
			List<IAnimationChannel> originalKeyframes = modelPartEntry.getValue();
			
			for (IAnimationChannel _channel : originalKeyframes) {
				AnimationChannelQuery channel = (AnimationChannelQuery) _channel;
				AnimationChannel.Target channelTarget = channel.target();
				KeyframeQuery[] chKeyframes = channel.rotpKeyframes();
				for (KeyframeQuery keyframe : chKeyframes) {
					String mirroredModelPartName;
					KeyframeQuery mirroredKeyframe;
					
					float time = keyframe.getKeyframe().timestamp();
					if (time >= startInclusive && time <= endInclusive) {
						mirroredModelPartName = partNameOpposite;
						mirroredKeyframe = mirror(channelTarget, keyframe);
					}
					else {
						mirroredModelPartName = partName;
						mirroredKeyframe = keyframe;
					}
					
					mirrored.computeIfAbsent(mirroredModelPartName, __ -> new HashMap<>())
							.computeIfAbsent(channelTarget, ___ -> new ArrayList<>())
							.add(mirroredKeyframe);
				}
			}
			
		}
		
		Map<String, List<IAnimationChannel>> mirroredResult = mirrored.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey, 
				modelPartEntry -> modelPartEntry.getValue().entrySet().stream().map(
						channelEntry -> (IAnimationChannel) new AnimationChannelQuery(
								channelEntry.getKey(), 
								Util.make(
										channelEntry.getValue(), list -> 
										list.sort(FloatUtils.comparingDouble(keyframe -> keyframe.getKeyframe().timestamp())))
								.toArray(KeyframeQuery[]::new))).toList()));
		return mirroredResult;
	}

	public static KeyframeQuery mirror(AnimationChannel.Target target, KeyframeQuery keyframe) {
		Keyframe vanillaKeyframe = keyframe.getKeyframe();
		KeyframeQuery copy = keyframe.copy();
		Vector3f vec = mirrorVec(target, vanillaKeyframe.target());
		copy.getKeyframe().target().set(vec);
		return copy;
	}

	public static Vector3f mirrorVec(AnimationChannel.Target channelTarget, Vector3f vec) {
		if (channelTarget == AnimationChannel.Targets.ROTATION) {
			return new Vector3f(vec.x, -vec.y, -vec.z);
		}
		else if (channelTarget == AnimationChannel.Targets.POSITION) {
			return new Vector3f(-vec.x, vec.y, vec.z);
		}
		else {
			return new Vector3f(vec);
		}
	}

}
