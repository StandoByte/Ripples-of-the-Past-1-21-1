package com.github.standobyte.jojo.client.entityanim.pose;

import javax.annotation.Nullable;

public interface AnimatedEntity {
	void jojo_ripples$setModelPose(PoseType poseType, @Nullable AnimFramePose pose);
	@Nullable AnimFramePose jojo_ripples$getModelPose(PoseType poseType);
	
	public enum PoseType {
		FINAL, // The actual pose that is applied to the model
		UNMODIFIED // The pose without things like stand motion tilt, etc. Used for things like combo punch interpolation
	}
	
	boolean jojo_ripples$crouchDisabled();
}
