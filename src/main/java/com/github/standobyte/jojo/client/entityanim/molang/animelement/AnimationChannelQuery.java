package com.github.standobyte.jojo.client.entityanim.molang.animelement;

import java.util.Arrays;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationChannel.Target;
import net.minecraft.client.animation.Keyframe;

public class AnimationChannelQuery implements IAnimationChannel {
	public final AnimationChannel.Target target;
	private KeyframeQuery[] rotpKeyframes;
	private Keyframe[] vanillaKeyframes;

	public AnimationChannelQuery(AnimationChannel.Target target, KeyframeQuery... keyframes) {
		this.target = target;
		setKeyframes(keyframes);
	}

	protected void setKeyframes(KeyframeQuery[] keyframes) {
		this.rotpKeyframes = keyframes;
		this.vanillaKeyframes = toVanilla(keyframes);
	}

	public static Keyframe[] toVanilla(KeyframeQuery[] rotpKeyframes) {
		return Arrays.stream(rotpKeyframes).map(KeyframeQuery::getKeyframe).toArray(Keyframe[]::new);
	}

	@Override
	public Target target() {
		return target;
	}

	@Override
	public Keyframe[] keyframes() {
		return vanillaKeyframes;
	}

	public KeyframeQuery[] rotpKeyframes() {
		return rotpKeyframes;
	}
}
