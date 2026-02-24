package com.github.standobyte.jojo.client.entityanim.pose;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import com.github.standobyte.v1_21_4_stuff.missingmethods._PartPose;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public class AnimFramePose {
	protected Map<String, ModelPartFrame> modelPartCache = new HashMap<>();
	public Map<String, ModelPartFrame> pose = new HashMap<>();
	
	public static AnimFramePose reused = new AnimFramePose();
	
	public ModelPartFrame getForModelPart(String modelPartName) {
		// FIXME ConcurrentModificationError on F3+T
		return this.pose.computeIfAbsent(modelPartName, __ -> modelPartCache
				.computeIfAbsent(modelPartName, ___ -> new ModelPartFrame()));
	}
	
	public ModelPartFrame getIfPresent(String modelPartName) {
		return this.pose.get(modelPartName);
	}
	
	
	public AnimFramePose clear() {
		for (ModelPartFrame obj : pose.values()) {
			obj.clear();
		}
		pose.clear();
		return this;
	}
	
	public void copyTo(AnimFramePose destPose) {
		destPose.clear();
		for (var modelPartPose : this.pose.entrySet()) {
			modelPartPose.getValue().copyTo(destPose.getForModelPart(modelPartPose.getKey()));
		}
	}
	
	public AnimFramePose deepCopy() {
		AnimFramePose copy = new AnimFramePose();
		for (var modelPartEntry : pose.entrySet()) {
			copy.pose.put(modelPartEntry.getKey(), modelPartEntry.getValue().deepCopy());
		}
		return copy;
	}
	
	
	public static class ModelPartFrame {
		public Vector3f positionOffset = new Vector3f();
		public Vector3f rotationOffset = new Vector3f();
		public Vector3f scaleOffset = new Vector3f();
		
		public void clear() {
			this.positionOffset.set(0, 0, 0);
			this.rotationOffset.set(0, 0, 0);
			this.scaleOffset.set(0, 0, 0);
		}
		
		public void apply(ModelPart modelPart) {
			if (!modelPart.visible) return;
			
			PartPose initialPose = modelPart.getInitialPose();
			modelPart.xRot = initialPose.xRot;
			modelPart.yRot = initialPose.yRot;
			modelPart.zRot = initialPose.zRot;
			modelPart.x = initialPose.x;
			modelPart.y = initialPose.y;
			modelPart.z = initialPose.z;
			modelPart.xScale = _PartPose.xScale(initialPose);
			modelPart.yScale = _PartPose.yScale(initialPose);
			modelPart.zScale = _PartPose.zScale(initialPose);
			
			modelPart.offsetPos(this.positionOffset);
			modelPart.offsetRotation(this.rotationOffset);
			modelPart.offsetScale(this.scaleOffset);
		}
		
		public void set(Vector3f value, AnimationChannel.Target target) {
			if (target == AnimationChannel.Targets.ROTATION)		this.rotationOffset.set(value);
			else if (target == AnimationChannel.Targets.POSITION)	this.positionOffset.set(value);
			else if (target == AnimationChannel.Targets.SCALE)		this.scaleOffset.set(value);
		}
		
		public Vector3f getForTarget(AnimationChannel.Target target) {
			if (target == AnimationChannel.Targets.ROTATION)		return rotationOffset;
			else if (target == AnimationChannel.Targets.POSITION) 	return positionOffset;
			else if (target == AnimationChannel.Targets.SCALE) 		return scaleOffset;
			throw new IllegalStateException();
		}
		
		public void copyTo(ModelPartFrame dest) {
			dest.positionOffset.set(this.positionOffset);
			dest.rotationOffset.set(this.rotationOffset);
			dest.scaleOffset.set(this.scaleOffset);
		}
		
		public ModelPartFrame deepCopy() {
			ModelPartFrame copy = new ModelPartFrame();
			copy.positionOffset.set(this.positionOffset);
			copy.rotationOffset.set(this.rotationOffset);
			copy.scaleOffset.set(this.scaleOffset);
			return copy;
		}
		
	}
}
