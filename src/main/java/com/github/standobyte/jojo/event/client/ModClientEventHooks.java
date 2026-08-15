package com.github.standobyte.jojo.event.client;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.LivingAnimState;
import com.github.standobyte.jojo.client.firstperson.FirstPersonRender;
import com.github.standobyte.jojo.mixin.client.firstperson.CameraAccessor;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

@ApiStatus.Internal
public abstract class ModClientEventHooks {
	
	public static ReplacePlayerModelEvent preRenderReplacePlayerModel(LivingEntity entity, 
			LivingEntityRenderer<?, ? extends PlayerModel<?>> renderer, 
			float partialTick, LivingAnimState animVariables) {
		return NeoForge.EVENT_BUS.post(new ReplacePlayerModelEvent(entity, renderer, partialTick, animVariables));
	}

	public static void afterCameraSetup(Camera camera, boolean thirdPerson, boolean thirdPersonReverse) {
		Vec3 cameraOffset = FirstPersonRender.onCameraOffsetSetup(camera, thirdPerson);
		
		RipplesCameraPostSetupEvent event = new RipplesCameraPostSetupEvent(camera, 
				thirdPerson, thirdPersonReverse, cameraOffset);
		event = NeoForge.EVENT_BUS.post(event);
		cameraOffset = event.getCameraOffset();
		
		if (cameraOffset.x != 0 || cameraOffset.y != 0 || cameraOffset.z != 0) {
			CameraAccessor camera_ = (CameraAccessor) camera;
			camera_.invokeSetPosition(camera.getPosition().add(cameraOffset));
		}
	}

	public static RipplesFirstPersonRenderEarlyEvent preFirstPersonRender(Entity povEntity, 
			float partialTick, PoseStack poseStack, BufferSource bufferSource, int light) {
		RipplesFirstPersonRenderEarlyEvent event = new RipplesFirstPersonRenderEarlyEvent(povEntity, 
				partialTick, poseStack, bufferSource, light);
		NeoForge.EVENT_BUS.post(event);
		return event;
	}

	public static boolean onKeyboardInputPre(int key, int scanCode, int action, int modifiers) {
		return NeoForge.EVENT_BUS.post(new PreKeyInputEvent(key, scanCode, action, modifiers)).isCanceled();
	}
}
