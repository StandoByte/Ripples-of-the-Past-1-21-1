package com.github.standobyte.jojo.client.entityanim.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.player.BendUtil.LimbSplit;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityrender.HumanoidPlayerModel;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.ResourceModelEntry;
import com.github.standobyte.jojo.client.entityrender.replace_player_model.CustomPlayerModel;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

// TODO (player animation) fix model bends with smaller child cubes
// TODO (player animation) parent xrot bones for limbs
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
// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!! (player anim) 1st person render
public class HumanoidModelPartsWithBends {
	protected Map<String, List<AlternativeModelPart>> vanillaCounterparts = new HashMap<>();
	protected Map<String, ModelPart> otherParts;
	
	public static HumanoidModelPartsWithBends createFromBase(HumanoidModel<?> model) {
		HumanoidModelPartsWithBends obj = new HumanoidModelPartsWithBends();
		PlayerModel<?> playerModel = model instanceof PlayerModel __ ? __ : null;
		HumanoidPlayerModel<?> clothesModel = model instanceof HumanoidPlayerModel __ ? __ : null;
		
		List<AlternativeModelPart> head = new ArrayList<>();
		obj.vanillaCounterparts.put("head", head);
		
		List<AlternativeModelPart> torso_lower = new ArrayList<>();
		List<AlternativeModelPart> torso_bend = new ArrayList<>();
		obj.vanillaCounterparts.put("torso_lower", torso_lower);
		obj.vanillaCounterparts.put("torso_bend", torso_bend);
		
		List<AlternativeModelPart> left_arm = new ArrayList<>();
		List<AlternativeModelPart> left_arm_bend = new ArrayList<>();
		List<AlternativeModelPart> left_arm_joint = new ArrayList<>();
		obj.vanillaCounterparts.put("left_arm", left_arm);
		obj.vanillaCounterparts.put("left_arm_bend", left_arm_bend);
		obj.vanillaCounterparts.put("left_arm_joint", left_arm_joint);
		
		List<AlternativeModelPart> right_arm = new ArrayList<>();
		List<AlternativeModelPart> right_arm_bend = new ArrayList<>();
		List<AlternativeModelPart> right_arm_joint = new ArrayList<>();
		obj.vanillaCounterparts.put("right_arm", right_arm);
		obj.vanillaCounterparts.put("right_arm_bend", right_arm_bend);
		obj.vanillaCounterparts.put("right_arm_joint", right_arm_joint);
		
		List<AlternativeModelPart> left_leg = new ArrayList<>();
		List<AlternativeModelPart> left_leg_bend = new ArrayList<>();
		List<AlternativeModelPart> left_leg_joint = new ArrayList<>();
		obj.vanillaCounterparts.put("left_leg", left_leg);
		obj.vanillaCounterparts.put("left_leg_bend", left_leg_bend);
		obj.vanillaCounterparts.put("left_leg_joint", left_leg_joint);
		
		List<AlternativeModelPart> right_leg = new ArrayList<>();
		List<AlternativeModelPart> right_leg_bend = new ArrayList<>();
		List<AlternativeModelPart> right_leg_joint = new ArrayList<>();
		obj.vanillaCounterparts.put("right_leg", right_leg);
		obj.vanillaCounterparts.put("right_leg_bend", right_leg_bend);
		obj.vanillaCounterparts.put("right_leg_joint", right_leg_joint);
		
		addPart(model.head, "head", head);
		
		addBendPart(model.body, "body", torso_lower, torso_bend, null, 0, 6, 0, -6, true);
		if (playerModel != null) {
			addBendPart(playerModel.jacket, "body2", torso_lower, torso_bend, null, 0, 6, 0, -6, true);
		}
		
		addBendPart(model.leftArm, "left_arm", left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		if (playerModel != null) {
			addBendPart(playerModel.leftSleeve, "left_arm2", left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		}
		else if (clothesModel != null) {
			addBendPart(clothesModel.leftArmSlim, "left_arm_slim", left_arm, left_arm_bend, left_arm_joint, -1, 4, 0, 0, false);
		}
		
		addBendPart(model.rightArm, "right_arm", right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		if (playerModel != null) {
			addBendPart(playerModel.rightSleeve, "right_arm2", right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		}
		else if (clothesModel != null) {
			addBendPart(clothesModel.rightArmSlim, "right_arm_slim", right_arm, right_arm_bend, right_arm_joint, 1, 4, 0, 0, false);
		}
		
		addBendPart(model.leftLeg, "left_leg", left_leg, left_leg_bend, left_leg_joint, 0, 6, 0, 0, false);
		if (playerModel != null) {
			addBendPart(playerModel.leftPants, "left_leg2", left_leg, left_leg_bend, left_leg_joint, 0, 6, 0, 0, false);
		}
		
		addBendPart(model.rightLeg, "right_leg", right_leg, right_leg_bend, right_leg_joint, 0, 6, 0, 0, false);
		if (playerModel != null) {
			addBendPart(playerModel.rightPants, "right_leg2", right_leg, right_leg_bend, right_leg_joint, 0, 6, 0, 0, false);
		}
		
		return obj;
	}
	
	protected static void addPart(ModelPart part, String name, List<AlternativeModelPart> dest) {
		AlternativeModelPart altPart = new AlternativeModelPart(part, part, name);
		dest.add(altPart);
	}
	
	protected static void addBendPart(ModelPart part, String name, 
			List<AlternativeModelPart> baseDest, 
			List<AlternativeModelPart> bendDest, 
			@Nullable List<AlternativeModelPart> jointDest, 
			float x, float y, float z, float yOffset, boolean bendIsAbove) {
		LimbSplit split = BendUtil.split(part, x, y, z, yOffset, bendIsAbove);
		
		AlternativeModelPart basePart = new AlternativeModelPart(part, split.base().makePart(), name);
		AlternativeModelPart bendPart = new AlternativeModelPart(part, split.bend().makePart(), name);
		AlternativeModelPart jointPart = null;
		
		baseDest.add(basePart);
		bendDest.add(bendPart);
		if (jointDest != null && !split.joint().isEmpty()) {
			jointPart = new AlternativeModelPart(part, new ModelPart(Collections.emptyList(), split.joint()), name);
			jointDest.add(jointPart);
		}
		
		for (var childEntry : part.children.entrySet()) {
			ModelPart child = childEntry.getValue();
			if (BendUtil.isSamePivotAsParent(child)) {
				addBendPart(child, childEntry.getKey(), 
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

	public void renderWithBends(HumanoidModel<?> model, ResourceModelEntry rig, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		Model_1_21_2plus rigModel = (Model_1_21_2plus) rig.getModel();
		ModelPart root = rigModel.jojo_ripples$root();
		
		renderModelPart(model, root, "root", 
				poseStack, buffer, packedLight, packedOverlay, color);
	}
	
	protected void renderModelPart(HumanoidModel<?> model, ModelPart rigModelPart, String animPartName, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		poseStack.pushPose();
		rigModelPart.translateAndRotate(poseStack);
		
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
		
		poseStack.popPose();
	}
	
	protected static record AlternativeModelPart(ModelPart fromVanilla, ModelPart part, List<AlternativeModelPart> children, String name) {
		protected AlternativeModelPart(ModelPart fromVanilla, ModelPart part, String name) { this(fromVanilla, part, new ArrayList<>(0), name); }
		
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
					
					// FIXME (clothes player animation) incorrect position of rotated torso parts
					/* Jotaro's belts, coat, chain
					 */
					boolean isTorsoPart = !this.name.contains("head");
					if (isTorsoPart) {
						poseStack.translate(0, -0.375, 0);
					}
					
					for (ModelPart rotatedVanillaChild : modelPart.children.values()) {
						rotatedVanillaChild.render(poseStack, buffer, packedLight, packedOverlay, color);
					}
					poseStack.popPose();
				}
			}
		}
	}
	
	
	public static void translateToAnimHand(
			HumanoidModel<?> model, ResourceModelEntry rig, 
			HumanoidArm side, PoseStack poseStack, boolean slim) {
		CustomPlayerModel.translateToItemHoldPos(side, poseStack, (ModelWithExtraFeatures) rig.getModel(), slim ? -0.5f : 0);
	}
	
	
	static String[] LIMB_BENDS = new String[] { "left_arm_bend", "right_arm_bend", "left_leg_bend", "right_leg_bend" };
	public static void adjustComplexBends(AnimFramePose pose) {
		for (String limbBend : LIMB_BENDS) {
			ModelPartFrame bonePose = pose.getIfPresent(limbBend);
			if (bonePose != null) {
				bonePose.rotationOffset.set(bonePose.rotationOffset.x, 0, 0);
			}
		}
		ModelPartFrame torsoBendPose = pose.getIfPresent("torso_bend");
		if (torsoBendPose != null) {
			float yRot = torsoBendPose.rotationOffset.y;
			torsoBendPose.rotationOffset.set(torsoBendPose.rotationOffset.x, 0, 0);
			ModelPartFrame parent = pose.getIfPresent("waist");
			if (parent != null) {
				parent.rotationOffset.add(0, yRot, 0);
			}
		}
	}
	
}
