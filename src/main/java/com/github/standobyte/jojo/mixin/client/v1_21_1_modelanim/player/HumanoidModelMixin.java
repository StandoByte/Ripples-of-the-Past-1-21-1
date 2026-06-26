package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim.player;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityanim.player.HumanoidModelCubesBent;
import com.github.standobyte.jojo.client.entityanim.player.RenderAnimatedPlayerModel;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState;
import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.ResourceModelEntry;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin/* extends ModelMixinSuperclass*/ extends AgeableModelMixinSuperclass implements IHumanoidAnimModel {
	@Nullable protected ResourceModelEntry jojo_ripples$playerAnimRig;
	protected HumanoidModelCubesBent humanoidModelWithBends;
	
	@Inject(method = "<init>("
			+ "Lnet/minecraft/client/model/geom/ModelPart;"
			+ "Ljava/util/function/Function;)V", at = @At("RETURN"))
	protected void jojo_ripples$initModel(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
		((Model_1_21_2plus) this).jojo_ripples$initRoot(root);
	}

	@Override
	public void jojo_ripples$setupHumanoidAnim(HumanoidRenderState renderState) {
		RipplesPlayerRenderState jojoRenderState = ((RipplesRenderStateExtensionMixin) renderState).get();
		this.jojo_ripples$playerAnimRig = EntityActionRenderState.setupModelAnim((HumanoidModel<?>) (Object) this, renderState, jojoRenderState);
	}
	
	@Override
	public boolean jojo_rippes$isPlayingAnimation() {
		return jojo_ripples$playerAnimRig != null;
	}
	
	@Override
	public void jojo_ripples$renderWithBends(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (jojo_ripples$playerAnimRig != null) {
			if (humanoidModelWithBends == null) {
				humanoidModelWithBends = HumanoidModelCubesBent.createFromBase((HumanoidModel<?>) (Object) this);
			}
			RenderAnimatedPlayerModel.renderWithBends((HumanoidModel<?>) (Object) this, 
					humanoidModelWithBends, jojo_ripples$playerAnimRig, 
					poseStack, buffer, packedLight, packedOverlay, color);
			ci.cancel();
		}
	}

	@Inject(method = "translateToHand", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$translateToBentHandBefore(HumanoidArm side, PoseStack poseStack, CallbackInfo ci) {
		if (jojo_ripples$playerAnimRig != null) {
			RenderAnimatedPlayerModel.translateToAnimHand(
					(HumanoidModel<?>) (Object) this, jojo_ripples$playerAnimRig, 
					side, poseStack, false);
			ci.cancel();
		}
	}


	@Inject(method = "copyPropertiesTo", at = @At("HEAD"))
	public void jojo_ripples$copyPose(HumanoidModel<?> _model, CallbackInfo ci) {
		HumanoidModelMixin model = (HumanoidModelMixin) (Object) _model;
		model.jojo_ripples$playerAnimRig = this.jojo_ripples$playerAnimRig;
	}
	
	@Shadow protected abstract ModelPart getArm(HumanoidArm side);
	
}
