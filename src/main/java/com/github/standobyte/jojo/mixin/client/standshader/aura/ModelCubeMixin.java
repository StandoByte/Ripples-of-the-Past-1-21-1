package com.github.standobyte.jojo.mixin.client.standshader.aura;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojo.client.shader.standaura.AuraUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;

@Mixin(ModelPart.Cube.class)
public class ModelCubeMixin {

	@ModifyVariable(method = "compile", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	public PoseStack.Pose jojo_ripples$inflateCube(PoseStack.Pose pose) {
		if (AuraUtil.inflateEachCube != null) {
			return AuraUtil.inflateCube(pose, (ModelPart.Cube) (Object) this);
		}
		return pose;
	}
}
