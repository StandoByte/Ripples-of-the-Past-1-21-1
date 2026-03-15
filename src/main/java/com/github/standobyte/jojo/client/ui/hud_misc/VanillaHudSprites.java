package com.github.standobyte.jojo.client.ui.hud_misc;

import com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.client.GuiAccessor;

import net.minecraft.resources.ResourceLocation;

public class VanillaHudSprites {
	public static ResourceLocation ARMOR_FULL_SPRITE;
	public static ResourceLocation ARMOR_HALF_SPRITE;
	public static ResourceLocation ARMOR_EMPTY_SPRITE;
	public static ResourceLocation HOTBAR_SPRITE;
	public static ResourceLocation HOTBAR_SELECTION_SPRITE;
	public static ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE;
	public static ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE;
	public static ResourceLocation EFFECT_BACKGROUND_AMBIENT_SPRITE;
	public static ResourceLocation EFFECT_BACKGROUND_SPRITE;
	public static ResourceLocation AIR_SPRITE;
	public static ResourceLocation AIR_BURSTING_SPRITE;
	public static ResourceLocation HEART_VEHICLE_CONTAINER_SPRITE;
	public static ResourceLocation HEART_VEHICLE_FULL_SPRITE;
	public static ResourceLocation HEART_VEHICLE_HALF_SPRITE;

	public static void cacheSpritePaths(GuiAccessor gui) {
		if (ARMOR_FULL_SPRITE == null) {
			ARMOR_FULL_SPRITE = GuiAccessor.getARMOR_FULL_SPRITE();
			ARMOR_HALF_SPRITE = GuiAccessor.getARMOR_HALF_SPRITE();
			ARMOR_EMPTY_SPRITE = GuiAccessor.getARMOR_EMPTY_SPRITE();
			HOTBAR_SPRITE = GuiAccessor.getHOTBAR_SPRITE();
			HOTBAR_SELECTION_SPRITE = GuiAccessor.getHOTBAR_SELECTION_SPRITE();
			HOTBAR_OFFHAND_LEFT_SPRITE = GuiAccessor.getHOTBAR_OFFHAND_LEFT_SPRITE();
			HOTBAR_OFFHAND_RIGHT_SPRITE = GuiAccessor.getHOTBAR_OFFHAND_RIGHT_SPRITE();
			EFFECT_BACKGROUND_AMBIENT_SPRITE = GuiAccessor.getEFFECT_BACKGROUND_AMBIENT_SPRITE();
			EFFECT_BACKGROUND_SPRITE = GuiAccessor.getEFFECT_BACKGROUND_SPRITE();
			AIR_SPRITE = GuiAccessor.getAIR_SPRITE();
			AIR_BURSTING_SPRITE = GuiAccessor.getAIR_BURSTING_SPRITE();
			HEART_VEHICLE_CONTAINER_SPRITE = GuiAccessor.getHEART_VEHICLE_CONTAINER_SPRITE();
			HEART_VEHICLE_FULL_SPRITE = GuiAccessor.getHEART_VEHICLE_FULL_SPRITE();
			HEART_VEHICLE_HALF_SPRITE = GuiAccessor.getHEART_VEHICLE_HALF_SPRITE();
		}
	}

}
