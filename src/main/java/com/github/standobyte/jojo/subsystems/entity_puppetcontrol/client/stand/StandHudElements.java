package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.hud_misc.VanillaGuiHelper;
import com.github.standobyte.jojo.client.ui.hud_misc.VanillaHudSprites;
import com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.client.GuiAccessor;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

public class StandHudElements {
	public static StandHudElements instance;
	public static void init() {
		if (instance == null) {
			instance = new StandHudElements();
			NeoForge.EVENT_BUS.register(instance);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void renderHudElements(RenderGuiLayerEvent.Pre event) {
		if (Minecraft.getInstance().options.hideGui) return;
		
		StandEntity stand = ClientGlobals.playerStandEntity;
		if (stand != null) {
			if (stand.isManuallyControlled()) {
				renderInManualControl(event, stand);
			}
			else {
				renderOutsideOfManualControl(event, stand);
			}
			renderAlways(event, stand);
		}
	}
	
	protected HealthHudTracker health = new HealthHudTracker();
	
	public static class HealthHudTracker {
		public long healthBlinkTime;
		public int lastHealth;
		public long lastHealthTime;
		public int displayHealth;
	}
	
	public void renderInManualControl(RenderGuiLayerEvent.Pre event, LivingEntity stand) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		DeltaTracker deltaTracker = event.getPartialTick();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		VanillaHudSprites.cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.PLAYER_HEALTH)) {
			if (mc.gameMode.canHurtPlayer()) {
				VanillaGuiHelper.renderHealth(stand, mc.player, guiGraphics, gui, mc, health);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.ARMOR_LEVEL)) {
			if (mc.gameMode.canHurtPlayer()) {
				VanillaGuiHelper.renderArmor(stand, guiGraphics, gui, mc);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.FOOD_LEVEL)) {
			if (mc.gameMode.canHurtPlayer() && mc.player != null) {
				VanillaGuiHelper.renderFood(mc.player, guiGraphics, gui, mc);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.AIR_LEVEL)) {
			if (mc.gameMode.canHurtPlayer() && mc.player != null) {
				VanillaGuiHelper.renderAir(mc.player, guiGraphics, gui, mc);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.HOTBAR)) {
			int center = guiGraphics.guiWidth() / 2;
			int xLeft = center;
			int xRight = center;
			VanillaGuiHelper.renderLivingHeldItems(stand, guiGraphics, gui, deltaTracker, mc, xLeft, xRight, true);
		}
	}
	
	public void renderOutsideOfManualControl(RenderGuiLayerEvent.Pre event, LivingEntity stand) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		DeltaTracker deltaTracker = event.getPartialTick();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		VanillaHudSprites.cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.HOTBAR)) {
			int center = guiGraphics.guiWidth() / 2;
			
            HumanoidArm offHand = mc.player != null ? mc.player.getMainArm().getOpposite() : HumanoidArm.LEFT;
            boolean attackIndicator = mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR;
			int xItemsCenter = switch (offHand) {
				case LEFT -> attackIndicator ? center + 167 : center + 141;
				case RIGHT -> attackIndicator ? center - 167 : center - 141;
			};
			
			VanillaGuiHelper.renderLivingHeldItems(stand, guiGraphics, gui, deltaTracker, mc, xItemsCenter, xItemsCenter, false);
		}
	}
	
	public void renderAlways(RenderGuiLayerEvent.Pre event, LivingEntity stand) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		VanillaHudSprites.cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.EFFECTS)) {
			int color = 0xFFFFFFFF;
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			StandSkin standSkin = StandSkinsLoader.getInstance().getSkin(standPower);
			if (standSkin != null) {
				color = standSkin.getColors().primary();
			}
			
			VanillaGuiHelper.renderStatusEffects(stand, guiGraphics, gui, mc, color);
		}
	}
}
