package com.github.standobyte.jojo.mixin.client.model.playeranim;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerLimbBend;
import com.github.standobyte.jojo.client.entityanim.playerbend.PlayerModelBends;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IPlayerLimbBend {
	@Shadow @Final private List<ModelPart.Cube> cubes;
	@Shadow @Final private Map<String, ModelPart> children;
	@Shadow PartPose initialPose;
	@Shadow private boolean skipDraw;
	private ModelPart jojo_ripples$bendBone;
	private float jojo_ripples$bendOffsetX;
	private float jojo_ripples$bendOffsetY;
	private float jojo_ripples$bendOffsetZ;
	private boolean jojo_ripples$invertBend = false;
	
	
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", 
			at = @At(value = "INVOKE", target = "translateAndRotate", shift = Shift.AFTER), 
			cancellable = true)
	private void jojo_ripples$cubesCompile(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (jojo_ripples$bendBone != null && (jojo_ripples$bendBone.xRot != 0.0F || jojo_ripples$bendBone.yRot != 0.0F || jojo_ripples$bendBone.zRot != 0.0F)) {
			ModelPart asModelPart = (ModelPart) (Object) this;
			PlayerModelBends.drawBentCubes(asModelPart, jojo_ripples$bendBone, jojo_ripples$invertBend, 
					jojo_ripples$bendOffsetX, jojo_ripples$bendOffsetY, jojo_ripples$bendOffsetZ,
					skipDraw, poseStack, 
					buffer, packedLight, packedOverlay, color);
			poseStack.popPose();
			ci.cancel();
		}
	}
	
	@Override
	public void jojo_ripples$setBendBone(ModelPart bendBone, boolean invertBend) {
		jojo_ripples$setBendBone(bendBone, 0, 0, 0, invertBend);
	}
	
	public void jojo_ripples$setBendBone(ModelPart bendBone, float bendOffsetX, float bendOffsetY, float bendOffsetZ, boolean invertBend) {
		if ((Object) bendBone == this) throw new IllegalArgumentException();
		this.jojo_ripples$bendBone = bendBone;
		this.jojo_ripples$bendOffsetX = bendOffsetX;
		this.jojo_ripples$bendOffsetY = bendOffsetY;
		this.jojo_ripples$bendOffsetZ = bendOffsetZ;
		this.jojo_ripples$invertBend = invertBend;
		for (ModelPart modelPart : children.values()) {
			if (modelPart != bendBone) {
				((ModelPartMixin) (Object) modelPart).jojo_ripples$setBendBone(bendBone, 
						bendOffsetX - this.initialPose.x, 
						bendOffsetY - this.initialPose.y, 
						bendOffsetZ - this.initialPose.z, 
						invertBend);
			}
		}
	}
	
	@Override
	public ModelPart jojo_ripples$getBendBone() {
		return jojo_ripples$bendBone;
	}

	@Inject(method = "resetPose", at = @At("TAIL"))
	public void jojo_ripples$onResetPose(CallbackInfo ci) {
		if (this.jojo_ripples$bendBone != null) {
			this.jojo_ripples$bendBone.resetPose();
		}
	}
	
	@Inject(method = "copyFrom", at = @At("TAIL"))
	public void jojo_ripples$onCopyPose(ModelPart modelPart, CallbackInfo ci) {
		if (this.jojo_ripples$bendBone != null) {
			ModelPart bend = ((ModelPartMixin) (Object) modelPart).jojo_ripples$bendBone;
			if (bend != null) {
				this.jojo_ripples$bendBone.copyFrom(bend);
			}
		}
	}
	
}
