package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.github.standobyte.jojo.client.ui.hud_misc.VanillaGuiHelper;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.mechanics.BleedingEffect;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(Gui.class)
public abstract class BleedingHealthGui {
	private float jojo_ripples$maxHealthWithBleeding;
	private float jojo_ripples$maxMountHealthWithBleeding;
	private float jojo_ripples$maxMountHealthWithoutBleeding;

	@Shadow public abstract Player getCameraPlayer();

	@ModifyVariable(method = "renderHealthLevel", at = @At("STORE"), ordinal = 0)
	public float jojo_ripples$maxHpWithBleeding(float maxHealth) {
		jojo_ripples$maxHealthWithBleeding = maxHealth;
		Player player = this.getCameraPlayer();
		if (player != null && player.hasEffect(ModStatusEffects.BLEEDING)) {
			maxHealth = (float) BleedingEffect.getMaxHealthWithoutBleeding(player);
		}
		return maxHealth;
	}

	@Inject(method = "renderHearts", at = @At(
			value = "INVOKE",
			target = "renderHeart",
			ordinal = 0), // when the "container heart" sprite is rendered
			locals = LocalCapture.CAPTURE_FAILSOFT)
	public void jojo_ripples$renderSpriteOnBleeding(GuiGraphics guiGraphics, Player player, 
			int healthX, int healthY, int height, int offsetHeartIndex, 
			float maxHealthWithoutBleeding, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci, 
			Gui.HeartType heartType, boolean hardcore, int hpHearts, int absorpHearts, int maxHealthWithoutBleedingInt, 
			int heart, int row, int column, int x, int y) {
		if (VanillaGuiHelper.shouldAddBleedingOnHeart(jojo_ripples$maxHealthWithBleeding, maxHealthWithoutBleeding, heart, currentHealth)) {
			PoseStack pose = guiGraphics.pose();
			pose.pushPose();
			pose.translate(0, 0, 1); // to render it on top of other sprites that will be rendered afterwards
			RenderSystem.enableBlend();
			guiGraphics.blitSprite(VanillaGuiHelper.HEART_BLEEDING_PLAYER, x, y, 9, 9);
			RenderSystem.disableBlend();
			pose.popPose();
		}
	}


	@ModifyVariable(method = "getVehicleMaxHearts", at = @At(value = "STORE", ordinal = 0))
	public float jojo_ripples$maxMountHpWithBleeding(float maxHealth, LivingEntity vehicle) {
		jojo_ripples$maxMountHealthWithBleeding = maxHealth;
		jojo_ripples$maxMountHealthWithoutBleeding = BleedingEffect.getMaxHealthWithoutBleeding(vehicle);
		return (float) jojo_ripples$maxMountHealthWithoutBleeding;
	}

	@Inject(method = "renderVehicleHealth", at = @At(
			value = "INVOKE",
			target = "blitSprite",
			ordinal = 0), // when the "container heart" sprite is rendered
			locals = LocalCapture.CAPTURE_FAILSOFT)
	public void jojo_ripples$renderSpriteOnMountBleeding(GuiGraphics guiGraphics, CallbackInfo ci, 
			LivingEntity mount, int maxHeartsWithoutBleeding, int health, 
			int yBottom, int xRight, int y, int healthAlreadyRendered, int heartsInRow, int heart, int x) {
		if (VanillaGuiHelper.shouldAddBleedingMountOnHeart(jojo_ripples$maxMountHealthWithBleeding, jojo_ripples$maxMountHealthWithoutBleeding, 
				heart, health, healthAlreadyRendered)) {
			PoseStack pose = guiGraphics.pose();
			pose.pushPose();
			pose.translate(0, 0, 1); // to render it on top of other sprites that will be rendered afterwards
			guiGraphics.blitSprite(VanillaGuiHelper.HEART_BLEEDING_MOUNT, x, y, 9, 9);
			pose.popPose();
		}
	}

}
