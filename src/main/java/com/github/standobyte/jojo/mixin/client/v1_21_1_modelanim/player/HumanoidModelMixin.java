package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim.player;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerBendModel;
import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerLimbBend;
import com.github.standobyte.jojo.client.entityanim.playerbend.PlayerModelBends;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState;
import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin/* extends ModelMixinSuperclass*/ extends AgeableModelMixinSuperclass implements IHumanoidAnimModel, IPlayerBendModel {
	@Shadow @Final public ModelPart body;
	@Shadow @Final public ModelPart rightArm;
	@Shadow @Final public ModelPart leftArm;
	@Shadow @Final public ModelPart rightLeg;
	@Shadow @Final public ModelPart leftLeg;
	private ModelPart jojo_ripples$animMainBody;
	private ModelPart jojo_ripples$animTorso;
	private ModelPart jojo_ripples$animTorsoBend;
	private ModelPart jojo_ripples$animRightArmBend;
	private ModelPart jojo_ripples$animLeftArmBend;
	private ModelPart jojo_ripples$animRightLegBend;
	private ModelPart jojo_ripples$animLeftLegBend;
	private ModelPart jojo_ripples$animRightItem;
	private ModelPart jojo_ripples$animLeftItem;
	private ModelPart jojo_ripples$animCapeBend;
	protected boolean jojo_ripples$playerAnim;
	
	@Inject(method = "<init>("
			+ "Lnet/minecraft/client/model/geom/ModelPart;"
			+ "Ljava/util/function/Function;)V", at = @At("RETURN"))
	protected void jojo_ripples$initModel(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
		float torsoLength = PlayerModelBends.getLimbHeight(body);				// 12
		float torsoLengthUpper = torsoLength / 2;								// 6
		float rightArmLength = PlayerModelBends.getLimbHeight(rightArm);		// 12
		float rightArmLengthUpper = rightArmLength / 2;							// 6
		float leftArmLength = PlayerModelBends.getLimbHeight(leftArm);			// 12
		float leftArmLengthUpper = leftArmLength / 2;							// 6
		float legLength = PlayerModelBends.getLimbHeight(rightLeg);				// 12
		float legLengthUpper = legLength / 2;									// 6
		float leftArmPos = leftArm.y;											// 2
		float rightArmPos = rightArm.y;											// 2
		float leftItemPos = (leftArmLength - leftArmLengthUpper) - 2.25f;		// 3.75
		float rightItemPos = (rightArmLength - rightArmLengthUpper) - 2.25f;	// 3.75
		
		HumanoidModel<?> humanoid = (HumanoidModel<?>) (Model) this;
		
		if (!root.children.containsKey("head")) {
			jojo_ripples$animMainBody = root.children.get("body");
		}
		if (jojo_ripples$animMainBody == null) {
			jojo_ripples$animMainBody = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animMainBody.setInitialPose(PartPose.offset(0, legLength, 0));
		}

		jojo_ripples$animTorso = jojo_ripples$animMainBody.children.get("torso");
		if (jojo_ripples$animTorso == null) {
			jojo_ripples$animTorso = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animTorso.setInitialPose(PartPose.offset(0, torsoLengthUpper, 0));
		}
		
		jojo_ripples$animTorsoBend = jojo_ripples$animTorso.children.get("torso_bend");
		if (jojo_ripples$animTorsoBend == null) {
			jojo_ripples$animTorsoBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animTorsoBend.setInitialPose(PartPose.offset(0, (torsoLength - torsoLengthUpper), 0));
		}

		jojo_ripples$animRightArmBend = humanoid.rightArm.children.get("right_arm_bend");
		if (jojo_ripples$animRightArmBend == null) {
			jojo_ripples$animRightArmBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animRightArmBend.setInitialPose(PartPose.offset(0, rightArmLengthUpper - rightArmPos, 0));
		}

		jojo_ripples$animLeftArmBend = humanoid.leftArm.children.get("left_arm_bend");
		if (jojo_ripples$animLeftArmBend == null) {
			jojo_ripples$animLeftArmBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animLeftArmBend.setInitialPose(PartPose.offset(0, leftArmLengthUpper - leftArmPos, 0));
		}

		jojo_ripples$animRightLegBend = humanoid.rightLeg.children.get("right_leg_bend");
		if (jojo_ripples$animRightLegBend == null) {
			jojo_ripples$animRightLegBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animRightLegBend.setInitialPose(PartPose.offset(0, legLengthUpper, 0));
		}

		jojo_ripples$animLeftLegBend = humanoid.leftLeg.children.get("left_leg_bend");
		if (jojo_ripples$animLeftLegBend == null) {
			jojo_ripples$animLeftLegBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animLeftLegBend.setInitialPose(PartPose.offset(0, legLengthUpper, 0));
		}

		jojo_ripples$animRightItem = jojo_ripples$animRightArmBend.children.get("rightItem");
		if (jojo_ripples$animRightItem == null) {
			jojo_ripples$animRightItem = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animRightItem.setInitialPose(PartPose.offset(0, rightItemPos, 2));
		}

		jojo_ripples$animLeftItem = jojo_ripples$animLeftArmBend.children.get("leftItem");
		if (jojo_ripples$animLeftItem == null) {
			jojo_ripples$animLeftItem = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animLeftItem.setInitialPose(PartPose.offset(0, leftItemPos, 2));
		}

		ModelPart cape = jojo_ripples$animTorsoBend.children.get("cape");
		if (cape != null) {
			jojo_ripples$animCapeBend = cape.children.get("cape_bend");
		}
		if (jojo_ripples$animCapeBend == null) {
			jojo_ripples$animCapeBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
			jojo_ripples$animCapeBend.setInitialPose(PartPose.offset(0, torsoLengthUpper, 0));
		}

		((IPlayerLimbBend) (Object) body).jojo_ripples$setBendBone(jojo_ripples$animTorsoBend, true);
		((IPlayerLimbBend) (Object) rightArm).jojo_ripples$setBendBone(jojo_ripples$animRightArmBend, false);
		((IPlayerLimbBend) (Object) leftArm).jojo_ripples$setBendBone(jojo_ripples$animLeftArmBend, false);
		((IPlayerLimbBend) (Object) rightLeg).jojo_ripples$setBendBone(jojo_ripples$animRightLegBend, false);
		((IPlayerLimbBend) (Object) leftLeg).jojo_ripples$setBendBone(jojo_ripples$animLeftLegBend, false);
//		setBend(body, "cape", jojo_ripples$animCapeBend, false);
		setBend(root, "cloak", jojo_ripples$animCapeBend, false);
		
		((Model_1_21_2plus) this).jojo_ripples$initRoot(root);
	}
	
	private static void setBend(ModelPart parent, String modelPartName, ModelPart bendBone, boolean invertBend) {
		ModelPart modelPart = parent.children.get(modelPartName);
		if (modelPart != null) {
			((IPlayerLimbBend) (Object) modelPart).jojo_ripples$setBendBone(bendBone, invertBend);
		}
	}

	@Override
	public void jojo_ripples$setupHumanoidAnim(HumanoidRenderState renderState) {
		RipplesPlayerRenderState jojoRenderState = ((RipplesRenderStateExtensionMixin) renderState).get();
		this.jojo_ripples$playerAnim = EntityActionRenderState.setupModelAnim((HumanoidModel<?>) (Object) this, renderState, jojoRenderState);
	}
	
	@Override
	public boolean jojo_rippes$isPlayingAnimation() {
		return jojo_ripples$playerAnim;
	}
	
	@Override
	public void jojo_ripples$renderWithBends(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (this.jojo_ripples$playerAnim) {
			PlayerModelBends.renderWithBends((HumanoidModel<?>) (Object) this, this, 
					poseStack, buffer, 
					packedLight, packedOverlay, color);
			ci.cancel();
		}
	}

	@Inject(method = "translateToHand", at = @At("HEAD"))
	public void jojo_ripples$translateToBentHandBefore(HumanoidArm side, PoseStack poseStack, CallbackInfo ci) {
		if (this.jojo_ripples$playerAnim) {
			PlayerModelBends.translateToAnimHand1((HumanoidModel<?>) (Object) this, this, side, poseStack);
		}
	}

	@Inject(method = "translateToHand", at = @At("TAIL"))
	public void jojo_ripples$translateToBentHandAfter(HumanoidArm side, PoseStack poseStack, CallbackInfo ci) {
		if (this.jojo_ripples$playerAnim) {
			 PlayerModelBends.translateToAnimHand2((HumanoidModel<?>) (Object) this, this, side, poseStack);
		}
	}


	@Override
//	public void jojo_ripples$onResetPose(CallbackInfo ci) {
	public void jojo_ripples_v1_21_1$onResetPose() {
		jojo_ripples$animMainBody.resetPose();
		jojo_ripples$animTorso.resetPose();
		jojo_ripples$animRightItem.resetPose();
		jojo_ripples$animLeftItem.resetPose();
//		// the bends are reset from their respective bone parts (ModelPartMixin#jojo_ripples$onResetPose)
	}

	@Inject(method = "copyPropertiesTo", at = @At("HEAD"))
	public void jojo_ripples$copyPose(HumanoidModel<?> _model, CallbackInfo ci) {
//		IPlayerPseudoModelParts model = (IPlayerPseudoModelParts) _model;
		HumanoidModelMixin model = (HumanoidModelMixin) (IPlayerBendModel) _model;
		model.jojo_ripples$animMainBody().copyFrom(this.jojo_ripples$animMainBody);
		model.jojo_ripples$animTorso().copyFrom(this.jojo_ripples$animTorso);
		model.jojo_ripples$animTorsoBend().copyFrom(this.jojo_ripples$animTorsoBend);
		model.jojo_ripples$animRightArmBend().copyFrom(this.jojo_ripples$animRightArmBend);
		model.jojo_ripples$animLeftArmBend().copyFrom(this.jojo_ripples$animLeftArmBend);
		model.jojo_ripples$animRightLegBend().copyFrom(this.jojo_ripples$animRightLegBend);
		model.jojo_ripples$animLeftLegBend().copyFrom(this.jojo_ripples$animLeftLegBend);
		model.jojo_ripples$animRightItem().copyFrom(this.jojo_ripples$animRightItem);
		model.jojo_ripples$animLeftItem().copyFrom(this.jojo_ripples$animLeftItem);
		model.jojo_ripples$animCapeBend().copyFrom(this.jojo_ripples$animCapeBend);
		model.jojo_ripples$playerAnim = this.jojo_ripples$playerAnim;
	}
	
	@Override public ModelPart jojo_ripples$animMainBody() { return jojo_ripples$animMainBody; }
	@Override public ModelPart jojo_ripples$animTorso() { return jojo_ripples$animTorso; }
	@Override public ModelPart jojo_ripples$animTorsoBend() { return jojo_ripples$animTorsoBend; }
	@Override public ModelPart jojo_ripples$animRightArmBend() { return jojo_ripples$animRightArmBend; }
	@Override public ModelPart jojo_ripples$animLeftArmBend() { return jojo_ripples$animLeftArmBend; }
	@Override public ModelPart jojo_ripples$animRightLegBend() { return jojo_ripples$animRightLegBend; }
	@Override public ModelPart jojo_ripples$animLeftLegBend() { return jojo_ripples$animLeftLegBend; }
	@Override public ModelPart jojo_ripples$animRightItem() { return jojo_ripples$animRightItem; }
	@Override public ModelPart jojo_ripples$animLeftItem() { return jojo_ripples$animLeftItem; }
	@Override public ModelPart jojo_ripples$animCapeBend() { return jojo_ripples$animCapeBend; }
	@Override public ModelPart jojo_ripples$leftArm() { return getArm(HumanoidArm.LEFT); }
	@Override public ModelPart jojo_ripples$rightArm() { return getArm(HumanoidArm.RIGHT); }
	@Shadow protected abstract ModelPart getArm(HumanoidArm side);
	
}
