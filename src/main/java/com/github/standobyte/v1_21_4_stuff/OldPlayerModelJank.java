package com.github.standobyte.v1_21_4_stuff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Use to set up and render the model parts of a humanoid model that comprise the outer skin layer.
 * On this version they are still children of root, 
 * but in 1.21.2+ they are changed (about time!) to being children of the respective inner layer model parts.
 * E.g. leftSleeve will be a child of leftArm, but on 1.21.1 it is still a child of root, so it needs to be rendered separately.
 * In a port to newer versions, all these function calls will be redundant.
 */
public class OldPlayerModelJank {
	
	public static void _onAnimate(HumanoidModel<?> model) {
		if (model instanceof PlayerModel playerModel) {
			if (playerModel.jacket != null) 
				playerModel.jacket.copyFrom(playerModel.body);
			if (playerModel.rightSleeve != null) 
				playerModel.rightSleeve.copyFrom(playerModel.rightArm);
			if (playerModel.leftSleeve != null) 
				playerModel.leftSleeve.copyFrom(playerModel.leftArm);
			if (playerModel.rightPants != null) 
				playerModel.rightPants.copyFrom(playerModel.rightLeg);
			if (playerModel.leftPants != null) 
				playerModel.leftPants.copyFrom(playerModel.leftLeg);
		}
	}

	public static void _renderOuterLayer(ModelPart modelPart, PoseStack poseStack, 
			VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		modelPart.render(poseStack, buffer, packedLight, packedOverlay, color);
	}
	
}
