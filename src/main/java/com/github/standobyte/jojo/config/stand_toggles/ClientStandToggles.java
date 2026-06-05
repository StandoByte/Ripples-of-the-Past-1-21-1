package com.github.standobyte.jojo.config.stand_toggles;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHudControlsElement;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientStandToggles {
	public static ClientStandToggle breakBlocks;
	public static ClientStandToggle pickUpItems;
	public static ClientStandToggle moveBeyondEffectiveRange;
	public static ClientStandToggle[] toggles;
	
	public static ClientStandToggle[] lazyInitToggles() {
		if (toggles == null) {
			var client = JojoMod.config.getClient();
			var broadcastClient = JojoMod.config.getPlayerBroadcast(Minecraft.getInstance().player);
			var common = JojoMod.config.getCommon();
			
			breakBlocks = new ClientStandToggle(
					broadcastClient.standBreaksBlocks, common.standsBreakBlocks, 
					ModConfigType.CLIENT_BROADCAST, 
					client.toggleVisible_standBreaksBlocks, 
					() -> {
						InputHandler input = InputHandler.getInstance();
						return input != null && input.getActiveControlScheme() != null;
					}, 
					keybinds -> keybinds.standToggle_breakBlocks, 
					new GuiIcon(ConfigGuiHelper.toIconPath("stands_break_blocks"), 16, 16), null, 
					Component.translatable("jojo_ripples.stand_toggles.destroy_blocks"));
			
			pickUpItems = new ClientStandToggle(
					broadcastClient.standPicksUpItems, null, 
					ModConfigType.CLIENT_BROADCAST, 
					client.toggleVisible_standPicksUpItems, 
					() -> {
						StandEntity stand = ClientGlobals.playerStandEntity;
						return stand != null && stand.isManuallyControlled();
					}, 
					keybinds -> keybinds.standToggle_pickUpItems, 
					new GuiIcon(ConfigGuiHelper.toIconPath("stand_pick_up_items"), 16, 16), null, 
					Component.translatable("jojo_ripples.stand_toggles.stand_pick_up_items"));
			
			moveBeyondEffectiveRange = new ClientStandToggle(
					broadcastClient.standMovesBeyondEffRange, null, 
					ModConfigType.CLIENT_BROADCAST, 
					client.toggleVisible_standMovesBeyondEffRange, 
					() -> {
						return PowerHudControlsElement.controlsHaveTypeAndAbility(PowerClass.STAND, null);
					}, 
					keybinds -> keybinds.standToggle_moveBeyondEffRange, 
					new GuiIcon(ConfigGuiHelper.toIconPath("stand_range_max"), 16, 16), 
					new GuiIcon(ConfigGuiHelper.toIconPath("stand_range_effective"), 16, 16), 
					Component.translatable("jojo_ripples.stand_toggles.stand_move_beyond_eff_range"));

			toggles = new ClientStandToggle[] {
					breakBlocks,
					pickUpItems,
					moveBeyondEffectiveRange
			};
		}
		return toggles;
	}
}
