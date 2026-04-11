package com.github.standobyte.jojo.client.entityanim.barrage;

import com.github.standobyte.jojo.client.entityanim.LivingAnimState;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings.BarrageSwing;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery.AnimMolangVariables;
import com.github.standobyte.jojo.client.util.functions.RGBUtil;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TwoHandedBarrageLoopSwing extends BarrageSwing {
	protected final float xRot;
	protected float animTimeOffset;
	protected final HumanoidArm side;
	protected final Vec3 offset;
	protected final float zRot;
	
	public static float loopLen = 4;

	public TwoHandedBarrageLoopSwing(RotpAnimDefinition barrageAnim, LivingEntity entity, 
			float startingAnim, float animMax, HumanoidArm side, double maxOffset, float animTimeOffset) {
		super(barrageAnim, startingAnim, animMax);
		this.xRot = entity.getXRot();
		this.animTimeOffset = animTimeOffset;
		this.side = side;
		double upOffset = (RANDOM.nextDouble() - 0.5) * maxOffset;
		double leftOffset = RANDOM.nextDouble() * maxOffset / 2;
		double frontOffset = RANDOM.nextDouble() * 0.5;
		if (side == HumanoidArm.RIGHT) {
			leftOffset *= -1;
		}
		double atan = Mth.atan2(upOffset, leftOffset);
		zRot = maxOffset == 0 ? 0 : MathUtil.wrapRadians((float) (Math.PI / 2 - atan));
		offset = new Vec3(leftOffset, upOffset, frontOffset);
		tickSpeed = 0.5f;
	}

	public static void addSwings(BarrageSwings swings, RotpAnimDefinition barrageAnim, 
			LivingEntity entity, float animTimeSecs) {
		float ticks = animTimeSecs * 20;
		float lastLoop = swings.loopLast;
		float loop = ticks / loopLen;
		if (swings.isBarragingAnim && loop > lastLoop) {
			float hits = swings.barrageSwingsPerSecond / 20F * Math.min(loop - lastLoop, 1) * loopLen;
			int swingsToAdd = MathUtil.fractionRandomInc(hits / 2);
			if (swingsToAdd > 0) {
				HumanoidArm side = HumanoidArm.RIGHT;
				double maxOffset = Math.max(1 - swings.barragePrecision / 64, 0);
				if (RANDOM.nextBoolean()) side = side.getOpposite();

				for (int i = 0; i < swingsToAdd; i++) {
					float x = ((float) i + (RANDOM.nextFloat() - 0.5F) * 0.4F) / swingsToAdd;
					float f = x * loopLen * 0.5F;
					float addTime = (side == HumanoidArm.LEFT ? loopLen * 0.5f : 0) + (animTimeSecs - animTimeSecs % loopLen);
					swings.barrageSwings.add(new TwoHandedBarrageLoopSwing(
							barrageAnim, entity, f, loopLen, side, maxOffset, addTime));
					side = side.getOpposite();
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
		
		LivingAnimState animState = LivingAnimState.reusedInstance;
		animState.actionPhase = ActionPhase.PERFORM;
		animState.phaseTime = ticks + animTimeOffset;
		animState.time = -1;
		animState.phaseCompletion = -1;
		AnimMolangVariables molangVariables = AnimMolangVariables.set(this.xRot, 0, 0);
		
		float seconds = barrageAnim.getAnimTime(animState);
		barrageAnim.animate(model, molangVariables, null, seconds, 1);
		ModelPart arm = BarrageSwings.getNoXRotArm(model, side);
		
		arm.zRot = Mth.lerp(swingAmount, arm.zRot, arm.zRot + zRot * 1.25f);
		arm.yRot = Mth.lerp(swingAmount, arm.yRot, 0);
		arm.xRot = Mth.lerp(swingAmount, arm.xRot, (float) -Math.PI * 0.5f);
		
		// XXX (barrage anim) some layers are not translucent (armor, clothes, mannequin model, etc.)
		float alpha = 0.75f * swingAmount;
		color = RGBUtil.scaleAlpha(color, alpha);
		((Model_1_21_2plus) model).jojo_ripples$root().render(poseStack, buffer, packedLight, packedOverlay, color);
		poseStack.popPose();
	}
}
