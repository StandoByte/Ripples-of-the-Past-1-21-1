package com.github.standobyte.jojo.mixin.client.model;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityrender.replace_player_model.ReplacePlayerModel;
import com.github.standobyte.jojo.client.firstperson.FirstPersonRender;
import com.github.standobyte.jojo.mixininterface.LivingRendererLayers;
import com.github.standobyte.jojo.mixininterface.PlayerRendererInterface;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingRendererLayersMixin<T extends LivingEntity, M extends EntityModel<T>> implements LivingRendererLayers<T, M> {
	@Shadow @Final protected List<RenderLayer<T, M>> layers;
	@Unique private boolean onlyRenderFirstPersonLayers = false;
	@Unique private boolean useLayerFilter = false;
	
	@Override
	public List<RenderLayer<T, M>> jojo_ripples$allLayers() { return layers; }
	
	@Override
	public void jojoRipples$setOnlyRenderFirstPersonLayers() {
		onlyRenderFirstPersonLayers = true;
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/world/entity/LivingEntity;isSpectator()Z"))
	private void checkIfModelWasReplaced(CallbackInfo ci) {
		useLayerFilter = this instanceof PlayerRendererInterface playerRenderer && playerRenderer.jojo_ripples$isUsingCustomModel();
	}

	@WrapWithCondition(method = "render", at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render("
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
					+ "I"
					+ "Lnet/minecraft/world/entity/Entity;"
					+ "FFFFFF"
					+ ")V"))
	private boolean disableLayersWithCustomModel(RenderLayer layer,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			Entity livingEntity,
			float limbSwing,
			float limbSwingAmount,
			float partialTick,
			float ageInTicks,
			float netHeadYaw,
			float headPitch) {
		return jojo_ripples$shouldRenderLayer(layer, livingEntity);
	}

	@Override
	public boolean jojo_ripples$shouldRenderLayer(RenderLayer layer, Entity livingEntity) {
		if (onlyRenderFirstPersonLayers && !FirstPersonRender.shouldRenderInFirstPersonAnim(layer)) {
			return false;
		}
		if (useLayerFilter) {
			var filter = ReplacePlayerModel.getRendererLayerFilter(livingEntity);
			if (filter != null) {
				return filter.test(layer);
			}
		}
		return true;
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;"
					+ "render("
					//+ "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
					+ "Lnet/minecraft/world/entity/Entity;FF"
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
					+ "I)V"))
	private void resetFilterFlags(LivingEntity entity, float entityYaw, float partialTicks, 
			PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo ci) {
		onlyRenderFirstPersonLayers = false;
		useLayerFilter = false;
	}
	
}
