package com.github.standobyte.jojo.client.entityrender;

import java.util.Collections;
import java.util.HashMap;

import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;

public class CustomPlayerModel extends PlayerModel {
	public ModelPart root;

	public CustomPlayerModel(ModelPart root, boolean slim) {
		super(root, slim);
	}
	
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		root.render(poseStack, buffer, packedLight, packedOverlay, color);
	}

	
	public static ModelPart DUMMY_ROOT = Util.make(new ModelPart(Collections.emptyList(), new HashMap<>()), root -> {
		root.children.put("head", new PartDefinition(Collections.emptyList(), PartPose.ZERO).bake(64, 64));
		root.children.put("hat", new PartDefinition(Collections.emptyList(), PartPose.ZERO).bake(64, 64));
		root.children.put("body", new PartDefinition(Collections.emptyList(), PartPose.ZERO).bake(64, 64));
		root.children.put("left_arm", new PartDefinition(Collections.emptyList(), PartPose.offset(5.0F, 2.0F, 0.0F)).bake(64, 64));
		root.children.put("right_arm", new PartDefinition(Collections.emptyList(), PartPose.offset(-5.0F, 2.0F, 0.0F)).bake(64, 64));
		root.children.put("left_leg", new PartDefinition(Collections.emptyList(), PartPose.offset(1.9F, 12.0F, 0.0F)).bake(64, 64));
		root.children.put("right_leg", new PartDefinition(Collections.emptyList(), PartPose.offset(-1.9F, 12.0F, 0.0F)).bake(64, 64));
		
		root.children.put("left_sleeve", new PartDefinition(Collections.emptyList(), PartPose.offset(5.0F, 2.0F, 0.0F)).bake(64, 64));
		root.children.put("right_sleeve", new PartDefinition(Collections.emptyList(), PartPose.offset(-5.0F, 2.0F, 0.0F)).bake(64, 64));
		root.children.put("left_pants", new PartDefinition(Collections.emptyList(), PartPose.offset(1.9F, 12.0F, 0.0F)).bake(64, 64));
		root.children.put("right_pants", new PartDefinition(Collections.emptyList(), PartPose.offset(-1.9F, 12.0F, 0.0F)).bake(64, 64));
		root.children.put("jacket", new PartDefinition(Collections.emptyList(), PartPose.ZERO).bake(64, 64));
		root.children.put("cloak", new PartDefinition(Collections.emptyList(), PartPose.ZERO).bake(64, 64));
		root.children.put("ear", new PartDefinition(Collections.emptyList(), PartPose.ZERO).bake(64, 64));
	});

	@SuppressWarnings("unchecked")
	public static PlayerModel createModel(ModelPart customRoot, boolean slim) {
		CustomPlayerModel model = new CustomPlayerModel(DUMMY_ROOT, slim);
		Model_1_21_2plus modelThatDoesntSuck = ((Model_1_21_2plus) model);
		modelThatDoesntSuck.jojo_ripples$initRoot(customRoot);
		model.root = modelThatDoesntSuck.jojo_ripples$root();
		
	    model.head = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "head");
	    model.hat = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "hat");
	    model.body = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "body");
	    model.leftArm = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "left_arm");
	    model.rightArm = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "right_arm");
	    model.leftLeg = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "left_leg");
	    model.rightLeg = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "right_leg");
	    
	    model.leftSleeve = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "left_sleeve");
	    model.rightSleeve = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "right_sleeve");
	    model.leftPants = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "left_pants");
	    model.rightPants = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "right_pants");
	    model.jacket = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "jacket");
	    model.cloak = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "cloak");
	    model.ear = findAnyDescendantsOrMakePlaceholder(modelThatDoesntSuck, "ear");
	    return model;
	}
	
	public static ModelPart findAnyDescendantsOrMakePlaceholder(Model_1_21_2plus model, String partName) {
		return model.jojo_ripples$getAnyDescendantWithName(partName)
				.orElseGet(() -> DUMMY_ROOT.children.get(partName));
	}


	@Override
	public void translateToHand(HumanoidArm side, PoseStack poseStack) {
		translateToItemHoldPos(side, poseStack, (ModelWithExtraFeatures) this);
	}
	
	public static void translateToItemHoldPos(HumanoidArm side, PoseStack poseStack, ModelWithExtraFeatures model) {
		var modelParts = switch (side) {
			case LEFT -> model.jojo_ripples$getPathToModelPart("left_item");
			case RIGHT -> model.jojo_ripples$getPathToModelPart("right_item");
		};
		if (modelParts != null) {
			for (ModelPartWithName part : modelParts) {
				part.part().translateAndRotate(poseStack);
			}
			// counteract the vanilla transforms hardcoded in ItemInHandLayer
			poseStack.translate((float)(side == HumanoidArm.LEFT ? -1 : 1) / 16.0F, -0.5F, 0.125F);
		}
	}
}
