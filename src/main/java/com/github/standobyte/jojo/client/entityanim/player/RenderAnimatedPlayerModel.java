package com.github.standobyte.jojo.client.entityanim.player;

import java.util.List;

import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.ResourceModelEntry;
import com.github.standobyte.jojo.client.entityrender.replace_player_model.CustomPlayerModel;
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

// TODO (player animation) other layers
/* held items
 * cape
 * elytra
 * custom head
 * parrot on shoulder
 * arrows and bee stingers
 */
// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!! (bend) adjust the clothes models for the bends
public class RenderAnimatedPlayerModel {
	
	public static float getLimbHeight(ModelPart limb) {
		if (limb.cubes.isEmpty()) {
			return 12;
		}
		ModelPart.Cube cube = limb.cubes.get(0);
		return cube.maxY - cube.minY;
	}
	
	public static void beforePlayerAnim(HumanoidModel<?> playerModel) {
		/* This vanilla part is not referenced in playerAnimator format, so we just reset it, 
		 * in order to get rid of things like y rotation from the vanilla punch animation.
		 */
		playerModel.body.loadPose(playerModel.body.getInitialPose());
	}

	public static void renderWithBends(HumanoidModel<?> model, 
			HumanoidModelCubesBent cubesWithBends, ResourceModelEntry rig, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		Model_1_21_2plus rigModel = (Model_1_21_2plus) rig.getModel();
		ModelPart root = rigModel.jojo_ripples$root();
		
		recRenderChildren(model, cubesWithBends, 
				root, "root", 
				poseStack, buffer, packedLight, packedOverlay, color);
	}
	
	// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!! (player anim) 1st person render
	static void recRenderChildren(HumanoidModel<?> model, 
			HumanoidModelCubesBent cubesWithBends, ModelPart rigModelPart, String animPartName, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		boolean isBend = false;
		ModelPart vanillaModelPart = null;
		switch (animPartName) {
			case "left_arm" -> {
				vanillaModelPart = model.leftArm;
			}
			case "left_arm_bend" -> {
				vanillaModelPart = model.leftArm;
				isBend = true;
			}
			case "right_arm" -> {
				vanillaModelPart = model.rightArm;
			}
			case "right_arm_bend" -> {
				vanillaModelPart = model.rightArm;
				isBend = true;
			}
			case "left_leg" -> {
				vanillaModelPart = model.leftLeg;
			}
			case "left_leg_bend" -> {
				vanillaModelPart = model.leftLeg;
				isBend = true;
			}
			case "right_leg" -> {
				vanillaModelPart = model.rightLeg;
			}
			case "right_leg_bend" -> {
				vanillaModelPart = model.rightLeg;
				isBend = true;
			}
			case "torso_lower" -> {
				vanillaModelPart = model.body;
			}
			case "torso_bend" -> {
				vanillaModelPart = model.body;
				isBend = true;
			}
			case "head" -> {
				vanillaModelPart = model.head;
			}
		}
		
		poseStack.pushPose();
		rigModelPart.translateAndRotate(poseStack);
		
		if (vanillaModelPart != null) {
			_compileBentCubes(vanillaModelPart, animPartName, false, cubesWithBends, 
					poseStack, buffer, packedLight, packedOverlay, color);

			// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! (bend) head outer layer doesn't render
			// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!! (bend) armor helmet doesn't render
			ModelPart outerLayerJank = null;
			if (vanillaModelPart == model.leftArm) {
				outerLayerJank = model instanceof PlayerModel playerModel ? playerModel.leftSleeve : null;
			}
			else if (vanillaModelPart == model.rightArm) {
				outerLayerJank = model instanceof PlayerModel playerModel ? playerModel.rightSleeve : null;
			}
			else if (vanillaModelPart == model.leftLeg) {
				outerLayerJank = model instanceof PlayerModel playerModel ? playerModel.leftPants : null;
			}
			else if (vanillaModelPart == model.rightLeg) {
				outerLayerJank = model instanceof PlayerModel playerModel ? playerModel.rightPants : null;
			}
			else if (vanillaModelPart == model.hat) {
				outerLayerJank = model.hat;
			}
			else if (vanillaModelPart == model.body) {
				outerLayerJank = model instanceof PlayerModel playerModel ? playerModel.jacket : null;
			}
			
			if (outerLayerJank != null) {
				_compileBentCubes(outerLayerJank, animPartName, true, cubesWithBends, 
						poseStack, buffer, packedLight, packedOverlay, color);
			}
			
			if (!isBend) {
				for (ModelPart child : vanillaModelPart.children.values()) {
					child.render(poseStack, buffer, packedLight, packedOverlay, color);
				}
			}
		}
		
		
		if (vanillaModelPart == null || vanillaModelPart.visible) {
			for (var childEntry : rigModelPart.children.entrySet()) {
				recRenderChildren(model, 
						cubesWithBends, childEntry.getValue(), childEntry.getKey(), 
						poseStack, buffer, packedLight, packedOverlay, color);
			}
		}
		poseStack.popPose();
	}
	
	public static void _compileBentCubes(ModelPart modelPart, String name, boolean outerLayer, HumanoidModelCubesBent cubesWithBends, 
			PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		if (modelPart.visible && !modelPart.skipDraw) {
			List<ModelPart.Cube> cubes = cubesWithBends.getCubes(name, outerLayer);
			if (cubes != null) {
				for (ModelPart.Cube cube : cubes) {
					cube.compile(poseStack.last(), buffer, packedLight, packedOverlay, color);
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
