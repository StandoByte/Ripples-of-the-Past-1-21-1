package com.github.standobyte.v1_21_4_stuff.missingmethods;

public class PartPoseScale {
	public static final PartPoseScale DEFAULT = new PartPoseScale(1, 1, 1);
	public final float xScale;
	public final float yScale;
	public final float zScale;
	
	public PartPoseScale(float xScale, float yScale, float zScale) {
		this.xScale = xScale;
		this.yScale = yScale;
		this.zScale = zScale;
	}
	
	public static interface PartPoseWithScale {
		PartPoseScale jojo_ripples$getScale();
		void jojo_ripples$setScale(float xScale, float yScale, float zScale);
	}
	
}
