package com.github.standobyte.jojo.client.entityrender.entities;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import com.github.standobyte.jojo.client.rendertype.ModRenderTypes;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.sidecontent.item.tommygun.BulletEntity;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BulletRenderer extends EntityRenderer<BulletEntity> {

	public BulletRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(BulletEntity entity) {
		return TRAIL_TEX;
	}

	@Override
	public boolean shouldRender(BulletEntity entity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
		return super.shouldRender(entity, pCamera, pCamX, pCamY, pCamZ) || 
				entity.initialPos != null && pCamera.isVisible(new AABB(entity.initialPos, entity.position()));
	}

	@Override
	public void render(BulletEntity entity, float yRotation, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		renderBullet(entity, entity.tracePos, 
				4, 0.015f, 
				getTextureLocation(entity), 0xFFFFFFB9, 0xFFFF9C00, 
				poseStack, buffer);
		super.render(entity, yRotation, partialTick, poseStack, buffer, packedLight);
	}

	public static final ResourceLocation TRAIL_TEX = JojoMod.resLoc("textures/entity/bullet_trace.png");
	public static final float V1 = 0.015625f;
	public static final float BULLET_U = 0.01953125f;
	
	public static void renderBullet(Entity bulletEntity, List<Vec3> trace, 
			double maxTrailLen, float beamWidth, 
			ResourceLocation texture, int colorBullet, int colorBulletTrace, 
			PoseStack poseStack, MultiBufferSource buffer) {
		double bulletLen = maxTrailLen * BULLET_U;
		if (trace.isEmpty()) {
			trace = Util.make(new ArrayList<>(), list -> {
				Vec3 pos = bulletEntity.position();
				list.add(pos.subtract(bulletEntity.getDeltaMovement().normalize().scale(bulletLen)));
				list.add(pos);
			});
		}

		poseStack.pushPose();
		poseStack.translate(0, bulletEntity.getBbHeight() / 2, 0);
		// TODO use vanilla entityTranslucent with Iris shader enabled
		VertexConsumer vertexBuilder = buffer.getBuffer(ModRenderTypes.entityBulletTrail(texture));

		double traceLen = maxTrailLen;
		int i;
		
		Vec3 pos = bulletEntity.position();
		Vec3 deltaMovement = bulletEntity.getDeltaMovement();
		if (deltaMovement.lengthSqr() < 1.0E-4) {
			deltaMovement = bulletEntity.getLookAngle();
		}
		else {
			deltaMovement = deltaMovement.normalize();
		}
		Vec3 pos2 = bulletEntity.position().add(deltaMovement.scale(bulletLen));
		trailSegment(pos, pos2, 1 - BULLET_U, 1, 
				poseStack, vertexBuilder, 
				bulletEntity, true, colorBullet, beamWidth);
		
		for (i = trace.size() - 1; i > 0 && traceLen > 0; i--) {
			Vec3 posCur = trace.get(i);
			Vec3 posPrev = trace.get(i - 1);
			float u0;
			float u1 = (float) (traceLen / maxTrailLen);

			Vec3 diffBack = posPrev.subtract(posCur);
			double len = diffBack.length();

			if (len > traceLen) {
				posPrev = posCur.add(diffBack.normalize().scale(traceLen));
				traceLen = 0;
			}
			else {
				traceLen -= len;
			}
			u0 = (float) (traceLen / maxTrailLen);

			u0 *= (1 - BULLET_U);
			u1 *= (1 - BULLET_U);
			trailSegment(posPrev, posCur, u0, u1, 
					poseStack, vertexBuilder, 
					bulletEntity, false, colorBulletTrace, beamWidth);
		}

		poseStack.popPose();
	}

	public static void trailSegment(Vec3 pos1, Vec3 pos2, float u0, float u1, 
			PoseStack poseStack, VertexConsumer vertexBuilder, 
			Entity entity, boolean first, int color, float beamWidth) {
		poseStack.pushPose();
		Vec3 trailSegmentVec = pos1.subtract(pos2);
		float yRot = MathUtil.yRotDegFromVec(trailSegmentVec);
		float xRot = MathUtil.xRotDegFromVec(trailSegmentVec);
		poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F - yRot));
		poseStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
		poseStack.scale(1.0F, beamWidth, beamWidth);
		Matrix3f lighting = poseStack.last().normal();
		lighting.m00(1).m01(0).m02(0).m10(0).m11(1).m12(0).m20(0).m21(0).m22(1); // set identity
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		lighting.rotate(Axis.XP.rotationDegrees(camera.getXRot()));
		float length = (float) trailSegmentVec.length();

		if (first) {
			renderFront(poseStack, new Vector3f(0, 0, 1), color, vertexBuilder);
		}
		renderSide(poseStack, new Vector3f(0, -1,  0),  length, u0, u1, color, vertexBuilder);
		renderSide(poseStack, new Vector3f(0,  0, -1),  length, u0, u1, color, vertexBuilder);
		renderSide(poseStack, new Vector3f(0,  1,  0),  length, u0, u1, color, vertexBuilder);
		renderSide(poseStack, new Vector3f(0,  0,  1),  length, u0, u1, color, vertexBuilder);

		poseStack.popPose();
		poseStack.translate(trailSegmentVec.x, trailSegmentVec.y, trailSegmentVec.z);
	}


	public static void renderSide(PoseStack poseStack, Vector3f lightNormal, float length, float u0, float u1, int color, VertexConsumer vertexBuilder) {
		int packedLight = ClientUtil.MAX_LIGHT;
		float v0 = 0;
		float v1 = V1;
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.pushPose();

		poseStack.translate(0, 0, 1f);

		PoseStack.Pose pose = poseStack.last();
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				0, -1, 0, 
				u1, v0, 
				lightNormal.x(), lightNormal.y(), lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				length, -1, 0, 
				u0, v0, 
				lightNormal.x(), lightNormal.y(), lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				length, 1, 0, 
				u0, v1, 
				lightNormal.x(), lightNormal.y(), lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				0, 1, 0, 
				u1, v1, 
				lightNormal.x(), lightNormal.y(), lightNormal.z());

//		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
//		vertex(pose, vertexBuilder, 
//				packedLight, OverlayTexture.NO_OVERLAY, color, 
//				0, -1, 0, 
//				u1, v0, 
//				-lightNormal.x(), -lightNormal.y(), -lightNormal.z());
//		vertex(pose, vertexBuilder, 
//				packedLight, OverlayTexture.NO_OVERLAY, color, 
//				length, -1, 0, 
//				u0, v0, 
//				-lightNormal.x(), -lightNormal.y(), -lightNormal.z());
//		vertex(pose, vertexBuilder, 
//				packedLight, OverlayTexture.NO_OVERLAY, color, 
//				length, 1, 0, 
//				u0, v1, 
//				-lightNormal.x(), -lightNormal.y(), -lightNormal.z());
//		vertex(pose, vertexBuilder, 
//				packedLight, OverlayTexture.NO_OVERLAY, color, 
//				0, 1, 0, 
//				u1, v1, 
//				-lightNormal.x(), -lightNormal.y(), -lightNormal.z());

		poseStack.popPose();
	}

	public static void renderFront(PoseStack poseStack, Vector3f lightNormal, int color, VertexConsumer vertexBuilder) {
		int packedLight = ClientUtil.MAX_LIGHT;
		float u0 = 0;
		float u1 = u0 + V1;
		float v0 = V1;
		float v1 = v0 + V1;
		poseStack.pushPose();

		poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

		PoseStack.Pose pose = poseStack.last();
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				-1, -1, 0, 
				u1, v0, 
				lightNormal.x(), lightNormal.y(), lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				1, -1, 0, 
				u0, v0, 
				lightNormal.x(), lightNormal.y(), lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				1, 1, 0, 
				u0, v1, 
				lightNormal.x(), lightNormal.y(), lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				-1, 1, 0, 
				u1, v1, 
				lightNormal.x(), lightNormal.y(), lightNormal.z());

		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				-1, -1, 0, 
				u1, v0, 
				-lightNormal.x(), -lightNormal.y(), -lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				1, -1, 0, 
				u0, v0, 
				-lightNormal.x(), -lightNormal.y(), -lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				1, 1, 0, 
				u0, v1, 
				-lightNormal.x(), -lightNormal.y(), -lightNormal.z());
		vertex(pose, vertexBuilder, 
				packedLight, OverlayTexture.NO_OVERLAY, color, 
				-1, 1, 0, 
				u1, v1, 
				-lightNormal.x(), -lightNormal.y(), -lightNormal.z());

		poseStack.popPose();

	}
	
	
	public static void vertex(PoseStack.Pose pose, VertexConsumer vertexBuilder, 
			int packedLight, int packedOverlay, int color, 
			float offsetX, float offsetY, float offsetZ, 
			float texU, float texV, 
			float normalX, float normalY, float normalZ) {
		vertexBuilder
		.addVertex(pose, offsetX, offsetY, offsetZ)
		.setColor(color)
		.setUv(texU, texV)
		.setOverlay(packedOverlay)
		.setLight(packedLight)
		.setNormal(pose, normalX, normalZ, normalY)
		;//.endVertex();
	}
}
