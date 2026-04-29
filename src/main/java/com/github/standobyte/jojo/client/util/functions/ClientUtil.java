package com.github.standobyte.jojo.client.util.functions;

import com.github.standobyte.jojo.client.ClientTickHandler;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopClientState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;

public class ClientUtil {
	public static final int MAX_LIGHT = 0xF000F0;
	public static final int NO_OVERLAY = OverlayTexture.NO_OVERLAY;

	public static void setCameraEntityPreventShaderSwitch(Entity entity) {
		Minecraft mc = Minecraft.getInstance();
		mc.setCameraEntity(entity);
		// TODO prevent shader switch
//		if (mc.gameRenderer.currentEffect() == null) {
//			ShaderEffectApplier.getInstance().updateCurrentShader();
//		}
	}

	public static float partialTick() {
		return partialTick(Minecraft.getInstance().getTimer(), false);
	}

	public static float partialTick(DeltaTracker deltaTracker, boolean worksInPauseToo) {
		if (worksInPauseToo) {
			return switch (deltaTracker) {
				case DeltaTracker.Timer timer -> timer.deltaTickResidual;
				case DeltaTracker.DefaultValue defaultVal -> defaultVal.getGameTimeDeltaPartialTick(false);
				default -> 0;
			};
		}
		ClientLevel level = Minecraft.getInstance().level;
		TickRateManager tickRateManager = level != null ? level.tickRateManager() : null;
		boolean runsNormally = tickRateManager == null || tickRateManager.runsNormally();
		return deltaTracker.getGameTimeDeltaPartialTick(runsNormally);
	}
	
	public static float partialTick(Entity entity) {
		Minecraft mc = Minecraft.getInstance();
		return partialTick(entity, mc.getTimer(), entity.level().tickRateManager());
	}
	
	public static float partialTick(Entity entity, DeltaTracker deltaTracker, TickRateManager tickRateManager) {
		if (TimeStopClientState.isEntityFrozen(entity)) {
			return TimeStopClientState.partialTick;
		}
		boolean isEntityFrozen = tickRateManager.isEntityFrozen(entity);
		return deltaTracker.getGameTimeDeltaPartialTick(!isEntityFrozen);
	}
	
	public static float getTime(boolean worksInPauseToo) {
		return ClientTickHandler.tickCount + partialTick(Minecraft.getInstance().getTimer(), worksInPauseToo);
	}

	public static int getScreenMouseX() {
		Minecraft mc = Minecraft.getInstance();
		return (int)(
				mc.mouseHandler.xpos()
				* (double)mc.getWindow().getGuiScaledWidth()
				/ (double)mc.getWindow().getScreenWidth());
	}

	public static int getScreenMouseY() {
		Minecraft mc = Minecraft.getInstance();
		return (int)(
				mc.mouseHandler.ypos()
				* (double)mc.getWindow().getGuiScaledHeight()
				/ (double)mc.getWindow().getScreenHeight());
	}

	// it just works
	public static float getHighlightAlpha(float ticks, float cycleTicks, float maxAlphaTicks, float minAlpha, float maxAlpha) {
		ticks %= cycleTicks;
		float coeff = maxAlpha / maxAlphaTicks;
		float alpha = ticks <= cycleTicks / 2 ? coeff * ticks : coeff * (cycleTicks - ticks);
		return Math.min(alpha, maxAlpha - minAlpha) + minAlpha;
	}

	public static void renderEntityFace(PoseStack poseStack, int x, int y, LivingEntity entity) {
		if (entity instanceof AbstractClientPlayer player) {
			renderPlayerFace(poseStack, x, y, player);
		}
	}

	public static final float OUTER_LAYER_SCALE = 9f/8f;
	public static void renderPlayerFace(PoseStack poseStack, int x, int y, AbstractClientPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		PlayerSkin playerSkin = player.getSkin();
		ResourceLocation playerFace = playerSkin.texture();
		BlitFloat.blit(poseStack, mc, playerFace, 
				x, y, 16, 16, 0, 
				8, 8, 8, 8, 64, 64, 
				BlitFloat.NO_TINT);
		if (player.isModelPartShown(PlayerModelPart.HAT)) {
			poseStack.pushPose();
			poseStack.translate(x + 8, y + 8, 0);
			poseStack.scale(OUTER_LAYER_SCALE, OUTER_LAYER_SCALE, 0);
			poseStack.translate(-x - 8, -y - 8, 0);
			BlitFloat.blit(poseStack, mc, playerFace, 
					x, y, 16, 16, 0, 
					40, 8, 8, 8, 64, 64, 
					BlitFloat.NO_TINT);
			poseStack.popPose();
		}
	}

}
