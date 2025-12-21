package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.client;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL14;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.rendertype.CustomMultiBufferSource;
import com.github.standobyte.jojo.client.ui.powerhud.PowerHud;
import com.github.standobyte.jojo.client.ui.powerhud.PowerHud.AbilityHud;
import com.github.standobyte.jojo.client.utils.ARGBUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.brokenblocks.PrevBlockInfo;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

// A helper class for rendering translucent blocks overlay
// as a quality-of-life feature for Crazy Diamond's terrain restoration ability

// The original idea of remapping render types is taken from
// MultiblockVisualizationHandler class from Vazkii's Patchouli mod
// (licensed under CC BY-NC-SA 3.0)
@EventBusSubscriber(value = Dist.CLIENT, modid = JojoMod.MOD_ID)
public class TranslucentBlockRenderHelper {
	private static MultiBufferSource.BufferSource buffers = null;

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void renderBlocksOverlay(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
			Minecraft mc = Minecraft.getInstance();
			AbilityHud hud = PowerHud.abilityHUDInstance;
			if (hud.isAbilitySelected("restore_terrain")) {
				PoseStack poseStack = event.getPoseStack();
				StandPower stand = ClientPowerCache.getPower(PowerClass.STAND);
				Entity entity = CrazyDRestoreTerrainAbility.restorationCenterEntity(mc.player, stand);
				Vec3i pos = CrazyDRestoreTerrainAbility.eyePos(entity);
				Vec3 lookVec = entity.getLookAngle();
				Vec3 eyePosD = entity.getEyePosition(1.0F);
				boolean resolveEffect = ModStatusEffects.isInResolveEffect(mc.player);
				int manhattanRange = CrazyDRestoreTerrainAbility.restorationDistManhattan(resolveEffect);
				Stream<Map.Entry<BlockPos, PrevBlockInfo>> allFixableBlocks = CrazyDRestoreTerrainAbility.getBrokenBlocksInRange(mc.level, mc.player, pos, 32, 
								block -> CrazyDRestoreTerrainAbility.blockCanBePlaced(mc.level, block.pos, block.state));
				Predicate<PrevBlockInfo> inAbilityRange = block -> CrazyDRestoreTerrainAbility.blockPosSelectedForRestoration(block.pos, entity, 
						lookVec, eyePosD, pos, manhattanRange, 
						resolveEffect, mc.player.isShiftKeyDown());
				TranslucentBlockRenderHelper.renderCDRestorationTranslucentBlocks(poseStack, mc, 
						allFixableBlocks, inAbilityRange);
			}
		}
	}

	public static void renderCDRestorationTranslucentBlocks(PoseStack poseStack, Minecraft mc, 
			Stream<Map.Entry<BlockPos, PrevBlockInfo>> blocks, Predicate<PrevBlockInfo> inAbilityRange) {
		if (buffers == null) {
			RenderStateShard.OutputStateShard targetShard = new RenderStateShard.OutputStateShard(
					"crazy_d_blocks", 
					() -> {
						RenderSystem.disableDepthTest();
						RenderSystem.enableBlend();
						RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
						GL14.glBlendColor(1, 1, 1, 0.3f);
					}, 
					() -> {
						GL14.glBlendColor(1, 1, 1, 1);
						RenderSystem.defaultBlendFunc();
						RenderSystem.disableBlend();
						RenderSystem.enableDepthTest();
					});
			
			
			buffers = CustomMultiBufferSource.create(mc, targetShard);
		}

		Camera camera = mc.gameRenderer.getMainCamera();
		Vec3 projectedView = camera.getPosition();
		poseStack.pushPose();
		
		poseStack.mulPose(Axis.ZP.rotationDegrees(camera.getRoll()));
		poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
		poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() - 180));
		
		poseStack.translate(
				-projectedView.x(), 
				-projectedView.y(), 
				-projectedView.z());

		BlockRenderDispatcher renderer = mc.getBlockRenderer();
		int overlayFade = Math.abs((int) (Util.getMillis() % 2000) / 100 - 10);
		int overlayTexture = OverlayTexture.pack(overlayFade, 10);
		blocks.forEach(blockEntry -> {
			PrevBlockInfo block = blockEntry.getValue();
			BlockPos pos = block.pos;
			BlockState blockState = block.state;
			ModelData tileData = mc.level.getModelData(pos);
			BakedModel bakedModel = renderer.getBlockModel(blockState);
			ModelData model = bakedModel.getModelData(mc.level, pos, blockState, tileData);
			poseStack.pushPose();
			poseStack.translate(
					pos.getX(), 
					pos.getY(), 
					pos.getZ());
			int overlay = inAbilityRange.test(block) ? overlayTexture : OverlayTexture.NO_OVERLAY;

			RenderShape renderShape = blockState.getRenderShape();
			if (renderShape == RenderShape.MODEL) {
				int color = mc.getBlockColors().getColor(blockState, mc.level, pos, 0);
				for (RenderType renderType : bakedModel.getRenderTypes(blockState, RandomSource.create(42), model)) {
					renderer.getModelRenderer().renderModel(poseStack.last(), buffers.getBuffer(renderType), 
							blockState, bakedModel, 
							ARGBUtil.red(color), ARGBUtil.green(color), ARGBUtil.blue(color), 
							ClientUtil.MAX_LIGHT, overlay, model, null);
				}
			}

			poseStack.popPose();
		});

		buffers.endBatch();
		poseStack.popPose();
	}
}
