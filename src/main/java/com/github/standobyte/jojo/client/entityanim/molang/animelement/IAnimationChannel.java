package com.github.standobyte.jojo.client.entityanim.molang.animelement;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;

public interface IAnimationChannel {
	AnimationChannel.Target target();
	Keyframe[] keyframes();
}
