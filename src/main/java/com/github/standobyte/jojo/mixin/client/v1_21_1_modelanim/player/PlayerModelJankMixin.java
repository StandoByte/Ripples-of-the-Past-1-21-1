package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.player.HumanoidModelPartsWithBends;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(PlayerModel.class)
public abstract class PlayerModelJankMixin extends HumanoidModelMixin {
	@Shadow private boolean slim;
	
	// if you ever animate skeletons, you'll need a separate mixin to SkeletonModel for the same reason
	@Inject(method = "translateToHand", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$becauseTheyDidntCallSuper_translateToBentHandBefore(HumanoidArm side, PoseStack poseStack, CallbackInfo ci) {
		if (jojo_ripples$playerAnimRig != null) {
			HumanoidModelPartsWithBends.translateToAnimHand(
					(HumanoidModel<?>) (Object) this, jojo_ripples$playerAnimRig, 
					side, poseStack, slim);
			ci.cancel();
		}
	}

}
