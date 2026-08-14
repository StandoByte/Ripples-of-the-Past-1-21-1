package com.github.standobyte.jojo.client.entityanim.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.humanoid_bend.BendUtil;
import com.github.standobyte.jojo.client.entityanim.humanoid_bend.BendableLimb;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityrender.HumanoidPlayerModel;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelRiggableWithBends extends ModelRiggable {
	protected Map<String, List<BendableLimb>> bendables = new HashMap<>();
	
	@Override
	public void initHumanoid(HumanoidModel<?> model) {
		PlayerModel<?> asPlayerModel = model instanceof PlayerModel __ ? __ : null;
		HumanoidPlayerModel<?> asClothesModel = model instanceof HumanoidPlayerModel __ ? __ : null;
		
		List<AlternativeModelPart> head = new ArrayList<>();
		this.vanillaCounterparts.put("head_rot", head);

		List<BendableLimb> torso = new ArrayList<>();
		List<AlternativeModelPart> torso_lower = new ArrayList<>();
		List<AlternativeModelPart> torso_bend = new ArrayList<>();
		this.vanillaCounterparts.put("torso_lower", torso_lower);
		this.vanillaCounterparts.put("torso_bend", torso_bend);
		this.bendables.put("torso_bend", torso);

		List<BendableLimb> leftArm = new ArrayList<>();
		List<AlternativeModelPart> left_arm = new ArrayList<>();
		List<AlternativeModelPart> left_arm_bend = new ArrayList<>();
		List<AlternativeModelPart> left_arm_joint = new ArrayList<>();
		this.vanillaCounterparts.put("left_arm", left_arm);
		this.vanillaCounterparts.put("left_arm_bend", left_arm_bend);
		this.vanillaCounterparts.put("left_arm_joint", left_arm_joint);
		this.bendables.put("left_arm_bend", leftArm);

		List<BendableLimb> rightArm = new ArrayList<>();
		List<AlternativeModelPart> right_arm = new ArrayList<>();
		List<AlternativeModelPart> right_arm_bend = new ArrayList<>();
		List<AlternativeModelPart> right_arm_joint = new ArrayList<>();
		this.vanillaCounterparts.put("right_arm", right_arm);
		this.vanillaCounterparts.put("right_arm_bend", right_arm_bend);
		this.vanillaCounterparts.put("right_arm_joint", right_arm_joint);
		this.bendables.put("right_arm_bend", rightArm);

		List<BendableLimb> leftLeg = new ArrayList<>();
		List<AlternativeModelPart> left_leg = new ArrayList<>();
		List<AlternativeModelPart> left_leg_bend = new ArrayList<>();
		List<AlternativeModelPart> left_leg_joint = new ArrayList<>();
		this.vanillaCounterparts.put("left_leg", left_leg);
		this.vanillaCounterparts.put("left_leg_bend", left_leg_bend);
		this.vanillaCounterparts.put("left_leg_joint", left_leg_joint);
		this.bendables.put("left_leg_bend", leftLeg);

		List<BendableLimb> rightLeg = new ArrayList<>();
		List<AlternativeModelPart> right_leg = new ArrayList<>();
		List<AlternativeModelPart> right_leg_bend = new ArrayList<>();
		List<AlternativeModelPart> right_leg_joint = new ArrayList<>();
		this.vanillaCounterparts.put("right_leg", right_leg);
		this.vanillaCounterparts.put("right_leg_bend", right_leg_bend);
		this.vanillaCounterparts.put("right_leg_joint", right_leg_joint);
		this.bendables.put("right_leg_bend", rightLeg);
		
		addPart(model.head, "head", head);
		addPart(model.hat, "head", head);
		
		addBendPart(model.body, "torso", torso, torso_lower, torso_bend, null, 0, 6, 0, -6, true);
		if (asPlayerModel != null) {
			addBendPart(asPlayerModel.jacket, "torso", torso, torso_lower, torso_bend, null, 0, 6, 0, -6, true);
		}
		
		addBendPart(model.leftArm, "left_arm", leftArm, left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		if (asPlayerModel != null) {
			addBendPart(asPlayerModel.leftSleeve, "left_arm", leftArm, left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		}
		else if (asClothesModel != null) {
			addBendPart(asClothesModel.leftArmSlim, "left_arm", leftArm, left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		}
		
		addBendPart(model.rightArm, "right_arm", rightArm, right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		if (asPlayerModel != null) {
			addBendPart(asPlayerModel.rightSleeve, "right_arm", rightArm, right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		}
		else if (asClothesModel != null) {
			addBendPart(asClothesModel.rightArmSlim, "right_arm", rightArm, right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		}
		
		addBendPart(model.leftLeg, "left_leg", leftLeg, left_leg, left_leg_bend, left_leg_joint, 0, 6, 0, 0, false);
		if (asPlayerModel != null) {
			addBendPart(asPlayerModel.leftPants, "left_leg", leftLeg, left_leg, left_leg_bend, left_leg_joint, 0, 6, 0, 0, false);
		}
		
		addBendPart(model.rightLeg, "right_leg", rightLeg, right_leg, right_leg_bend, right_leg_joint, 0, 6, 0, 0, false);
		if (asPlayerModel != null) {
			addBendPart(asPlayerModel.rightPants, "right_leg", rightLeg, right_leg, right_leg_bend, right_leg_joint, 0, 6, 0, 0, false);
		}
	}
	
	protected static void addBendPart(ModelPart part, String name, 
			List<BendableLimb> bendables, 
			List<AlternativeModelPart> baseDest, 
			List<AlternativeModelPart> bendDest, 
			@Nullable List<AlternativeModelPart> jointDest, 
			float x, float y, float z, float yOffset, boolean bendIsAbove) {
		BendableLimb bendable = BendableLimb.create(part, x, y, z, yOffset, bendIsAbove);
		bendables.add(bendable);
		
		AlternativeModelPart basePart = new AlternativeModelPart(part, bendable.base().makePart(), name, yOffset);
		AlternativeModelPart bendPart = new AlternativeModelPart(part, bendable.bend().makePart(), name + "_bend", yOffset);
		AlternativeModelPart jointPart = null;
		
		baseDest.add(basePart);
		bendDest.add(bendPart);
		if (jointDest != null && !bendable.joint().isEmpty()) {
			jointPart = new AlternativeModelPart(part, new ModelPart(Collections.emptyList(), bendable.joint()), name + "_joint", yOffset);
			jointDest.add(jointPart);
		}
		
		for (var childEntry : part.children.entrySet()) {
			ModelPart child = childEntry.getValue();
			if (BendUtil.isSamePivotAsParent(child)) {
				addBendPart(child, childEntry.getKey(), bendables, 
						basePart.children(), bendPart.children(), jointPart != null ? jointPart.children() : null, 
						x, y, z, yOffset, bendIsAbove);
			}
		}
	}
	
	@Deprecated
	@Override
	protected void __reinitialize(HumanoidModel<?> model) {
		bendables.clear();
		super.__reinitialize(model);
	}
	
	
	public void renderWithPosedRig(HumanoidModel<?> model, 
			Model rig, AnimFramePose pose, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		setupBends(bendables, (Model_1_21_2plus) rig);
		super.renderWithPosedRig(model, 
				rig, pose, 
				poseStack, buffer, packedLight, packedOverlay, color);
	}
	
	public static void setupBends(Map<String, List<BendableLimb>> bendables, Model_1_21_2plus rigModel) {
		for (var bendableEntry : bendables.entrySet()) {
			List<BendableLimb> partBendables = bendableEntry.getValue();
			float bend = rigModel.jojo_ripples$getAnyDescendantWithName(bendableEntry.getKey()).map(animPart -> animPart.xRot).orElse(0f);
			
			for (BendableLimb bendable : partBendables) {
				BendUtil.connectVertices(bendable.base(), bend, 
						bendable.x(), 
						bendable.y(), 
						bendable.z(), 
						false);
				
				BendUtil.connectVertices(bendable.bend(), bend, 
						0, 0, 0, true);
			}
		}
	}
	
	static String[] LIMB_BENDS = new String[] { "left_arm_bend", "right_arm_bend", "left_leg_bend", "right_leg_bend" };
	public static boolean adjustComplexBends(AnimFramePose pose) {
		boolean ret = false;
		
		for (String limbBend : LIMB_BENDS) {
			ModelPartFrame bonePose = pose.getIfPresent(limbBend);
			if (bonePose != null) {
				Vector3f rotation = bonePose.rotationOffset;
				if (rotation.y != 0 || rotation.z != 0) {
					ret = true;
					rotation.set(rotation.x, 0, 0);
				}
			}
		}
		
		ModelPartFrame torsoBendPose = pose.getIfPresent("torso_bend");
		if (torsoBendPose != null) {
			Vector3f rotation = torsoBendPose.rotationOffset;
			if (rotation.y != 0 || rotation.z != 0) {
				ret = true;
				if (rotation.y != 0) {
					ModelPartFrame parent = pose.getIfPresent("waist");
					if (parent != null) {
						parent.rotationOffset.add(0, rotation.y, 0);
					}
				}
				rotation.set(rotation.x, 0, 0);
			}
		}
		
		return ret;
	}
}
