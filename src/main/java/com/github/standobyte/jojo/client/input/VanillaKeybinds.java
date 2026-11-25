package com.github.standobyte.jojo.client.input;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.JojoMenuTabs;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.Tab;
import com.github.standobyte.jojo.config.stand_toggles.StandTogglesHandler;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class VanillaKeybinds {
	public static final String MAIN_CATEGORY = "key.categories." + JojoMod.MOD_ID;
	public VanillaKeyMappingExtended summonStand;
	public VanillaKeyMappingExtended standArmsOnlyHUD;
	public VanillaKeyMappingExtended playerPowerHUD;
	public VanillaKeyMappingExtended useAbility;
	public VanillaKeyMappingExtended switchSpecial;
	public VanillaKeyMappingExtended disableHUDControls;
	public VanillaKeyMappingExtended jojoStuffMenu;
	public VanillaKeyMappingExtended standManualControl;
	
	public static VanillaKeybinds register(RegisterKeyMappingsEvent event) {
		VanillaKeybinds binds = new VanillaKeybinds();
		
		event.register(binds.summonStand = new VanillaKeyMappingExtended(
				JojoMod.MOD_ID + ".key.toggle_stand", 
				new KeyCtxAndThen(() -> {
					StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
					return standPower != null && standPower.hasPower();
				}), 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, MAIN_CATEGORY)
				.inInitOrder()
				.withDescTooltip());
		
		event.register(binds.standArmsOnlyHUD = new VanillaKeyMappingExtended(
				JojoMod.MOD_ID + ".key.stand_mode", 
				new KeyCtxAndThen(() -> {
					StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
					return standPower != null && standPower.hasPower() && !standPower.isSummoned();
				}), 
				KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, MAIN_CATEGORY)
				.inInitOrder()
				.withDescTooltip());
		
		event.register(binds.playerPowerHUD = new VanillaKeyMappingExtended(
				JojoMod.MOD_ID + ".key.non_stand_mode", 
				new KeyCtxAndThen(() -> {
					PlayerPower playerPower = ClientPowerCache.getPower(PowerClass.PLAYER_POWER);
					return playerPower != null && playerPower.hasPower();
				}), 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, MAIN_CATEGORY)
				.inInitOrder()
				.withDescTooltip());
		
		event.register(binds.useAbility = new VanillaKeyMappingExtended(
				JojoMod.MOD_ID + ".key.use_special_ability", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, MAIN_CATEGORY)
				.inInitOrder()
				.withDescTooltip());
		
		event.register(binds.switchSpecial = new VanillaKeyMappingExtended(
				JojoMod.MOD_ID + ".key.ability_hotbar", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, MAIN_CATEGORY)
				.inInitOrder()
				.withDescTooltip());
		
		event.register(binds.disableHUDControls = new VanillaKeyMappingExtended(
				JojoMod.MOD_ID + ".key.disable_hotbars", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, MAIN_CATEGORY)
				.inInitOrder()
				.withDescTooltip()
				.canBeHoldOrToggle(JojoMod.config.getClient().toggleDisableHotbars));
		
		event.register(binds.jojoStuffMenu = new VanillaKeyMappingExtended(
				JojoMod.MOD_ID + ".key.jojo_menu", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, MAIN_CATEGORY)
				.inInitOrder()
				.withDescTooltip());
		
		event.register(binds.standManualControl = new VanillaKeyMappingExtended(
				JojoMod.MOD_ID + ".key.stand_remote_control", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, MAIN_CATEGORY)
				.inInitOrder());
		
		return binds;
	}
	
	public static boolean shouldConsumeModKeyMappingClicks(Minecraft mc) {
		return mc.level != null && mc.getOverlay() == null && mc.screen == null;
	}
	
	public static boolean consumeAllClicks(KeyMapping keyMapping) {
		if (keyMapping.clickCount == 0) {
			return false;
		} else {
			keyMapping.clickCount = 0;
			return true;
		}
	}


	public void handleTick(InputHandler inputHandler) {
		Minecraft mc = Minecraft.getInstance();
		if (!shouldConsumeModKeyMappingClicks(mc)) return;
		
		inputHandler.postTickHandleHudModeKeybinds(this);
		
		while (disableHUDControls.consumeClick()) {
			if (JojoMod.config.getClient().toggleDisableHotbars.getAsBoolean()) {
				InputHandler.inputsDisabled = !InputHandler.inputsDisabled;
			}
		}
		
		if (consumeAllClicks(jojoStuffMenu)) {
			Tab tab = JojoMenuTabs.getTabToOpenOnMenuKey();
			if (tab != null) {
				tab.onClick(mc, mc.screen);
			}
		}
		
		StandTogglesHandler.onTickPost(mc);
	}
	
}
