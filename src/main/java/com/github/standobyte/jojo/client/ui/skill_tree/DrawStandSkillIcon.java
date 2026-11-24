package com.github.standobyte.jojo.client.ui.skill_tree;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.LivingEntity;

public class DrawStandSkillIcon {
	
	public static boolean drawnAsSilhouette(UnlockableSkill skill, PowerData levelingData, ConditionCheck canUnlock) {
		return drawnAsSilhouette(levelingData.isSkillUnlocked(skill.skillName), canUnlock);
	}
	
	public static boolean drawnAsSilhouette(boolean isUnlocked, ConditionCheck canUnlock) {
		return !isUnlocked && !canUnlock.positive() && canUnlock != StandUnlockableSkill.NOT_ENOUGH_EXP;
	}
	
	public static void drawSkillIcon(UnlockableSkill skill, 
			AbilityIconSprites abilityIconSprites, StandSkin standSkin, 
			GuiGraphics guiGraphics, int x, int y, 
			boolean drawAsSilhouette) {
		int skillSpriteColor = drawAsSilhouette ? 0x40000000 : BlitFloat.NO_TINT;
		
		switch (skill.skillName) {
			// oh well
			case "manual_control" -> {
				renderManualControlSkillIcon(guiGraphics, 
						x, y, skillSpriteColor, false,
						standSkin, Minecraft.getInstance().player);
			}
			default -> {
				TextureAtlasSprite icon = abilityIconSprites.getAbilityIcon(skill.skillName, standSkin);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), icon, 
						x, y, 16, 16, 0, skillSpriteColor);
			}
		}
	}
	
	public static void renderManualControlSkillIcon(GuiGraphics guiGraphics, 
			float x, float y, int color, boolean playerOnForeground,
			StandSkin standSkin, LivingEntity standUser) {
		PoseStack poseStack = guiGraphics.pose();
		guiGraphics.enableScissor((int) x, (int) y, (int) x + 16, (int) y + 16);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		if (playerOnForeground) {
			PowerHud.renderStandIcon(standSkin, poseStack, (int) x + 4, y - 4, ARGB32.multiply(color, 0x60FFFFFF));
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			poseStack.pushPose();
			poseStack.scale(0.5f, 0.5f, 1);
			poseStack.translate(x, y, 0);
			ClientUtil.renderEntityFace(poseStack, x + 4, y + 10, standUser, color, false);
			poseStack.popPose();
		}
		else {
			//guiGraphics.enableScissor((int) x + 11, (int) y, (int) x + 16, (int) y + 16);
			poseStack.pushPose();
			poseStack.scale(0.5f, 0.5f, 1);
			poseStack.translate(x, y, 0);
			ClientUtil.renderEntityFace(poseStack, x + 16, y, standUser, ARGB32.multiply(color, 0x60FFFFFF), false);
			poseStack.popPose();
			//guiGraphics.disableScissor();
			PowerHud.renderStandIcon(standSkin, poseStack, x - 2, y, color);
		}
		guiGraphics.disableScissor();
	}
	
}
