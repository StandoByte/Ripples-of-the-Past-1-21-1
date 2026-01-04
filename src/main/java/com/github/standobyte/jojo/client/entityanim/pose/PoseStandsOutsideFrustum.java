package com.github.standobyte.jojo.client.entityanim.pose;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.utils.ModelUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class PoseStandsOutsideFrustum {

	@SubscribeEvent
	public static void onRenderFrame(RenderLevelStageEvent event) {
		RenderLevelStageEvent.Stage stage = event.getStage();
		boolean beforeEntities = stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS;
		if (beforeEntities) {
			ClientLevel level = Minecraft.getInstance().level;
			for (Entity entity : level.entitiesForRendering()) {
				((EntityKeepAnimPose) entity).jojo_ripples$preFrameRender();
			}
		}
		else if (stage == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			ClientLevel level = Minecraft.getInstance().level;
			for (Entity entity : level.entitiesForRendering()) {
				EntityKeepAnimPose _entity = (EntityKeepAnimPose) entity;
				if (_entity.jojo_ripples$keepsModelPose() && _entity.jojo_ripples$getModelPose() == null) {
					EntityRenderer renderer = ModelUtil.getEntityRenderer(entity);
					if (renderer instanceof StandEntityRenderer standRenderer) {
						standRenderer.pose((StandEntity) entity, ClientUtil.partialTick(event.getPartialTick(), entity));
					}
				}
			}
		}
	}
}
