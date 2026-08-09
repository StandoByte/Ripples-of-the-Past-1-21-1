package com.github.standobyte.jojo.client.entityanim.humanoid_bend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.UglyCrutchesClient;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityrender.HumanoidPlayerModel;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.jojo.client.entityrender.replace_player_model.CustomPlayerModel;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

// TODO (player animation) 1st person animation
// TODO (clothes player animation) rotate joints

// TODO (player animation) other layers
/* held items
 * cape
 * elytra
 * custom head
 * parrot on shoulder
 * arrows and bee stingers
 */
public class HumanoidModelPartsWithBends {
	protected Map<String, List<AlternativeModelPart>> vanillaCounterparts = new HashMap<>();
	protected Map<String, List<BendableLimb>> bendables = new HashMap<>();
	
	public static HumanoidModelPartsWithBends createFromBase(HumanoidModel<?> model) {
		HumanoidModelPartsWithBends obj = new HumanoidModelPartsWithBends();
		obj.initHumanoid(model);
		return obj;
	}
	
	@Deprecated
	void __reinitialize(HumanoidModel<?> model) {
		vanillaCounterparts.clear();
		bendables.clear();
		initHumanoid(model);
	}
	
	void initHumanoid(HumanoidModel<?> model) {
		PlayerModel<?> playerModel = model instanceof PlayerModel __ ? __ : null;
		HumanoidPlayerModel<?> clothesModel = model instanceof HumanoidPlayerModel __ ? __ : null;
		
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
		if (playerModel != null) {
			addBendPart(playerModel.jacket, "torso", torso, torso_lower, torso_bend, null, 0, 6, 0, -6, true);
		}
		
		addBendPart(model.leftArm, "left_arm", leftArm, left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		if (playerModel != null) {
			addBendPart(playerModel.leftSleeve, "left_arm", leftArm, left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		}
		else if (clothesModel != null) {
			addBendPart(clothesModel.leftArmSlim, "left_arm", leftArm, left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		}
		
		addBendPart(model.rightArm, "right_arm", rightArm, right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		if (playerModel != null) {
			addBendPart(playerModel.rightSleeve, "right_arm", rightArm, right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		}
		else if (clothesModel != null) {
			addBendPart(clothesModel.rightArmSlim, "right_arm", rightArm, right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		}
		
		addBendPart(model.leftLeg, "left_leg", leftLeg, left_leg, left_leg_bend, left_leg_joint, 0, 6, 0, 0, false);
		if (playerModel != null) {
			addBendPart(playerModel.leftPants, "left_leg", leftLeg, left_leg, left_leg_bend, left_leg_joint, 0, 6, 0, 0, false);
		}
		
		addBendPart(model.rightLeg, "right_leg", rightLeg, right_leg, right_leg_bend, right_leg_joint, 0, 6, 0, 0, false);
		if (playerModel != null) {
			addBendPart(playerModel.rightPants, "right_leg", rightLeg, right_leg, right_leg_bend, right_leg_joint, 0, 6, 0, 0, false);
		}
	}
	
	protected static void addPart(ModelPart part, String name, List<AlternativeModelPart> dest) {
		AlternativeModelPart altPart = new AlternativeModelPart(part, part, name, 0);
		dest.add(altPart);
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
						basePart.children, bendPart.children, jointPart != null ? jointPart.children : null, 
						x, y, z, yOffset, bendIsAbove);
			}
		}
	}
	
	
	public static void beforePlayerAnim(HumanoidModel<?> playerModel) {
		/* This vanilla part is not referenced in playerAnimator format, so we just reset it, 
		 * in order to get rid of things like y rotation from the vanilla punch animation.
		 */
		playerModel.body.loadPose(playerModel.body.getInitialPose());
	}

	public void renderWithBends(HumanoidModel<?> model, Model rig, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		Model_1_21_2plus rigModel = (Model_1_21_2plus) rig;
		ModelPart root = rigModel.jojo_ripples$root();

		for (var bendableEntry : bendables.entrySet()) {
			rigModel.jojo_ripples$getAnyDescendantWithName(bendableEntry.getKey())
			.ifPresent(animPart -> {
				float bend = animPart.xRot;
				List<BendableLimb> bendables = bendableEntry.getValue();
				for (BendableLimb bendable : bendables) {
					BendUtil.connectVertices(bendable.base(), bend, 
							bendable.x(), 
							bendable.y() + bendable.yOffset(), 
							bendable.z(), 
							false);
					
					BendUtil.connectVertices(bendable.bend(), bend, 
							0, 0, 0, true);
				}
			});
		}
		
		renderModelPart(model, root, "root", 
				poseStack, buffer, packedLight, packedOverlay, color);
	}
	
	protected void renderModelPart(HumanoidModel<?> model, ModelPart rigModelPart, String animPartName, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		poseStack.pushPose();
		rigModelPart.translateAndRotate(poseStack);
		UglyCrutchesClient.pushPlayerAnimPart(rigModelPart);
		
		List<AlternativeModelPart> vanillaToBend = this.vanillaCounterparts.get(animPartName);
		if (vanillaToBend != null) {
			for (AlternativeModelPart partEntry : vanillaToBend) {
				partEntry.render(poseStack, buffer, packedLight, packedOverlay, color);
			}
		}

		for (var childEntry : rigModelPart.children.entrySet()) {
			renderModelPart(model, childEntry.getValue(), childEntry.getKey(), 
					poseStack, buffer, packedLight, packedOverlay, color);
		}
		
		UglyCrutchesClient.popPlayerAnimPart();
		poseStack.popPose();
	}
	
	protected static record AlternativeModelPart(ModelPart fromVanilla, ModelPart part, List<AlternativeModelPart> children, String name, float yOffset) {
		protected AlternativeModelPart(ModelPart fromVanilla, ModelPart part, String name, float yOffset) { this(fromVanilla, part, new ArrayList<>(0), name, yOffset); }
		
		protected void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
			ModelPart vanillaModelPart = this.fromVanilla;
			if (vanillaModelPart.visible) {
				ModelPart modelPart = this.part;
				if (!vanillaModelPart.skipDraw) {
					for (ModelPart.Cube cube : modelPart.cubes) {
						cube.compile(poseStack.last(), buffer, packedLight, packedOverlay, color);
					}
				}
				for (AlternativeModelPart child : this.children) {
					child.render(poseStack, buffer, packedLight, packedOverlay, color);
				}
				
				if (!modelPart.children.isEmpty()) {
					poseStack.pushPose();
					
					if (yOffset != 0) {
						poseStack.translate(0, yOffset / 16, 0);
					}
					
					for (var rotatedVanillaChildEntry : modelPart.children.entrySet()) {
						String childName = rotatedVanillaChildEntry.getKey();
						ModelPart child = rotatedVanillaChildEntry.getValue();
						UglyCrutchesClient.adjustClothesInROTPAnim(childName, child);
						child.render(poseStack, buffer, packedLight, packedOverlay, color);
						child.resetPose();
					}
					poseStack.popPose();
				}
			}
		}
	}
	
	
	public static void translateToAnimHand(
			HumanoidModel<?> model, Model rig, 
			HumanoidArm side, PoseStack poseStack, boolean slim) {
		CustomPlayerModel.translateToItemHoldPos(side, poseStack, (ModelWithExtraFeatures) rig, slim ? -0.5f : 0);
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
