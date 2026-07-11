package com.github.standobyte.jojo.client.entityanim.player;

import java.util.List;

import com.github.standobyte.jojo.client.entityanim.player.HumanoidModelPartsWithBends.AlternativeModelPart;
import com.github.standobyte.jojo.client.entityanim.player.bend_crutches.DeformableCube;
import com.github.standobyte.jojo.client.entityanim.player.bend_crutches.DeformableQuad;
import com.github.standobyte.jojo.client.entityanim.player.bend_crutches.DeformableVertex;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;

public class TmpBendCrutches {

	public static void partiallyFixTorsoClothesParts(AlternativeModelPart modelPart, PoseStack poseStack) {
		boolean isTorsoPart = !modelPart.name().contains("head");
		if (isTorsoPart) {
			poseStack.translate(0, -0.375, 0);
		}
	}
	
	
	
	public static float leftArmBend = 0;
	public static float rightArmBend = 0;
	public static float leftLegBend = 0;
	public static float rightLegBend = 0;
	public static float torsoBend = 0;
	
	public static void setBends(Model_1_21_2plus rigModel) {
		leftArmBend = 0;
		rightArmBend = 0;
		leftLegBend = 0;
		rightLegBend = 0;
		torsoBend = 0;
		rigModel.jojo_ripples$getAnyDescendantWithName("left_arm_bend").ifPresent(part -> TmpBendCrutches.leftArmBend = part.xRot);
		rigModel.jojo_ripples$getAnyDescendantWithName("right_arm_bend").ifPresent(part -> TmpBendCrutches.rightArmBend = part.xRot);
		rigModel.jojo_ripples$getAnyDescendantWithName("left_leg_bend").ifPresent(part -> TmpBendCrutches.leftLegBend = part.xRot);
		rigModel.jojo_ripples$getAnyDescendantWithName("right_leg_bend").ifPresent(part -> TmpBendCrutches.rightLegBend = part.xRot);
		rigModel.jojo_ripples$getAnyDescendantWithName("torso_bend").ifPresent(part -> TmpBendCrutches.torsoBend = part.xRot);
	}
	
	public static void modifyVertices(String modelPartName, List<ModelPart.Cube> deformableCubes) {
		for (ModelPart.Cube _cube : deformableCubes) {
			if (_cube instanceof DeformableCube cube) {
				cube.reset();
				switch (modelPartName) {
					case "left_arm" -> {
						connectVertices(cube, leftArmBend, -1, 4, 0, false);
					}
					case "left_arm_bend" -> {
						connectVertices(cube, leftArmBend, 0, 0, 0, true);
					}
					case "right_arm" -> {
						connectVertices(cube, rightArmBend, 1, 4, 0, false);
					}
					case "right_arm_bend" -> {
						connectVertices(cube, rightArmBend, 0, 0, 0, true);
					}
					case "left_leg" -> {
						connectVertices(cube, leftLegBend, 0, 6, 0, false);
					}
					case "left_leg_bend" -> {
						connectVertices(cube, leftLegBend, 0, 0, 0, true);
					}
					case "right_leg" -> {
						connectVertices(cube, rightLegBend, 0, 6, 0, false);
					}
					case "right_leg_bend" -> {
						connectVertices(cube, rightLegBend, 0, 0, 0, true);
					}
					case "torso" -> {
						connectVertices(cube, torsoBend, 0, 0, 0, false);
					}
					case "torso_bend" -> {
						connectVertices(cube, torsoBend, 0, 0, 0, true);
					}
				}
			}
		}
	}
	
	static void connectVertices(DeformableCube cube, float bend, float bendX, float bendY, float bendZ, boolean isBendPart) {
		if (bend == 0) return;
		
		float width = (cube.maxZ - cube.minZ) / 2;
		float yDiff = width * MathUtil.tan(bend / 2);
		for (DeformableQuad quad : cube.dQuads) {
			for (DeformableVertex vertex : quad.vertices) {
				if (vertex.pos.y == bendY) {
					if (vertex.pos.z - bendZ < 0) {
						if (isBendPart)	vertex.pos.y -= yDiff;
						else			vertex.pos.y += yDiff;
					}
					else {
						if (isBendPart)	vertex.pos.y += yDiff;
						else			vertex.pos.y -= yDiff;
					}
				}
			}
		}
	}
	
}
