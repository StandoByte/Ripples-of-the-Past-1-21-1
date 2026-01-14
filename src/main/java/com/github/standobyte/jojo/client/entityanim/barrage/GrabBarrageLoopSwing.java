package com.github.standobyte.jojo.client.entityanim.barrage;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings.BarrageSwing;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.ui.utils.RGBUtil;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

public class GrabBarrageLoopSwing extends BarrageSwing {
	protected final float xRot;
	protected float animTimeOffset;
	protected final HumanoidArm side;
	protected final Vec3 offset;
	protected final float zRot;
	
	public static float loopLen = 20f / 6;

	public GrabBarrageLoopSwing(RotpAnimDefinition barrageAnim, LivingEntityRenderState renderState, 
			float startingAnim, float animMax, HumanoidArm side, double maxOffset, float animTimeOffset) {
		super(barrageAnim, startingAnim, animMax);
		this.xRot = renderState.xRot;
		this.animTimeOffset = animTimeOffset;
		this.side = side;
		double upOffset = (RANDOM.nextDouble() - 0.5) * maxOffset;
		double leftOffset = RANDOM.nextDouble() * maxOffset / 2;
		double frontOffset = RANDOM.nextDouble() * 0.25;
		if (side == HumanoidArm.RIGHT) {
			leftOffset *= -1;
		}
		double atan = Mth.atan2(upOffset, leftOffset);
		zRot = maxOffset == 0 ? 0 : MathUtil.wrapRadians((float) (Math.PI / 2 - atan));
		offset = new Vec3(leftOffset, upOffset, frontOffset);
		tickSpeed = 0.5f;
	}

	public static void addSwings(BarrageSwings swings, RotpAnimDefinition barrageAnim, 
			LivingEntityRenderState curRenderState, float animTimeSecs, HumanoidArm punchingArm) {
		float lastLoop = swings.loopLast;
		float loop = curRenderState.ageInTicks / loopLen;
		if (swings.isBarragingAnim && loop > lastLoop) {
			EntityActionRenderState stats = EntityActionRenderState.getFrom(curRenderState);
			
			float hits = stats.barrageSwingsPerSecond / 20F * Math.min(loop - lastLoop, 1) * loopLen / 2;
			int swingsToAdd = MathUtil.fractionRandomInc(hits / 2);
			if (swingsToAdd > 0) {
				final HumanoidArm side = punchingArm;
				double maxOffset = Math.max(1 - stats.barragePrecision / 64, 0);

				for (int i = 0; i < swingsToAdd; i++) {
					float x = ((float) i + (RANDOM.nextFloat() - 0.5F) * 0.4F) / swingsToAdd;
					float f = x * loopLen * 0.5F;
					float addTime = (side == HumanoidArm.LEFT ? loopLen * 0.5f : loopLen * 0.5f) + (animTimeSecs - animTimeSecs % loopLen);
					swings.barrageSwings.add(new GrabBarrageLoopSwing(
							barrageAnim, curRenderState, f, loopLen, side, maxOffset, addTime));
				}
			}
		}
		swings.loopLast = loop;
	}

	@Override
	public void poseAndRender(EntityModel<?> model, 
			PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color) {
		BarrageSwings.setOnlyOneArmVisible(model, side);
		float loopCompletion = ticks / ticksMax;
		float swingAmount = loopCompletion < 0.5 ? loopCompletion * 2 : (1 - loopCompletion) * 2;
		double zAdditional = 0.5 * swingAmount;
		Vec3 offsetRot = new Vec3(offset.x, -offset.y, offset.z + zAdditional).xRot(xRot * MathUtil.DEG_TO_RAD);
		poseStack.pushPose();
		poseStack.translate(offsetRot.x, offsetRot.y, -offsetRot.z);
		
		sharedActionRenderState.actionPhase = ActionPhase.PERFORM;
		sharedActionRenderState.phaseTime = ticks + animTimeOffset;
		sharedActionRenderState.disableCrouch = true;
		sharedRenderState.xRot = this.xRot;
		sharedRenderState.yRot = 0;
		
		float seconds = barrageAnim.getAnimTime(sharedActionRenderState);
		barrageAnim.animate(model, sharedRenderState, sharedActionRenderState, seconds, 1);
		ModelPart arm = BarrageSwings.getNoXRotArm(model, side);
		
		arm.yRot = Mth.lerp(swingAmount, arm.yRot, arm.yRot + zRot * 0.5f);
		
		// XXX (barrage anim) some layers are not translucent (armor, clothes, mannequin model, etc.)
		float alpha = 0.75f * swingAmount;
		color = RGBUtil.scaleAlpha(color, alpha);
		((Model_1_21_2plus) model).jojo_ripples$root().render(poseStack, buffer, packedLight, packedOverlay, color);
		poseStack.popPose();
	}
}
