package com.github.standobyte.jojo;

import com.github.standobyte.jojo.mechanics.clothes.client.layer.HumanoidClothesModel;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class UglyCrutchesClient {
	public static FloatStack modelPoseXRot = new FloatArrayList();
//	public static Stack<Vector3f> modelPoseRotation = new Stack<>();
	
	public static void pushPlayerAnimPart(ModelPart rigMmodelPart) {
		pushPlayerAnimPart(rigMmodelPart.xRot, rigMmodelPart.yRot, rigMmodelPart.zRot);
	}
	
	public static void pushPlayerAnimPart(float xRot, float yRot, float zRot) {
		float xRotSum = modelPoseXRot.isEmpty() ? 0 : modelPoseXRot.topFloat();
//		Vector3f rot = modelPoseRotation.isEmpty() ? new Vector3f(0, 0, 1) : new Vector3f(modelPoseRotation.peek());
//		
//		if (xRot != 0 || yRot != 0 || zRot != 0) {
//			rot.rotate(new Quaternionf().rotationZYX(zRot, yRot, xRot));
//		}
//		modelPoseRotation.add(rot);
		
		modelPoseXRot.push(xRotSum + xRot);
	}
	
	public static void popPlayerAnimPart() {
//		modelPoseRotation.pop();
		
		modelPoseXRot.popFloat();
	}
	
	public static void adjustClothesInROTPAnim(String modelPartName, ModelPart modelPart) {
		if (!modelPoseXRot.isEmpty() && 
				("coat".equals(modelPartName))) {
			modelPart.xRot -= modelPoseXRot.topFloat();
		}
	}
	
	
	public static void adjustClothesWithVanillaAnim(HumanoidClothesModel clothesModel, LivingEntity entity, float partialTick) {
		if (entity == null) return;
		
		if (entity instanceof Player player) {
			((Model_1_21_2plus) clothesModel).jojo_ripples$getAnyDescendantWithName("coat").ifPresent(coat -> {
				coat.loadPose(coat.getInitialPose());

				// Ctrl+C Ctrl+V from CapeLayer
				double d0 = Mth.lerp(partialTick, player.xCloakO, player.xCloak) - Mth.lerp(partialTick, entity.xo, entity.getX());
				double d1 = Mth.lerp(partialTick, player.yCloakO, player.yCloak) - Mth.lerp(partialTick, entity.yo, entity.getY());
				double d2 = Mth.lerp(partialTick, player.zCloakO, player.zCloak) - Mth.lerp(partialTick, entity.zo, entity.getZ());
				float f = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
				double d3 = (double)Mth.sin(f * MathUtil.DEG_TO_RAD);
				double d4 = (double)(-Mth.cos(f * MathUtil.DEG_TO_RAD));
				float f1 = (float)d1 * 10.0F;
				f1 = Mth.clamp(f1, -6.0F, 32.0F);
				float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
				f2 = Mth.clamp(f2, 0.0F, 150.0F);
				float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
				f3 = Mth.clamp(f3, -20.0F, 20.0F);
				if (f2 < 0.0F) {
					f2 = 0.0F;
				}

				float f4 = Mth.lerp(partialTick, player.oBob, player.bob);
				f1 += Mth.sin(Mth.lerp(partialTick, entity.walkDistO, entity.walkDist) * 6.0F) * 32.0F * f4;
				if (entity.isCrouching()) {
					f1 += 25.0F;
				}

				coat.xRot += MathUtil.DEG_TO_RAD * (/*6.0F + */ f2 / 2.0F + f1);

				coat.xRot = MathUtil.max(coat.xRot, clothesModel.leftLeg.xRot, clothesModel.rightLeg.xRot);
				coat.xRot = Mth.clamp(coat.xRot, 0, MathUtil.PI * 0.5f);
			});
		}
	}
	
}
