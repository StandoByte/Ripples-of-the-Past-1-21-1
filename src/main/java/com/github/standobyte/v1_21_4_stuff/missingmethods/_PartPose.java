package com.github.standobyte.v1_21_4_stuff.missingmethods;

import com.github.standobyte.v1_21_4_stuff.missingmethods.PartPoseScale.PartPoseWithScale;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public class _PartPose {
	public static PartPoseScale getScale(PartPose partPose) { return ((PartPoseWithScale) partPose).jojo_ripples$getScale(); }
	public static void setScale(PartPose partPose, float xScale, float yScale, float zScale) { ((PartPoseWithScale) partPose).jojo_ripples$setScale(xScale, yScale, zScale); }
	@Deprecated public static float xScale(PartPose partPose) { return getScale(partPose).xScale; }
	@Deprecated public static float yScale(PartPose partPose) { return getScale(partPose).yScale; }
	@Deprecated public static float zScale(PartPose partPose) { return getScale(partPose).zScale; }
	
	public static void resetScale(ModelPart modelPart) {
		PartPose initialPose = modelPart.getInitialPose();
		PartPoseScale initialScale = getScale(initialPose);
		modelPart.xScale = initialScale.xScale;
		modelPart.yScale = initialScale.yScale;
		modelPart.zScale = initialScale.zScale;
	}
}
