package com.github.standobyte.jojo.client.entityanim;

import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;

public interface IHumanoidAnimModel {
	public boolean jojo_rippes$isPlayingAnimation();
	public void jojo_ripples$setupHumanoidAnim(HumanoidRenderState renderState);
	public void jojo_ripples$setupHumanoidPose(AnimFramePose pose);
}
