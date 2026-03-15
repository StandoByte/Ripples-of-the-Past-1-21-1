package com.github.standobyte.jojo.client.entityanim.playerbend;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.v1_21_4_stuff.OldPlayerModelJank;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

// TODO (player animation) fix model bends with smaller child cubes
// TODO (player animation) fix model bends with StuckInBodyLayer
// TODO (player animation) parent xrot bones for limbs
// TODO (player animation) 1st person animation
public class PlayerModelBends {
	
	public static float getLimbHeight(ModelPart limb) {
		if (limb.cubes.isEmpty()) {
			return 12;
		}
		ModelPart.Cube cube = limb.cubes.get(0);
		return cube.maxY - cube.minY;
	}
	
	@Nullable
	public static ModelPart getModelPartForPlayerAnim(HumanoidModel<?> playerModel, String animBoneName) {
		return switch (animBoneName) {
			case "body" -> 				((IPlayerBendModel) playerModel).jojo_ripples$animMainBody();
			case "torso" -> 			((IPlayerBendModel) playerModel).jojo_ripples$animTorso();
			case "left_arm" -> 			playerModel.leftArm;
			case "right_arm" -> 		playerModel.rightArm;
			case "left_leg" ->			playerModel.leftLeg;
			case "right_leg" -> 		playerModel.rightLeg;
			case "head" -> 				playerModel.head;
			case "torso_bend" -> 		((IPlayerBendModel) playerModel).jojo_ripples$animTorsoBend();
			case "left_arm_bend" -> 	((IPlayerBendModel) playerModel).jojo_ripples$animLeftArmBend();
			case "right_arm_bend" -> 	((IPlayerBendModel) playerModel).jojo_ripples$animRightArmBend();
			case "left_leg_bend" -> 	((IPlayerBendModel) playerModel).jojo_ripples$animLeftLegBend();
			case "right_leg_bend" -> 	((IPlayerBendModel) playerModel).jojo_ripples$animRightLegBend();
			case "leftItem" -> 			((IPlayerBendModel) playerModel).jojo_ripples$animLeftItem();
			case "rightItem" -> 		((IPlayerBendModel) playerModel).jojo_ripples$animRightItem();
//			case "cape" -> 				playerModel.body.children.get("cape");
			case "cape" -> 				((Model_1_21_2plus) playerModel).jojo_ripples$root().children.get("cloak");
			case "cape_bend" -> 		((IPlayerBendModel) playerModel).jojo_ripples$animCapeBend();
			default -> null;
		};
	}

	public static void renderWithBends(HumanoidModel<?> model, IPlayerBendModel animModel, 
			PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color) {
		PlayerModel<?> playerModel = model instanceof PlayerModel pm/*o*/ ? pm : null;
		poseStack.pushPose();
			ModelPart body = animModel.jojo_ripples$animMainBody();
			body.translateAndRotate(poseStack);
			poseStack.translate(-body.getInitialPose().x / 16, -body.getInitialPose().y / 16, -body.getInitialPose().z / 16);
			
			model.leftLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
			if (playerModel != null && playerModel.leftPants != null) OldPlayerModelJank._renderOuterLayer(playerModel.leftPants, poseStack, buffer, packedLight, packedOverlay, color);
			model.rightLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
			if (playerModel != null && playerModel.rightPants != null) OldPlayerModelJank._renderOuterLayer(playerModel.rightPants, poseStack, buffer, packedLight, packedOverlay, color);
			poseStack.pushPose();
				ModelPart torso = animModel.jojo_ripples$animTorso();
				poseStack.translate(-torso.getInitialPose().x / 16, -torso.getInitialPose().y / 16, -torso.getInitialPose().z / 16);
				torso.translateAndRotate(poseStack);

				ModelPart torsoBend = animModel.jojo_ripples$animTorsoBend();
				rotateAndTranslateBack(torsoBend, poseStack);
				
				model.body.render(poseStack, buffer, packedLight, packedOverlay, color);
				if (playerModel != null && playerModel.jacket != null) OldPlayerModelJank._renderOuterLayer(playerModel.jacket, poseStack, buffer, packedLight, packedOverlay, color);
				model.head.render(poseStack, buffer, packedLight, packedOverlay, color);
				if (playerModel != null && playerModel.hat != null) OldPlayerModelJank._renderOuterLayer(playerModel.hat, poseStack, buffer, packedLight, packedOverlay, color);
				animModel.jojo_ripples$leftArm().render(poseStack, buffer, packedLight, packedOverlay, color);
				if (playerModel != null && playerModel.leftSleeve != null) OldPlayerModelJank._renderOuterLayer(playerModel.leftSleeve, poseStack, buffer, packedLight, packedOverlay, color);
				animModel.jojo_ripples$rightArm().render(poseStack, buffer, packedLight, packedOverlay, color);
				if (playerModel != null && playerModel.rightSleeve != null) OldPlayerModelJank._renderOuterLayer(playerModel.rightSleeve, poseStack, buffer, packedLight, packedOverlay, color);
			poseStack.popPose();
		poseStack.popPose();
	}
	
	
	public static void translateToAnimHand1(HumanoidModel<?> model, IPlayerBendModel animModel, HumanoidArm side, PoseStack poseStack) {
		ModelPart body = animModel.jojo_ripples$animMainBody();
		body.translateAndRotate(poseStack);
		poseStack.translate(-body.getInitialPose().x / 16, -body.getInitialPose().y / 16, -body.getInitialPose().z / 16);

		ModelPart torso = animModel.jojo_ripples$animTorso();
		poseStack.translate(-torso.getInitialPose().x / 16, -torso.getInitialPose().y / 16, -torso.getInitialPose().z / 16);
		torso.translateAndRotate(poseStack);

		ModelPart torsoBend = animModel.jojo_ripples$animTorsoBend();
		rotateAndTranslateBack(torsoBend, poseStack);
	}
	
	// ...then the vanilla does ModelPart#translateAndRotate(PoseStack) on the arm...
	
	public static void translateToAnimHand2(HumanoidModel<?> model, IPlayerBendModel animModel, HumanoidArm side, PoseStack poseStack) {
		ModelPart armBend = switch (side) {
			case LEFT -> animModel.jojo_ripples$animLeftArmBend();
			case RIGHT -> animModel.jojo_ripples$animRightArmBend();
		};
		rotateAndTranslateBack(armBend, poseStack);
		
		ModelPart itemRepos = switch (side) {
			case LEFT -> animModel.jojo_ripples$animLeftItem();
			case RIGHT -> animModel.jojo_ripples$animRightItem();
		};
		rotateAndTranslateBack(itemRepos, poseStack);
	}
	
	
	public static void repositionCloak(HumanoidModel<?> model, IPlayerBendModel animModel, PoseStack poseStack) {
		ModelPart body = animModel.jojo_ripples$animMainBody();
		body.translateAndRotate(poseStack);
		poseStack.translate(-body.getInitialPose().x / 16, -body.getInitialPose().y / 16, -body.getInitialPose().z / 16);

		ModelPart torso = animModel.jojo_ripples$animTorso();
		poseStack.translate(-torso.getInitialPose().x / 16, -torso.getInitialPose().y / 16, -torso.getInitialPose().z / 16);
		torso.translateAndRotate(poseStack);

		ModelPart torsoBend = animModel.jojo_ripples$animTorsoBend();
		rotateAndTranslateBack(torsoBend, poseStack);
	}
	
	
	public static void rotateAndTranslateBack(ModelPart modelPart, PoseStack poseStack) {
		poseStack.translate(-modelPart.getInitialPose().x / 16, -modelPart.getInitialPose().y / 16, -modelPart.getInitialPose().z / 16);
		poseStack.translate(modelPart.x / 16.0F * 2, modelPart.y / 16.0F * 2, modelPart.z / 16.0F * 2);
		if (modelPart.xRot != 0.0F || modelPart.yRot != 0.0F || modelPart.zRot != 0.0F) {
			poseStack.mulPose(new Quaternionf().rotationZYX(modelPart.zRot, modelPart.yRot, modelPart.xRot));
		}
		poseStack.translate(-modelPart.x / 16.0F, -modelPart.y / 16.0F, -modelPart.z / 16.0F);
	}
	
	
	
	public static void drawBentCubes(ModelPart limb, ModelPart bend, boolean invertBend, 
			float bendOffsetX, float bendOffsetY, float bendOffsetZ, 
			boolean skipDraw, PoseStack poseStack, 
			VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		if (!skipDraw) {
			if (invertBend) {
				bend.xRot = -bend.xRot;
			}
			for (ModelPart.Cube cube : limb.cubes) {
//				cube.compile(poseStack.last(), buffer, packedLight, packedOverlay, color);
				renderBentPolygons(cube.polygons, poseStack, bend,
						bendOffsetX, bendOffsetY, bendOffsetZ, 
						buffer, packedLight, packedOverlay, color);
			}
			if (invertBend) {
				bend.xRot = -bend.xRot;
			}
		}
		
		for (ModelPart modelpart : limb.children.values()) {
			if (modelpart != bend) {
				modelpart.render(poseStack, buffer, packedLight, packedOverlay, color);
			}
		}
		poseStack.pushPose();
		bend.translateAndRotate(poseStack);
		for (ModelPart modelpart : bend.children.values()) {
			modelpart.render(poseStack, buffer, packedLight, packedOverlay, color);
		}
		poseStack.popPose();
	}
	
	// XXX (player anim) use the main cube height (12 in case of players) instead of the individual cube heights for bending
	private static Vector3f dest = new Vector3f();
	private static void renderBentPolygons(ModelPart.Polygon[] polygons, PoseStack poseStack, ModelPart bend,
			float bendOffsetX, float bendOffsetY, float bendOffsetZ, 
			VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		for (ModelPart.Polygon polygon : polygons) {
			Vector3f normal = polygon.normal;
			
			ModelPart.Vertex[] vertices = polygon.vertices;
			int vert = vertices.length;
			MutablePolygon upperRectangle = vert == 4 ? RECTANGLE1 : new MutablePolygon(vert);
			MutablePolygon lowerRectangle = vert == 4 ? RECTANGLE2 : new MutablePolygon(vert);
			float bendY = bend.y / 16;
			float bendZ = bend.z / 16;
			boolean upperHalf = fillCutRectangle(vertices, upperRectangle, -Float.MAX_VALUE, bendY);
			boolean lowerHalf = fillCutRectangle(vertices, lowerRectangle, bendY, Float.MAX_VALUE);
			if (upperHalf && lowerHalf) {
				float width = Math.abs(vertices[0].pos.z / 16);
				float yDiff = width * MathUtil.tan(bend.xRot / 2);
				for (var vertex : upperRectangle.vertices) {
					if (vertex.y == bendY) {
						if (vertex.z - bendZ > 0) 	vertex.y -= yDiff;
						else						vertex.y += yDiff;
					}
				}
				for (var vertex : lowerRectangle.vertices) {
					if (vertex.y == bendY) {
						if (vertex.z - bendZ > 0) 	vertex.y += yDiff;
						else						vertex.y -= yDiff;
					}
				}
			}
			if (upperHalf) {
				renderPolygon(poseStack.last(), normal, upperRectangle.vertices, 
						buffer, packedLight, packedOverlay, color);
			}
			poseStack.pushPose();
			bend.translateAndRotate(poseStack);
			poseStack.translate(0, -bend.getInitialPose().y / 16, 0);
			if (lowerHalf) {
				renderPolygon(poseStack.last(), normal, lowerRectangle.vertices, 
						buffer, packedLight, packedOverlay, color);
			}
			poseStack.popPose();
		}
	}
	
	private static boolean fillCutRectangle(ModelPart.Vertex[] vertices, MutablePolygon rectangle, float minLimitY, float maxLimitY) {
		BendVertex[] targetArr = rectangle.vertices;
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		float minV = Float.MAX_VALUE;
		float maxV = -Float.MAX_VALUE;
		for (int i = 0; i < vertices.length; i++) {
			ModelPart.Vertex vertex = vertices[i];
			BendVertex target = targetArr[i];
			Vector3f vertexPos = vertex.pos;
			target.x = vertexPos.x / 16.0F;
			target.y = vertexPos.y / 16.0F;
			target.z = vertexPos.z / 16.0F;
			target.u = vertex.u;
			target.v = vertex.v;
			minY = Math.min(minY, target.y);
			maxY = Math.max(maxY, target.y);
			minV = Math.min(minV, target.v);
			maxV = Math.max(maxV, target.v);
		}
		if (maxY < minLimitY || minY > maxLimitY) { // not render the faces that do not belong to this half of the limb
			return false;
		}
		for (BendVertex vertex : targetArr) {
			if (vertex.y < minLimitY) {
				vertex.y = minLimitY;
				vertex.wasChanged = true;
			}
			else if (vertex.y > maxLimitY) {
				vertex.y = maxLimitY;
				vertex.wasChanged = true;
			}
			else {
				vertex.wasChanged = false;
			}
			if (vertex.wasChanged) { // remap the UV accordingly
				float yRatio = (vertex.y - minY) / (maxY - minY);
				vertex.v = minV + (maxV - minV) * yRatio;
			}
			rectangle.minY = Math.max(minY, minLimitY);
			rectangle.maxY = Math.min(maxY, maxLimitY);
		}
		return true;
	}
	
	// XXX try to fix the bent quad rendering
	/* 
	 * it's drawing a rectangle/quad as two triangles split by a diagonal line, 
	 * when the quad is bent - both individual triangles are transformed correctly, 
	 * but the resulting quad looks broken
	 */
	private static void renderPolygon(PoseStack.Pose pose, Vector3f normalVec, BendVertex[] vertices, 
			VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		dest.set(0);
		Matrix4f poseMatrix = pose.pose();
		Vector3f normal = pose.transformNormal(normalVec, dest);
		float normalX = normal.x;
		float normalY = normal.y;
		float normalZ = normal.z;

		for (int i = 0; i < vertices.length; i++) {
			BendVertex vertex = vertices[i];
			Vector3f pos = poseMatrix.transformPosition(vertex.x, vertex.y, vertex.z, dest);
			buffer.addVertex(
				pos.x, pos.y, pos.z, color, 
				vertex.u, vertex.v, packedOverlay, packedLight, 
				normalX, normalY, normalZ
			);
		}
	}
	
	private static final MutablePolygon RECTANGLE1 = new MutablePolygon(4);
	private static final MutablePolygon RECTANGLE2 = new MutablePolygon(4);

	@ApiStatus.Internal
	public static class MutablePolygon {
		final BendVertex[] vertices;
		float minY;
		float maxY;
		
		MutablePolygon(int verticesCount) {
			this.vertices = Util.make(new BendVertex[verticesCount], arr -> {
				for (int i = 0; i < arr.length; i++) arr[i] = new BendVertex();
			});
		}
	}

	@ApiStatus.Internal
	public static class BendVertex {
		float x;
		float y;
		float z;
		float u;
		float v;
		boolean wasChanged;
	}
	
}
