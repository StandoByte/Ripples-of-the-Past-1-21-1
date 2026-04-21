package com.github.standobyte.jojo.mixin.client.v1_21_4_stuff;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.v1_21_4_stuff.missingmethods.PartPoseScale;
import com.github.standobyte.v1_21_4_stuff.missingmethods.PartPoseScale.PartPoseWithScale;

import net.minecraft.client.model.geom.PartPose;

@Mixin(PartPose.class)
public class PartPoseScaleMixin implements PartPoseWithScale {
	protected PartPoseScale scale = PartPoseScale.DEFAULT;

	@Override
	public PartPoseScale jojo_ripples$getScale() {
		return scale;
	}

	@Override
	public void jojo_ripples$setScale(float xScale, float yScale, float zScale) {
		this.scale = xScale != 1 || yScale != 1 || zScale != 1 ? 
				new PartPoseScale(xScale, yScale, zScale) : PartPoseScale.DEFAULT;
	}

}
