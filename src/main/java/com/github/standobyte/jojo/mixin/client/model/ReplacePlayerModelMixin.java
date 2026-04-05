package com.github.standobyte.jojo.mixin.client.model;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.entityrender.ReplacePlayerModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@SuppressWarnings({ "rawtypes" })
@Mixin(PlayerRenderer.class)
public abstract class ReplacePlayerModelMixin extends ReplaceLivingModelMixin {
	@Unique protected EntityModel prevModel;
	
	
	@Unique protected void setReplacementModel(Entity entity) {
		if (this.prevModel == null) {
			PlayerModel replacementModel = ReplacePlayerModel.getModel(entity);
			if (replacementModel != null) {
				this.prevModel = this.model;
				this.model = replacementModel;
			}
		}
	}
	
	@Unique protected void restoreModel() {
		if (this.prevModel != null) {
			this.model = this.prevModel;
			this.prevModel = null;
		}
	}
	
	@Override
	protected boolean isUsingCustomModel() {
		return prevModel != null;
	}


	@Inject(method = "render", at = @At("HEAD"))
	public void replaceModel(AbstractClientPlayer entity, float entityYaw, float partialTicks, 
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		setReplacementModel(entity);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void restoreModel(AbstractClientPlayer entity, float entityYaw, float partialTicks, 
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		restoreModel();
	}


	@Inject(method = "renderRightHand", at = @At("HEAD"))
	public void replaceModel1stPersonR(PoseStack poseStack, MultiBufferSource buffer, 
			int combinedLight, AbstractClientPlayer entity, CallbackInfo ci) {
		setReplacementModel(entity);
	}

	@Inject(method = "renderLeftHand", at = @At("HEAD"))
	public void replaceModel1stPersonL(PoseStack poseStack, MultiBufferSource buffer, 
			int combinedLight, AbstractClientPlayer entity, CallbackInfo ci) {
		setReplacementModel(entity);
	}

	@Inject(method = "renderHand", at = @At("TAIL"))
	public void restoreModel1stPerson(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, 
			AbstractClientPlayer entity, ModelPart rendererArm, ModelPart rendererArmwear, CallbackInfo ci) {
		restoreModel();
	}
	
	
	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	public void replacePlayerTexture(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> ci) {
		ResourceLocation replacementTexture = ReplacePlayerModel.getTexture(entity);
		if (replacementTexture != null) {
			ci.setReturnValue(replacementTexture);
		}
	}

	// TODO custom hand render in 1st person (to fix wrong rotation angle, and entitySolid render type for inner layer causing black pixels)
	// you'd think they would call getTextureLocation, but they forgor (it's 60 LOC above in the same file)
	@ModifyVariable(method = "renderHand", at = @At("STORE"), ordinal = 0)
	public ResourceLocation replacePlayerTexture1stPersonRender(ResourceLocation vanillaTexture, 
			PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer entity,
			ModelPart rendererArm, ModelPart rendererArmwear) {
		ResourceLocation replacementTexture = ReplacePlayerModel.getTexture(entity);
		if (replacementTexture != null) {
			return replacementTexture;
		}
		return vanillaTexture;
	}
}
