package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityrender.HiddenModelPartsUtil;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.jojo.client.utils.ModelPartWithName;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

public class StandEntityModel<T extends StandEntity, S extends StandEntityRenderState> extends EntityModel<T> implements ArmedModel {
	public ModelPart body_rot;
	public ModelPart left_arm_xrot;
	public ModelPart left_arm;
	public ModelPart left_arm_bend;
	public ModelPart right_arm_xrot;
	public ModelPart right_arm;
	public ModelPart right_arm_bend;
	public ModelPart head;
	public ModelPart head_rot;
	public ModelPart torso_no_arms;
	public ModelPart torso_lower;
	public ModelPart torso_bend;
	public ModelPart left_leg_xrot;
	public ModelPart left_leg;
	public ModelPart left_leg_bend;
	public ModelPart right_leg_xrot;
	public ModelPart right_leg;
	public ModelPart right_leg_bend;

	public StandEntityModel(ModelPart root) {
//		super(root, RenderType::entityTranslucent);
		super(RenderType::entityTranslucent);
		Model_1_21_2plus _this = (Model_1_21_2plus) this;
		_this.jojo_ripples$initRoot(root);
		body_rot = _this.jojo_ripples$getAnyDescendantWithName("body_rot").orElse(null);
		left_arm_xrot = _this.jojo_ripples$getAnyDescendantWithName("left_arm_xrot").orElse(null);
		left_arm = _this.jojo_ripples$getAnyDescendantWithName("left_arm").orElse(null);
		left_arm_bend = _this.jojo_ripples$getAnyDescendantWithName("left_arm_bend").orElse(null);
		right_arm_xrot = _this.jojo_ripples$getAnyDescendantWithName("right_arm_xrot").orElse(null);
		right_arm = _this.jojo_ripples$getAnyDescendantWithName("right_arm").orElse(null);
		right_arm_bend = _this.jojo_ripples$getAnyDescendantWithName("right_arm_bend").orElse(null);
		head = _this.jojo_ripples$getAnyDescendantWithName("head").orElse(null);
		head_rot = _this.jojo_ripples$getAnyDescendantWithName("head_rot").orElse(null);
		torso_no_arms = _this.jojo_ripples$getAnyDescendantWithName("torso_no_arms").orElse(null);
		torso_lower = _this.jojo_ripples$getAnyDescendantWithName("torso_lower").orElse(null);
		torso_bend = _this.jojo_ripples$getAnyDescendantWithName("torso_bend").orElse(null);
		left_leg_xrot = _this.jojo_ripples$getAnyDescendantWithName("left_leg_xrot").orElse(null);
		left_leg = _this.jojo_ripples$getAnyDescendantWithName("left_leg").orElse(null);
		left_leg_bend = _this.jojo_ripples$getAnyDescendantWithName("left_leg_bend").orElse(null);
		right_leg_xrot = _this.jojo_ripples$getAnyDescendantWithName("right_leg_xrot").orElse(null);
		right_leg = _this.jojo_ripples$getAnyDescendantWithName("right_leg").orElse(null);
		right_leg_bend = _this.jojo_ripples$getAnyDescendantWithName("right_leg_bend").orElse(null);
		
		addMissingItemHoldPoints();
		HiddenModelPartsUtil.initHiddenParts(this);
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

	public AnimFramePose pose;
//	@Override // 1.21.2+
	public void setupAnim(S renderState) {
//		super.setupAnim(renderState); // 1.21.2+
		EntityRenderState.resetPose(this);

		HumanoidPart.setPartsVisible(this, renderState.visibleParts);
		
		if (renderState.action.staticPose != null) {
			pose = renderState.action.staticPose;
			RotpAnimDefinition.animate(this, renderState.action.staticPose);
		}
		else {
			RotpAnimDefinition anim = renderState.action.anim;
			float seconds = renderState.action.timeSeconds;
			if (anim != null) {
				pose = anim.animate(this, renderState, seconds, 1);
			}
			else if (head != null) {
				head.xRot = renderState.xRot * MathUtil.DEG_TO_RAD;
				head.yRot = renderState.yRot * MathUtil.DEG_TO_RAD;
			}
		}

//		if (ClientModSettings.getSettingsReadOnly().standMotionTilt) {
			doMotionTilt(renderState);
//		}
	}
	
	@SuppressWarnings("unchecked")
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
			case LEFT -> ((ModelWithExtraFeatures) this).jojo_ripples$getPathToModelPart("left_item");
			case RIGHT -> ((ModelWithExtraFeatures) this).jojo_ripples$getPathToModelPart("right_item");
		};
		if (modelParts != null) {
			for (ModelPartWithName part : modelParts) {
				part.part().translateAndRotate(poseStack);
			}
			// counteract the vanilla transforms hardcoded in ItemInHandLayer
			poseStack.translate((float)(side == HumanoidArm.LEFT ? -1 : 1) / 16.0F, -0.5F, 0.125F);
		}
	}



	private static final int TICKS_MOTION_TILT_LERP = 5;
	public void prepareMotionTilt(S renderState, T entity) {
		float ticks = renderState.ageInTicks;
		float partialTick = Mth.frac(ticks);
		
		Vec3 tiltVec;
		List<Vec3> vecQueue = entity.clientStuff.tiltVecQueue;
		while (vecQueue.size() > TICKS_MOTION_TILT_LERP) vecQueue.remove(vecQueue.size() - 1);
		boolean fillQueue = vecQueue.size() < TICKS_MOTION_TILT_LERP;
		if (fillQueue || Mth.floor(entity.clientStuff.lastMotionTiltTick) != Mth.floor(ticks)) {
			Vec3 motion = entity.position().subtract(entity.xOld, entity.yOld, entity.zOld);

			tiltVec = motion.yRot(entity.yBodyRot * MathUtil.DEG_TO_RAD).scale(2);
			tiltVec = new Vec3(tiltVec.z, 0, tiltVec.x);
			double motionSqr = tiltVec.lengthSqr();
			if (motionSqr > Math.pow(Math.PI / 4, 2)) {
				tiltVec = tiltVec.normalize().scale(Math.PI / 4);
			}

			if (fillQueue) {
				for (int i = vecQueue.size(); i < TICKS_MOTION_TILT_LERP; i++) {
					vecQueue.add(tiltVec);
				}
			}
			else {
				vecQueue.remove(0);
				vecQueue.add(tiltVec);
			}

			entity.clientStuff.lastMotionTiltTick = ticks;
		}
		tiltVec = lerpVecs(vecQueue, partialTick);
		
		renderState.motionTiltVec = tiltVec;
	}

	protected void doMotionTilt(S renderState) {
		boolean isSummonPose = false;
		if (!isSummonPose) {
			Vec3 tiltVec = renderState.motionTiltVec;

			boolean idlePose = renderState.action.animId != null && renderState.action.animId.isIdle();
			double tiltSqr = tiltVec.lengthSqr();
			if (tiltSqr > 1.0E-4) {
				double tilt = Math.sqrt(tiltSqr);
				float d1 = (float) Mth.clamp(1 - tilt / Math.PI * 4, 0, 1);

				float tiltX = (float) tiltVec.x;
				float bodyTiltX = tiltX * 0.75f;
				float legsTiltX = tiltX - bodyTiltX;

				if (this.body_rot != null) {
					this.body_rot.xRot += bodyTiltX;
					if (this.head_rot != null) {
						this.head_rot.xRot -= bodyTiltX;
					}
					if (idlePose) {
						this.body_rot.zRot += tiltVec.z;
						float diff = this.body_rot.yRot - (this.body_rot.yRot * d1);
						this.body_rot.yRot -= diff;
						if (this.head_rot != null) {
							this.head_rot.zRot -= tiltVec.z;
							this.head_rot.yRot += diff;
						}
					}
				}

				double d = Mth.clamp(1 - 1.5 * tilt / Math.PI, 0, 1);
				if (this.left_leg_bend != null) {
					this.left_leg_bend.xRot *= d;
					this.left_leg_bend.yRot *= d;
					this.left_leg_bend.zRot *= d;
				}
				if (this.right_leg_bend != null) {
					this.right_leg_bend.xRot *= d;
					this.right_leg_bend.yRot *= d;
					this.right_leg_bend.zRot *= d;
				}
				if (this.torso_bend != null) {
					double movementFront = Mth.clamp(tiltVec.x, -1, 1);
					if (movementFront > 0 && torso_bend.xRot > 0) {
						torso_bend.xRot *= 1 - movementFront;
					}
					else if (movementFront < 0 && torso_bend.xRot < 0) {
						torso_bend.xRot *= 1 + movementFront;
					}
				}
				if (idlePose) {
					if (this.left_arm_bend != null) {
						this.left_arm_bend.xRot *= d;
						this.left_arm_bend.yRot *= d;
						this.left_arm_bend.zRot *= d;
					}
					if (this.right_arm_bend != null) {
						this.right_arm_bend.xRot *= d;
						this.right_arm_bend.yRot *= d;
						this.right_arm_bend.zRot *= d;
					}
				}

				double d2 = Mth.clamp(1 - tilt / (2 * Math.PI), 0, 1);
				if (idlePose) {
					if (this.left_arm != null) {
						this.left_arm.xRot *= d2;
						this.left_arm.yRot *= d2;
						this.left_arm.zRot *= d2;
					}
					if (this.right_arm != null) {
						this.right_arm.xRot *= d2;
						this.right_arm.yRot *= d2;
						this.right_arm.zRot *= d2;
					}
				}
				else {
					if (this.left_arm_xrot != null) {
						this.left_arm_xrot.xRot -= bodyTiltX;
					}
					if (this.right_arm_xrot != null) {
						this.right_arm_xrot.xRot -= bodyTiltX;
					}
				}

				if (this.right_leg != null) {
					this.right_leg.xRot *= d2;
					this.right_leg.yRot *= d2;
					this.right_leg.zRot *= d2;
				}
				if (this.right_leg_xrot != null) {
					this.right_leg_xrot.xRot += legsTiltX;
				}
				if (this.left_leg != null) {
					this.left_leg.xRot *= d2;
					this.left_leg.yRot *= d2;
					this.left_leg.zRot *= d2;
				}
				if (this.left_leg_xrot != null) {
					this.left_leg_xrot.xRot += legsTiltX;
				}
			}
		}
	}

	private static Vec3 lerpVecs(List<Vec3> vecs, float partialTick) {
		double x = 0;
		double y = 0;
		double z = 0;
		Vec3 prevVec = vecs.get(0);
		Vec3 vec;
		float n = vecs.size();
		for (int i = 1; i < n; i++) {
			vec = vecs.get(i);
			x += Mth.lerp(partialTick, prevVec.x, vec.x);
			y += Mth.lerp(partialTick, prevVec.y, vec.y);
			z += Mth.lerp(partialTick, prevVec.z, vec.z);
			prevVec = vec;
		}
		return new Vec3(x / n, y / n, z / n);
	}
}
