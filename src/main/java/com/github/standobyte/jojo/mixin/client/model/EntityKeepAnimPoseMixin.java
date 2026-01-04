package com.github.standobyte.jojo.mixin.client.model;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.EntityKeepAnimPose;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class EntityKeepAnimPoseMixin implements EntityKeepAnimPose {
	boolean jojo_ripples$keepModelPose = false;
	AnimFramePose jojo_ripples$modelPose = null;

	@Override
	public void jojo_ripples$setKeepModelPose(boolean keepPose) {
		this.jojo_ripples$keepModelPose = keepPose;
		if (!keepPose) {
			this.jojo_ripples$modelPose = null;
		}
	}
	
	@Override
	public boolean jojo_ripples$keepsModelPose() {
		return jojo_ripples$keepModelPose;
	}
	
	@Override
	public void jojo_ripples$preFrameRender() {
		this.jojo_ripples$modelPose = null;
	}

	@Override
	public void jojo_ripples$keepModelPose(AnimFramePose pose) {
		if (this.jojo_ripples$keepModelPose) {
			this.jojo_ripples$modelPose = pose.deepCopy();
		}
	}

	@Nullable
	@Override
	public AnimFramePose jojo_ripples$getModelPose() {
		return this.jojo_ripples$modelPose;
	}

}
