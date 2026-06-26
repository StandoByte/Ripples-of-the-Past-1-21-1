package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
		super(renderer);
	}

	@Inject(method = "render", 
			at = @At(value = "INVOKE", 
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", 
			ordinal = 0))
	public void jojo_ripples$fixCapePos(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer livingEntity,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		PlayerModel<AbstractClientPlayer> model = getParentModel();
		if (((IHumanoidAnimModel) model).jojo_rippes$isPlayingAnimation()) {
			// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!! (bend) cape layer
			//RenderAnimatedPlayerModel.repositionCloak(model, (IPlayerBendModel) model, poseStack);
		}
	}
}
