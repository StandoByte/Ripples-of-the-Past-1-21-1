package com.github.standobyte.jojo.client.ui.hud_power;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.ElementTransparency;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;

public class WindupAtCrosshair {
	public static final GuiIcon WINDUP_CROSSHAIR = new GuiIcon(JojoMod.resLoc("textures/gui/windup_crosshair.png"), 15, 15);
	public static final GuiIcon WINDUP_CROSSHAIR_FILLED = new GuiIcon(JojoMod.resLoc("textures/gui/windup_crosshair_filled.png"), 15, 15);

	public static boolean _doRender = false;
	public static WindupIndicator _renderWindup = new WindupIndicator();
	public static boolean _filledAnim = false;

	public static final ElementTransparency crosshairFillTransparency = new ElementTransparency(8, 6) {
		private static final int FADE_IN_TICKS = 2;
		@Override
		public float getValue(float partialTick) {
			if (ticksMax - ticks <= FADE_IN_TICKS) {
				return (ticksMax - ticks + partialTick) / FADE_IN_TICKS;
			}
			return super.getValue(partialTick);
		}
	};

	public static void setRender(@Nullable WindupIndicator windup) {
		_doRender = windup != null;
		if (windup != null) {
			_renderWindup.copyFrom(windup);
		}
	}

	public static void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Minecraft mc) {
		if (mc.options.hideGui || !mc.options.getCameraType().isFirstPerson() || mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			return;
		}
		
		if (_renderWindup == null || _renderWindup.maxValue <= 0 || _renderWindup.value <= 0) {
			_filledAnim = false;
			return;
		}

		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		float width = WINDUP_CROSSHAIR.width;
		float height = WINDUP_CROSSHAIR.height;
		float x = (int) ((mc.getWindow().getGuiScaledWidth() - width) / 2);
		float y = (int) ((mc.getWindow().getGuiScaledHeight() - height) / 2);
		poseStack.translate(x + width / 2, y + height / 2, 0);
		x = -width / 2;
		y = -height / 2;

		float partialTick = ClientUtil.partialTick(deltaTracker, false);

		float heldActionRatio = Mth.clamp(((float) _renderWindup.value + partialTick) / _renderWindup.maxValue, 0, 1);
		if (heldActionRatio > 0) {
			if (heldActionRatio < 1) {
				_filledAnim = false;
			}
			else if (!_filledAnim) {
				_filledAnim = true;
				crosshairFillTransparency.reset();
			}

			float fillHeight = heldActionRatio == 1 ? height : 3 + 9 * heldActionRatio;
			BlitFloat.blit(guiGraphics.pose(), mc, WINDUP_CROSSHAIR.file, 
					x, y + height - fillHeight, width, fillHeight, 0, 
					0, height - fillHeight, width, fillHeight, width, height, 
					BlitFloat.NO_TINT);
		}

		if (crosshairFillTransparency.shouldRender()) {
			float alpha = crosshairFillTransparency.getAlpha(partialTick);
			alpha = Math.min(alpha, 1);
			if (alpha > 0) {
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				float scale = 1 + alpha * 0.25f;
				poseStack.scale(scale, scale, 1);
				BlitFloat.blit(guiGraphics.pose(), mc, WINDUP_CROSSHAIR_FILLED.file, 
						x, y, width, height, 0, 
						ARGB.white(alpha));
			}
		}

		poseStack.popPose();
	}

}
