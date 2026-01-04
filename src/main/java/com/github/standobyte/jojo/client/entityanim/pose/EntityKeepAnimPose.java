package com.github.standobyte.jojo.client.entityanim.pose;

import javax.annotation.Nullable;

public interface EntityKeepAnimPose {
	void jojo_ripples$setKeepModelPose(boolean keepPose);
	boolean jojo_ripples$keepsModelPose();
	
	void jojo_ripples$preFrameRender();
	void jojo_ripples$keepModelPose(AnimFramePose pose);
	@Nullable AnimFramePose jojo_ripples$getModelPose();
}
