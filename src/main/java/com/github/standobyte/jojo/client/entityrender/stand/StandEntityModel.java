package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityrender.HiddenModelPartsUtil;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.jojo.client.entityrender.replace_player_model.CustomPlayerModel;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.functions.MathUtil;
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

//	@Override // 1.21.2+
	public void setupAnim(S renderState) {
//		super.setupAnim(renderState); // 1.21.2+
		EntityRenderState.resetPose(this);

		HumanoidPart.setPartsVisible(this, renderState.visibleParts);
		
		if (renderState.action.pose != null) {
			RotpAnimDefinition.animate(this, renderState.action.pose);
		}
		else if (head != null) {
			head.xRot = renderState.xRot * MathUtil.DEG_TO_RAD;
			head.yRot = renderState.yRot * MathUtil.DEG_TO_RAD;
		}
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
		CustomPlayerModel.translateToItemHoldPos(side, poseStack, (ModelWithExtraFeatures) this, 0);
	}



	private static final int TICKS_MOTION_TILT_LERP = 5;
	public Vec3 prepareMotionTilt(T entity, float partialTick) {
		int tick = entity.tickCount;
		float ticks = tick + partialTick;
		
		Vec3 tiltVec;
		List<Vec3> vecQueue = entity.clientStuff.tiltVecQueue;
		while (vecQueue.size() > TICKS_MOTION_TILT_LERP) vecQueue.remove(vecQueue.size() - 1);
		boolean fillQueue = vecQueue.size() < TICKS_MOTION_TILT_LERP;
		if (fillQueue || Mth.floor(entity.clientStuff.lastMotionTiltTick) != tick) {
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
		
		return tiltVec;
	}

	public void doMotionTilt(Vec3 tiltVec, AnimFramePose targetPose, boolean idlePose) {
		boolean isSummonPose = false;
		if (!isSummonPose) {
			double tiltSqr = tiltVec.lengthSqr();
			if (tiltSqr > 1.0E-4) {
				double tilt = Math.sqrt(tiltSqr);
				float d1 = (float) Mth.clamp(1 - tilt / Math.PI * 4, 0, 1);

				float tiltX = (float) tiltVec.x;
				float bodyTiltX = tiltX * 0.75f;
				float legsTiltX = tiltX - bodyTiltX;
				
				Vector3f body_rot = getPoseRotationVec(targetPose, "body_rot");
				Vector3f head_rot = getPoseRotationVec(targetPose, "head_rot");
				Vector3f torso_bend = getPoseRotationVec(targetPose, "torso_bend");
				Vector3f left_arm_xrot = getPoseRotationVec(targetPose, "left_arm_xrot");
				Vector3f left_arm = getPoseRotationVec(targetPose, "left_arm");
				Vector3f left_arm_bend = getPoseRotationVec(targetPose, "left_arm_bend");
				Vector3f right_arm_xrot = getPoseRotationVec(targetPose, "right_arm_xrot");
				Vector3f right_arm = getPoseRotationVec(targetPose, "right_arm");
				Vector3f right_arm_bend = getPoseRotationVec(targetPose, "right_arm_bend");
				Vector3f left_leg_xrot = getPoseRotationVec(targetPose, "left_leg_xrot");
				Vector3f left_leg = getPoseRotationVec(targetPose, "left_leg");
				Vector3f left_leg_bend = getPoseRotationVec(targetPose, "left_leg_bend");
				Vector3f right_leg_xrot = getPoseRotationVec(targetPose, "right_leg_xrot");
				Vector3f right_leg = getPoseRotationVec(targetPose, "right_leg");
				Vector3f right_leg_bend = getPoseRotationVec(targetPose, "right_leg_bend");

				if (body_rot != null) {
					body_rot.x += bodyTiltX;
					if (head_rot != null) {
						head_rot.x -= bodyTiltX;
					}
					if (idlePose) {
						body_rot.z += tiltVec.z;
						float diff = body_rot.y - (body_rot.y * d1);
						body_rot.y -= diff;
						if (head_rot != null) {
							head_rot.z -= tiltVec.z;
							head_rot.y += diff;
						}
					}
				}

				double d = Mth.clamp(1 - 1.5 * tilt / Math.PI, 0, 1);
				if (left_leg_bend != null) {
					left_leg_bend.x *= d;
					left_leg_bend.y *= d;
					left_leg_bend.z *= d;
				}
				if (right_leg_bend != null) {
					right_leg_bend.x *= d;
					right_leg_bend.y *= d;
					right_leg_bend.z *= d;
				}
				if (torso_bend != null) {
					double movementFront = Mth.clamp(tiltVec.x, -1, 1);
					if (movementFront > 0 && torso_bend.x > 0) {
						torso_bend.x *= 1 - movementFront;
					}
					else if (movementFront < 0 && torso_bend.x < 0) {
						torso_bend.x *= 1 + movementFront;
					}
				}
				if (idlePose) {
					if (left_arm_bend != null) {
						left_arm_bend.x *= d;
						left_arm_bend.y *= d;
						left_arm_bend.z *= d;
					}
					if (right_arm_bend != null) {
						right_arm_bend.x *= d;
						right_arm_bend.y *= d;
						right_arm_bend.z *= d;
					}
				}

				double d2 = Mth.clamp(1 - tilt / (2 * Math.PI), 0, 1);
				if (idlePose) {
					if (left_arm != null) {
						left_arm.x *= d2;
						left_arm.y *= d2;
						left_arm.z *= d2;
					}
					if (right_arm != null) {
						right_arm.x *= d2;
						right_arm.y *= d2;
						right_arm.z *= d2;
					}
				}
				else {
					if (left_arm_xrot != null) {
						left_arm_xrot.x -= bodyTiltX;
					}
					if (right_arm_xrot != null) {
						right_arm_xrot.x -= bodyTiltX;
					}
				}

				if (right_leg != null) {
					right_leg.x *= d2;
					right_leg.y *= d2;
					right_leg.z *= d2;
				}
				if (right_leg_xrot != null) {
					right_leg_xrot.x += legsTiltX;
				}
				if (left_leg != null) {
					left_leg.x *= d2;
					left_leg.y *= d2;
					left_leg.z *= d2;
				}
				if (left_leg_xrot != null) {
					left_leg_xrot.x += legsTiltX;
				}
			}
		}
	}
	
	@Nullable
	protected static Vector3f getPoseRotationVec(AnimFramePose pose, String modelPartName) {
		ModelPartFrame modelPart = pose.getIfPresent(modelPartName);
		return modelPart != null ? modelPart.rotationOffset : null;
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
