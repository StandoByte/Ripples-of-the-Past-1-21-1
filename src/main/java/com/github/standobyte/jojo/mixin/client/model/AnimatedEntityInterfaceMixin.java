package com.github.standobyte.jojo.mixin.client.model;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimatedEntity;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class AnimatedEntityInterfaceMixin implements AnimatedEntity {
	@Unique private AnimFramePose modelPose;
	@Unique private boolean hasPose = false;

	@Override
	public void jojo_ripples$setModelPose(PoseType poseType, AnimFramePose srcPose) {
		this.hasPose = srcPose != null;
		if (srcPose != null) {
			if (this.modelPose == null) {
				this.modelPose = new AnimFramePose();
			}
			srcPose.copyTo(this.modelPose);
		}
		else if (this.modelPose != null) {
			this.modelPose.clear();
		}
	}

	@Nullable
	@Override
	public AnimFramePose jojo_ripples$getModelPose(PoseType poseType) {
		return hasPose ? this.modelPose : null;
	}
	
	
	@Override
	public boolean jojo_ripples$crouchDisabled() {
		return hasPose;
	}

}
