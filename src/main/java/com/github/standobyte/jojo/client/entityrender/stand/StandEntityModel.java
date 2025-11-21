package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.HiddenModelParts;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.utils.ModelUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.v1_21_4_stuff.Reminder;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.github.standobyte.v1_21_4_stuff.renderstate.EntityRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.RenderStateCrutches;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.HumanoidArm;

public class StandEntityModel<T extends StandEntity, S extends StandEntityRenderState> extends EntityModel<T> implements ArmedModel, HiddenModelParts {
	public ModelPart left_arm_xrot;
	public ModelPart left_arm;
	public ModelPart right_arm_xrot;
	public ModelPart right_arm;
	public ModelPart head;
	public ModelPart torso_no_arms;
	public ModelPart torso_lower;
	public ModelPart left_leg_xrot;
	public ModelPart left_leg;
	public ModelPart right_leg_xrot;
	public ModelPart right_leg;
	protected Set<ModelPart> hiddenParts = new HashSet<>();
	protected Map<String, ModelPart[]> inheritanceChains = new HashMap<>();

	public StandEntityModel(ModelPart root) {
//		super(root, RenderType::entityTranslucent);
		super(RenderType::entityTranslucent);
		Model_1_21_2plus _this = (Model_1_21_2plus) this;
		_this.jojo_ripples$initRoot(root);
		left_arm_xrot = _this.jojo_ripples$getAnyDescendantWithName("left_arm_xrot").orElse(null);
		left_arm = _this.jojo_ripples$getAnyDescendantWithName("left_arm").orElse(null);
		right_arm_xrot = _this.jojo_ripples$getAnyDescendantWithName("right_arm_xrot").orElse(null);
		right_arm = _this.jojo_ripples$getAnyDescendantWithName("right_arm").orElse(null);
		head = _this.jojo_ripples$getAnyDescendantWithName("head").orElse(null);
		torso_no_arms = _this.jojo_ripples$getAnyDescendantWithName("torso_no_arms").orElse(null);
		torso_lower = _this.jojo_ripples$getAnyDescendantWithName("torso_lower").orElse(null);
		left_leg_xrot = _this.jojo_ripples$getAnyDescendantWithName("left_leg_xrot").orElse(null);
		left_leg = _this.jojo_ripples$getAnyDescendantWithName("left_leg").orElse(null);
		right_leg_xrot = _this.jojo_ripples$getAnyDescendantWithName("right_leg_xrot").orElse(null);
		right_leg = _this.jojo_ripples$getAnyDescendantWithName("right_leg").orElse(null);
		// TODO (entity anim) make an array of all model parts that aren't visible by default
		
		addMissingItemHoldPoints();
		initHiddenParts(this);
		inheritanceChains = ModelUtil.modelPartInheritanceChains("root", ((Model_1_21_2plus) this).jojo_ripples$root(), "left_item", "right_item");
	}
	
	protected void addMissingItemHoldPoints() {
		if (left_arm != null) {
			ModelPart armBend = left_arm.getChild("left_arm_bend");
			if (armBend != null && !armBend.hasChild("left_item")) {
				ModelPart itemPoint = new ModelPart(new ArrayList<>(), new HashMap<>());
				itemPoint.setInitialPose(PartPose.offset(0, 3.75f, -2.0f));
				itemPoint.resetPose();
				armBend.children.put("left_item", itemPoint);
			}
		}
		if (right_arm != null) {
			ModelPart armBend = right_arm.getChild("right_arm_bend");
			if (armBend != null && !armBend.hasChild("right_item")) {
				ModelPart itemPoint = new ModelPart(new ArrayList<>(), new HashMap<>());
				itemPoint.setInitialPose(PartPose.offset(0, 3.75f, -2.0f));
				itemPoint.resetPose();
				armBend.children.put("right_item", itemPoint);
			}
		}
	}
	
	@Override
	public Collection<ModelPart> getInitiallyHidden() {
		return hiddenParts;
	}

//	@Override // 1.21.2+
	public void setupAnim(S renderState) {
//		super.setupAnim(renderState); // 1.21.2+
		EntityRenderState.resetPose(this);

		HumanoidPart.setPartsVisible(this, renderState.visibleParts);
		
		RotpAnimDefinition anim = renderState.action.anim;
		float seconds = renderState.action.timeSeconds;
		if (anim != null) {
			anim.animate(this, renderState, seconds, 1);
		}
		else if (head != null) {
			head.xRot = renderState.xRot * MathUtil.DEG_TO_RAD;
			head.yRot = renderState.yRot * MathUtil.DEG_TO_RAD;
		}
		
		// TODO (entity anim) iterate over the array of parts invisible by default - if a part was not animated, set visible to false
	}
	
	@Deprecated
	@Override
    public void setupAnim(StandEntity entity, float limbSwing, float limbSwingAmount, 
    		float ageInTicks, float netHeadYaw, float headPitch) {
    	if (RenderStateCrutches.currentEntityRenderState != null) {
    		setupAnim((S) RenderStateCrutches.currentEntityRenderState);
    	}
    }
	
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		StandEntityRenderState renderState = RenderStateCrutches.currentStandEntityRenderState;
		if (renderState != null) {
			if (renderState.tint != -1) {
				color = FastColor.ARGB32.multiply(color, renderState.tint);
			}
			if (renderState.alpha < 1) {
				color = FastColor.ARGB32.color(FastColor.as8BitChannel(renderState.alpha), color);
			}
		}
		((Model_1_21_2plus) this).jojo_ripples$root().render(poseStack, buffer, packedLight, packedOverlay, color);
		Reminder.thatThisShouldBeInAnEntityModelMixinInstead();
		if (BarrageSwings.currentlyRendering != null) {
			BarrageSwings.currentlyRendering.renderLayerBarrage((EntityModel<?>) (Object) this, 
					poseStack, buffer, packedLight, packedOverlay, color);
		}
	}

	
	public void setAllVisible(boolean visible) {
//		for (ModelPart modelPart : allParts()) {
		for (ModelPart modelPart : ((Model_1_21_2plus) this).jojo_ripples$allParts()) {
			modelPart.visible = visible;
		}
	}
	
	public static void setVisible(@Nullable ModelPart modelPart, boolean visible) {
		if (modelPart != null) modelPart.visible = visible;
	}

	@Override
	public void translateToHand(HumanoidArm side, PoseStack poseStack) {
		var modelParts = switch (side) {
			case LEFT -> inheritanceChains.get("left_item");
			case RIGHT -> inheritanceChains.get("right_item");
		};
		if (modelParts != null) {
			for (ModelPart part : modelParts) {
				part.translateAndRotate(poseStack);
			}
			// counteract the vanilla transforms hardcoded in ItemInHandLayer
			poseStack.translate((float)(side == HumanoidArm.LEFT ? -1 : 1) / 16.0F, -0.5F, 0.125F);
		}
	}

}
