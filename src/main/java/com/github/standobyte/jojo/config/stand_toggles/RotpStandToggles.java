package com.github.standobyte.jojo.config.stand_toggles;

import com.github.standobyte.jojo.client.input.VanillaKeybinds;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class RotpStandToggles {
	public static StandToggleKeyMapping standToggle_breakBlocks;
	public static StandToggleKeyMapping standToggle_pickUpItems;
	public static StandToggleKeyMapping standToggle_moveBeyondEffRange;
	
	public static ClientStandToggle breakBlocks;
	public static ClientStandToggle pickUpItems;
	public static ClientStandToggle moveBeyondEffectiveRange;

	// Called in JojoMod#registerConfig(RegisterRotpConfigEvent)
	public static void makeToggles() {
		var client = JojoMod.config.getClient();
		var broadcastClient = JojoMod.config.getPlayerBroadcast(Minecraft.getInstance().player);
		var common = JojoMod.config.getCommon();
		
		breakBlocks = new ClientStandToggle(
				broadcastClient.standBreaksBlocks, common.standsBreakBlocks, 
				ModConfigType.CLIENT_BROADCAST, 
				client.toggleVisible_standBreaksBlocks, 
				() -> standToggle_breakBlocks, 
				new GuiIcon(ConfigGuiHelper.toIconPath("stands_break_blocks"), 16, 16), null, 
				Component.translatable("jojo_ripples.stand_toggles.destroy_blocks"));
		StandTogglesHandler.addToggle(breakBlocks);
		
		pickUpItems = new ClientStandToggle(
				broadcastClient.standPicksUpItems, null, 
				ModConfigType.CLIENT_BROADCAST, 
				client.toggleVisible_standPicksUpItems, 
				() -> standToggle_pickUpItems, 
				new GuiIcon(ConfigGuiHelper.toIconPath("stand_pick_up_items"), 16, 16), null, 
				Component.translatable("jojo_ripples.stand_toggles.stand_pick_up_items"));
		StandTogglesHandler.addToggle(pickUpItems);
		
		moveBeyondEffectiveRange = new ClientStandToggle(
				broadcastClient.standMovesBeyondEffRange, null, 
				ModConfigType.CLIENT_BROADCAST, 
				client.toggleVisible_standMovesBeyondEffRange, 
				() -> standToggle_moveBeyondEffRange, 
				new GuiIcon(ConfigGuiHelper.toIconPath("stand_range_max"), 16, 16), 
				new GuiIcon(ConfigGuiHelper.toIconPath("stand_range_effective"), 16, 16), 
				Component.translatable("jojo_ripples.stand_toggles.stand_move_beyond_eff_range"));
		StandTogglesHandler.addToggle(moveBeyondEffectiveRange);
	}
	
	// Called in ClientSetup#registerKeyBindings(RegisterKeyMappingsEvent)
	// We're not registering these keybinds in the event itself, so that they don't show up in the settings menu
	public static void makeKeyBindings() {
		standToggle_breakBlocks = new StandToggleKeyMapping(
				JojoMod.MOD_ID + ".stand_toggles.destroy_blocks", 
				() -> breakBlocks, 
				InputConstants.UNKNOWN, VanillaKeybinds.MAIN_CATEGORY);
		
		standToggle_pickUpItems = new StandToggleKeyMapping(
				JojoMod.MOD_ID + ".stand_toggles.stand_pick_up_items", 
				() -> pickUpItems,
				InputConstants.UNKNOWN, VanillaKeybinds.MAIN_CATEGORY);
		
		standToggle_moveBeyondEffRange = new StandToggleKeyMapping(
				JojoMod.MOD_ID + ".stand_toggles.stand_move_beyond_eff_range", 
				() -> moveBeyondEffectiveRange,
				InputConstants.UNKNOWN, VanillaKeybinds.MAIN_CATEGORY);
	}
	
}
