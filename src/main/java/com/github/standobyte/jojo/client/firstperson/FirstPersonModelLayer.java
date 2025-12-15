package com.github.standobyte.jojo.client.firstperson;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

// TODO (1.16.5) render the layers in 1st person when the player is invisible
public interface FirstPersonModelLayer {
	void renderHandFirstPerson(HumanoidArm side, PoseStack poseStack, 
			MultiBufferSource buffer, int light, LivingEntity entity, 
			LivingEntityRenderer<?, ?> entityRenderer);

	static void defaultRender(HumanoidArm side, PoseStack poseStack, 
			MultiBufferSource buffer, int light, LivingEntity entity, 
			LivingEntityRenderer<?, ?> entityRenderer, 
			HumanoidModel<?> model, ResourceLocation texture) {
		defaultRender(side, poseStack, buffer, light, entity, entityRenderer, model, texture,
				0xFFFFFFFF);
	}

	static void defaultRender(HumanoidArm side, PoseStack poseStack, 
			MultiBufferSource buffer, int light, LivingEntity entity, 
			LivingEntityRenderer<?, ?> entityRenderer, 
			HumanoidModel<?> model, ResourceLocation texture,
			int color) {
		if (texture == null || entity.isSpectator()) return;
		setupForFirstPersonRender(model, entity);
		VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));

		ModelPart arm = getArm(model, side);
		arm.xRot = 0.0F;
		arm.render(poseStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, color);

		if (model instanceof PlayerModel playerModel) {
			ModelPart armOuter = getArmOuter(playerModel, side);
			armOuter.xRot = 0.0F;
			armOuter.render(poseStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, color);
		}
	}


	static ModelPart getArm(HumanoidModel<?> model, HumanoidArm side) {
		return side == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
	}

	static ModelPart getArmOuter(PlayerModel<?> model, HumanoidArm side) {
		return side == HumanoidArm.LEFT ? model.leftSleeve : model.rightSleeve;
	}

	@SuppressWarnings("unchecked")
	static <T extends LivingEntity> void setupForFirstPersonRender(HumanoidModel<T> model, LivingEntity entity) {
		model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		model.attackTime = 0.0F;
		model.crouching = false;
		model.swimAmount = 0.0F;
		model.setupAnim((T) entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
	}
}
