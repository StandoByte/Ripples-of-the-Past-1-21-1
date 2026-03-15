package com.github.standobyte.jojo.mixin.client.aim;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.input.ClientsideAim;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.util.functions.RGBUtil;
import com.github.standobyte.jojo.powersystem.entityaction.ActionOBB;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.hitboxes.OrientedBoundingBox;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;
import com.github.standobyte.jojo.subsystems.target.HitResultUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLLoader;

@Mixin(EntityRenderDispatcher.class)
public class HitboxRenderingMixin {

	@Inject(method = "renderHitbox", at = @At("TAIL"))
	private static void jojo_ripples$standPrecisionBox(PoseStack poseStack, VertexConsumer buffer, Entity p_entity, float red, float green, float blue, float alpha, CallbackInfo ci) {
		if (!FMLLoader.isProduction()) {
			Minecraft mc = Minecraft.getInstance();
			if (ClientGlobals.standPrecision > 0 && ClientGlobals.playerStandEntity != null && p_entity != ClientGlobals.playerStandEntity 
					&& p_entity != mc.player && !ClientsideAim.precisionAimingDisabled(mc)) {
				AABB aabb = p_entity.getBoundingBox().move(-p_entity.getX(), -p_entity.getY(), -p_entity.getZ());
				AABB precisionAABB = HitResultUtil.standPrecisionTargetHitbox(aabb, ClientGlobals.standPrecision);
				StandSkin skin = StandSkinsLoader.getInstance().getSkin(ClientGlobals.playerStandEntity);
				if (skin != null) {
					float[] setColor = RGBUtil.rgb(skin.getColor());
					ActionTarget target = ClientsideAim.standAim.getTarget();
					if (target.getEntity() == p_entity) {
						LevelRenderer.renderLineBox(poseStack, buffer, precisionAABB, setColor[0], setColor[1], setColor[2], 1);
						
						Optional<Vec3> clipPos = target.getClipPos();
						if (clipPos.isPresent()) {
							Vec3 point = clipPos.get().subtract(p_entity.getX(), p_entity.getY(), p_entity.getZ());
							LevelRenderer.renderLineBox(
									poseStack, buffer,
									point.x - 0.01, point.y - 0.01, point.z - 0.01,
									point.x + 0.01, point.y + 0.01, point.z + 0.01,
									1.0F, 0.0F, 0.0F, 1.0F);
						}
					}
					else {
						LevelRenderer.renderLineBox(poseStack, buffer, precisionAABB, setColor[0], setColor[1], setColor[2], 0.25f);
					}
				}
			}
		}
	}

    @Inject(method = "renderHitbox", at = @At("TAIL"))
    private static void jojo_ripples$obbRender(PoseStack poseStack, VertexConsumer buffer, Entity p_entity, float red, float green, float blue, float alpha, CallbackInfo ci) {
        if (!FMLLoader.isProduction()) {
            Minecraft mc = Minecraft.getInstance();
            if (p_entity instanceof StandEntity){
                StandEntity standEntity = (StandEntity) p_entity;
                EntityActionInstance action = standEntity.getCurStandAction();
                if (action instanceof ActionOBB && ((ActionOBB)action).extendableOBB() != null){
                    drawOutline(poseStack, ((ActionOBB)action).extendableOBB().rotatableHitbox(), false);
                }
            }
        }
    }

    private static void drawOutline(PoseStack matrixStack, OrientedBoundingBox obb, /*List<OrientedBoundingBox> otherObbs,*/ boolean collides) {
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(2.0f);
        obb = obb.updateVertex();
        if (collides) {
            //System.out.println("Drawing collider +");
            outlineOBB(matrixStack, obb, bufferBuilder,
                    1, 0, 0,
                    1, 0, 0,0.5F);
        } else {
            //System.out.println("Drawing collider -");
            outlineOBB(matrixStack, obb, bufferBuilder,
                    1, 0, 0,
                    1, 0, 0,0.5F);
        }
//        look(matrixStack, obb, bufferBuilder, 0.5F);

//        for(OrientedBoundingBox otherObb: otherObbs){
//            outlineOBB(matrixStack, otherObb, bufferBuilder,
//                    1, 0, 0,
//                    1, 0, 0,0.5F);
//        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableBlend();
    }

    private static void look(PoseStack matrixStack, OrientedBoundingBox box, BufferBuilder buffer, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(0, 0, 0, alpha);

        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(1, 0, 0, alpha);
        buffer.addVertex(matrix4f, (float) (box.center.x + box.axisZ.x), (float) (box.center.y + box.axisZ.y), (float) (box.center.z + box.axisZ.z)).setColor(1, 0, 0, alpha);
        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(1, 0, 0, alpha);

        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(0, 1, 0, alpha);
        buffer.addVertex(matrix4f, (float) (box.center.x +  box.axisY.x), (float) (box.center.y +  box.axisY.y), (float) (box.center.z +  box.axisY.z)).setColor(0, 1, 0, alpha);
        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(0, 1, 0, alpha);

        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(0, 0, 1, alpha);
        buffer.addVertex(matrix4f, (float) (box.center.x +  box.axisX.x), (float) (box.center.y +  box.axisX.y), (float) (box.center.z +  box.axisX.z)).setColor(0, 0, 1, alpha);
        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(0, 0, 1, alpha);

        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(0, 0, 0, alpha);
    }

    private static void outlineOBB(PoseStack matrixStack, OrientedBoundingBox box, VertexConsumer buffer,
                            float red1, float green1, float blue1,
                            float red2, float green2, float blue2,
                            float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        buffer.addVertex(matrix4f, (float) box.vertex1.x, (float) box.vertex1.y, (float) box.vertex1.z).setColor(0, 0, 0, 0);

        buffer.addVertex(matrix4f, (float) box.vertex1.x, (float) box.vertex1.y, (float) box.vertex1.z).setColor(red1, green1, blue1, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex2.x, (float) box.vertex2.y, (float) box.vertex2.z).setColor(red1, green1, blue1, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex3.x, (float) box.vertex3.y, (float) box.vertex3.z).setColor(red1, green1, blue1, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex4.x, (float) box.vertex4.y, (float) box.vertex4.z).setColor(red1, green1, blue1, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex1.x, (float) box.vertex1.y, (float) box.vertex1.z).setColor(red1, green1, blue1, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex5.x, (float) box.vertex5.y, (float) box.vertex5.z).setColor(red2, green2, blue2, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex6.x, (float) box.vertex6.y, (float) box.vertex6.z).setColor(red2, green2, blue2, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex2.x, (float) box.vertex2.y, (float) box.vertex2.z).setColor(red1, green1, blue1, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex6.x, (float) box.vertex6.y, (float) box.vertex6.z).setColor(red2, green2, blue2, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex7.x, (float) box.vertex7.y, (float) box.vertex7.z).setColor(red2, green2, blue2, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex3.x, (float) box.vertex3.y, (float) box.vertex3.z).setColor(red1, green1, blue1, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex7.x, (float) box.vertex7.y, (float) box.vertex7.z).setColor(red2, green2, blue2, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex8.x, (float) box.vertex8.y, (float) box.vertex8.z).setColor(red2, green2, blue2, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex4.x, (float) box.vertex4.y, (float) box.vertex4.z).setColor(red1, green1, blue1, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex8.x, (float) box.vertex8.y, (float) box.vertex8.z).setColor(red2, green2, blue2, alpha);
        buffer.addVertex(matrix4f, (float) box.vertex5.x, (float) box.vertex5.y, (float) box.vertex5.z).setColor(red2, green2, blue2, alpha);

        buffer.addVertex(matrix4f, (float) box.vertex5.x, (float) box.vertex5.y, (float) box.vertex5.z).setColor(0, 0, 0, 0);
        buffer.addVertex(matrix4f, (float) box.center.x, (float) box.center.y, (float) box.center.z).setColor(0, 0, 0, 0);
    }
}
