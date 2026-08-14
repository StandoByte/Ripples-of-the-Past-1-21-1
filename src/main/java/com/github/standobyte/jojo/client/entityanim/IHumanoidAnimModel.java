package com.github.standobyte.jojo.client.entityanim;

import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IHumanoidAnimModel {
	void jojo_rippes$initDisableBends();
	
	boolean jojo_rippes$isPlayingAnimation();
	void jojo_ripples$setupHumanoidAnim(HumanoidRenderState renderState);
	void jojo_ripples$setupHumanoidPose(AnimFramePose pose);
	boolean jojo_ripples$renderWithRig(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color);
}
