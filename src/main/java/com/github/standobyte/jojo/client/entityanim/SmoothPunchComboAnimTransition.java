package com.github.standobyte.jojo.client.entityanim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.molang.animelement.AnimationChannelQuery;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.IAnimationChannel;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.KeyframeQuery;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationChannel.Interpolations;

public class SmoothPunchComboAnimTransition {

	public static Map<String, List<IAnimationChannel>> transition(Map<String, List<IAnimationChannel>> boneAnimations, AnimFramePose prevPunchPose) {
//		// FIXME memory allocations, shit code
		if (prevPunchPose != null) {
			Map<String, List<IAnimationChannel>> anim = new HashMap<>();
			for (Map.Entry<String, List<IAnimationChannel>> modelPart : boneAnimations.entrySet()) {
				String partName = modelPart.getKey();
				List<IAnimationChannel> originalKeyframes = modelPart.getValue();
				
				ModelPartFrame partPose = prevPunchPose.getForModelPart(partName);
				if (partPose != null) {
					List<IAnimationChannel> withPose = new ArrayList<>();
					for (IAnimationChannel channel : originalKeyframes) {
						Vector3f offset = partPose.getForTarget(channel.target());
						
						offset = new Vector3f(offset);
						AnimationChannel.Target target = channel.target();
						if (target == AnimationChannel.Targets.ROTATION) {
							offset.mul(MathUtil.RAD_TO_DEG);
						}
						else if (target == AnimationChannel.Targets.POSITION) {
							offset.mul(1, -1, 1);
						}
						else if (target == AnimationChannel.Targets.SCALE) {
							offset.add(1, 1, 1);
						}
						
						AnimationChannelQuery _channel = (AnimationChannelQuery) channel;
						KeyframeQuery[] keyframes = _channel.rotpKeyframes();
						KeyframeQuery[] keyframes_;
						boolean replaceFirstKeyframe = keyframes.length > 1 && keyframes[0].getKeyframe().timestamp() == 0 && keyframes[0].isNumericLiteral();
						if (replaceFirstKeyframe) {
							keyframes_ = new KeyframeQuery[keyframes.length];
							keyframes_[0] = KeyframeQuery.constant(offset, 0, Interpolations.LINEAR);
							for (int i = 1; i < keyframes.length; i++) {
								keyframes_[0 + i] = keyframes[i];
							}
						}
						else {
							keyframes_ = new KeyframeQuery[keyframes.length + 1];
							keyframes_[0] = KeyframeQuery.constant(offset, 0, Interpolations.LINEAR);
							for (int i = 0; i < keyframes.length; i++) {
								keyframes_[1 + i] = keyframes[i];
							}
						}
						AnimationChannelQuery copy = new AnimationChannelQuery(_channel.target, keyframes_);
						withPose.add(copy);
					}
					anim.put(partName, withPose);
				}
				else {
					anim.put(partName, originalKeyframes);
				}
			}
			return anim;
		}
		return boneAnimations;
	}
}
