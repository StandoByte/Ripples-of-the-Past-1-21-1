package com.github.standobyte.jojo.client.entityanim.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.UglyCrutchesClient;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.jojo.client.entityrender.entities.HumanoidLikeModel;
import com.github.standobyte.jojo.client.entityrender.replace_player_model.CustomPlayerModel;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
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
public class ModelRiggable {
	protected Map<String, List<AlternativeModelPart>> vanillaCounterparts = new HashMap<>();
	
	public static ModelRiggable createFromBase(HumanoidModel<?> model) {
		ModelRiggable obj = new ModelRiggable();
		obj.initHumanoid(model);
		return obj;
	}
	
	@Deprecated
	protected void __reinitialize(HumanoidModel<?> model) {
		vanillaCounterparts.clear();
		initHumanoid(model);
	}
	
	public void initHumanoid(HumanoidModel<?> model) {
		HumanoidLikeModel<?> asModModel = model instanceof HumanoidLikeModel __ ? __ : null;
		
		List<AlternativeModelPart> head = new ArrayList<>();
		this.vanillaCounterparts.put("head_rot", head);

		List<AlternativeModelPart> torso_lower = new ArrayList<>();
		List<AlternativeModelPart> torso_bend = new ArrayList<>();
		this.vanillaCounterparts.put("torso_lower", torso_lower);
		this.vanillaCounterparts.put("torso_bend", torso_bend);

		List<AlternativeModelPart> left_arm = new ArrayList<>();
		List<AlternativeModelPart> left_arm_bend = new ArrayList<>();
		List<AlternativeModelPart> left_arm_joint = new ArrayList<>();
		this.vanillaCounterparts.put("left_arm", left_arm);
		this.vanillaCounterparts.put("left_arm_bend", left_arm_bend);
		this.vanillaCounterparts.put("left_arm_joint", left_arm_joint);

		List<AlternativeModelPart> right_arm = new ArrayList<>();
		List<AlternativeModelPart> right_arm_bend = new ArrayList<>();
		List<AlternativeModelPart> right_arm_joint = new ArrayList<>();
		this.vanillaCounterparts.put("right_arm", right_arm);
		this.vanillaCounterparts.put("right_arm_bend", right_arm_bend);
		this.vanillaCounterparts.put("right_arm_joint", right_arm_joint);

		List<AlternativeModelPart> left_leg = new ArrayList<>();
		List<AlternativeModelPart> left_leg_bend = new ArrayList<>();
		List<AlternativeModelPart> left_leg_joint = new ArrayList<>();
		this.vanillaCounterparts.put("left_leg", left_leg);
		this.vanillaCounterparts.put("left_leg_bend", left_leg_bend);
		this.vanillaCounterparts.put("left_leg_joint", left_leg_joint);

		List<AlternativeModelPart> right_leg = new ArrayList<>();
		List<AlternativeModelPart> right_leg_bend = new ArrayList<>();
		List<AlternativeModelPart> right_leg_joint = new ArrayList<>();
		this.vanillaCounterparts.put("right_leg", right_leg);
		this.vanillaCounterparts.put("right_leg_bend", right_leg_bend);
		this.vanillaCounterparts.put("right_leg_joint", right_leg_joint);
		
		addPart(model.head, "head", head);
		addPart(model.hat, "head", head);
		
		if (asModModel != null) {
			addPart(asModModel.torso_lower, "torso_lower", torso_lower);
			addPart(asModModel.torso_no_arms, "torso_bend", torso_bend);
		}
		
		addPart(model.leftArm, "left_arm", left_arm);
		if (asModModel != null) {
			addPart(asModModel.left_arm_bend, "left_arm_bend", left_arm_bend);
		}
		
		addPart(model.rightArm, "right_arm", right_arm);
		if (asModModel != null) {
			addPart(asModModel.right_arm_bend, "right_arm_bend", right_arm_bend);
		}
		
		addPart(model.leftLeg, "left_leg", left_leg);
		if (asModModel != null) {
			addPart(asModModel.left_leg_bend, "left_leg_bend", left_leg_bend);
		}
		
		addPart(model.rightLeg, "right_leg", right_leg);
		if (asModModel != null) {
			addPart(asModModel.right_leg_bend, "right_leg_bend", right_leg_bend);
		}
	}
	
	protected static void addPart(ModelPart part, String name, List<AlternativeModelPart> dest) {
		AlternativeModelPart altPart = new AlternativeModelPart(part, part, name, 0);
		dest.add(altPart);
	}
	
	
	public void renderWithPosedRig(HumanoidModel<?> model, 
			Model rig, AnimFramePose animPose, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		Model_1_21_2plus rigModel = (Model_1_21_2plus) rig;
		ModelPart root = rigModel.jojo_ripples$root();
		renderModelPart(model, root, animPose, "root", 
				poseStack, buffer, packedLight, packedOverlay, color);
	}
	
	protected void renderModelPart(HumanoidModel<?> model, ModelPart rigModelPart, AnimFramePose animPose, String animPartName, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		poseStack.pushPose();
		rigModelPart.translateAndRotate(poseStack);
		UglyCrutchesClient.pushPlayerAnimPart(rigModelPart);
		
		List<AlternativeModelPart> vanillaToBend = this.vanillaCounterparts.get(animPartName);
		if (vanillaToBend != null) {
			for (AlternativeModelPart partEntry : vanillaToBend) {
				partEntry.render(animPose, poseStack, buffer, packedLight, packedOverlay, color);
			}
		}

		for (var childEntry : rigModelPart.children.entrySet()) {
			renderModelPart(model, childEntry.getValue(), animPose, childEntry.getKey(), 
					poseStack, buffer, packedLight, packedOverlay, color);
		}
		
		UglyCrutchesClient.popPlayerAnimPart();
		poseStack.popPose();
	}
	
	protected static record AlternativeModelPart(ModelPart fromVanilla, ModelPart part, List<AlternativeModelPart> children/*, String name*/, float yOffset) {
		protected AlternativeModelPart(ModelPart fromVanilla, ModelPart part, String name, float yOffset) { this(fromVanilla, part, new ArrayList<>(0)/*, name*/, yOffset); }
		
		protected void render(AnimFramePose animPose, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
			ModelPart vanillaModelPart = this.fromVanilla;
			if (vanillaModelPart != null && vanillaModelPart.visible) {
				ModelPart modelPart = this.part;
				if (!vanillaModelPart.skipDraw) {
					for (ModelPart.Cube cube : modelPart.cubes) {
						cube.compile(poseStack.last(), buffer, packedLight, packedOverlay, color);
					}
				}
				for (AlternativeModelPart child : this.children) {
					child.render(animPose, poseStack, buffer, packedLight, packedOverlay, color);
				}
				
				if (!modelPart.children.isEmpty()) {
					poseStack.pushPose();
					
					if (yOffset != 0) {
						poseStack.translate(0, yOffset / 16, 0);
					}
					
					for (var externalChildEntry : modelPart.children.entrySet()) {
						String childName = externalChildEntry.getKey();
						ModelPart child = externalChildEntry.getValue();
						ModelPartFrame modelPartAnim = animPose != null ? animPose.getIfPresent(childName) : null;
						if (modelPartAnim != null) {
							modelPartAnim.apply(child);
						}
						else {
							UglyCrutchesClient.adjustClothesInROTPAnim(childName, child);
						}
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
	
}
