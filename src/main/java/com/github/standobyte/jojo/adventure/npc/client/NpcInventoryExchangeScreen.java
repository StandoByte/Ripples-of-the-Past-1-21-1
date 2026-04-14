package com.github.standobyte.jojo.adventure.npc.client;

import com.github.standobyte.jojo.adventure.npc.NpcInventoryExchangeContainer;
import com.github.standobyte.jojo.adventure.npc.debug.NpcDebugSettingsScreen;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class NpcInventoryExchangeScreen {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static MenuScreens.ScreenConstructor<NpcInventoryExchangeContainer, ?> SCREEN_FACTORY = new MenuScreens.ScreenConstructor() {

		@Override
		public Screen create(AbstractContainerMenu _menu, Inventory inventory, Component title) {
			NpcInventoryExchangeContainer menu = (NpcInventoryExchangeContainer) _menu;
			if (menu.isDebug) {
				return new NpcDebugSettingsScreen(menu, inventory, title);
			}
			return null;
		}
		
	};
}
