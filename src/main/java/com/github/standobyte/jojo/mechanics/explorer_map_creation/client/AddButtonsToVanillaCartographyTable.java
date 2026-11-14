package com.github.standobyte.jojo.mechanics.explorer_map_creation.client;

import com.github.standobyte.jojo.UglyCrutchesClient;
import com.github.standobyte.jojo.client.ui.screen_widgets.ButtonWithImage;
import com.github.standobyte.jojo.client.ui.screen_widgets.utils.ButtonDecoration;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.CartographyTableMode;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.packets.ClCartographyContainerModePacket;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.CartographyExplorerMapContainerMenu;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class AddButtonsToVanillaCartographyTable {
	public static final GuiIcon VANILLA_MODE_ICON = new GuiIcon(JojoMod.resLoc("textures/gui/cartography_vanilla.png"), 16, 16);
	public static final GuiIcon EXPLORER_MODE_ICON = new GuiIcon(JojoMod.resLoc("textures/gui/cartography_explorer.png"), 16, 16);

	@SubscribeEvent
	public static void onVanillaCartographyScreenInit(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screen.getClass() == CartographyTableScreen.class) {
			Button[] buttonsToAdd = onCartographyTableInit((AbstractContainerScreen<?>) screen, CartographyTableMode.VANILLA);
			for (Button button : buttonsToAdd) {
				event.addListener(button);
			}
		}
	}
	
	public static Button[] onCartographyTableInit(AbstractContainerScreen<?> screen, CartographyTableMode curScreenMode) {
		if (UglyCrutchesClient.lastBlockInteractedWith == null) return new Button[0];
		
		int x = screen.getGuiLeft();
		int y = screen.getGuiTop();
		
		Button vanillaModeButton = new ButtonWithImage(x, y - 20, 20, 20, 
				ButtonDecoration.icon(VANILLA_MODE_ICON), b -> {
					PacketDistributor.sendToServer(new ClCartographyContainerModePacket(
							/*
							 * We need the block pos to open either CartographyTableMenu or ExplorerMapCreationContainerMenu on server,
							 * but there is no clean way to retrieve the position of the block from the menu.
							 * So instead we store the last right clicked table's position on the client, that's at least *something*.
							 */
							CartographyTableMode.VANILLA, UglyCrutchesClient.lastBlockInteractedWith));
				},
				Tooltip.create(CartographyExplorerMapContainerMenu.VANILLA_CONTAINER_TITLE));
		
		Button explorerModeButton = new ButtonWithImage(x + 20, y - 20, 20, 20, 
				ButtonDecoration.icon(EXPLORER_MODE_ICON), b -> {
					PacketDistributor.sendToServer(new ClCartographyContainerModePacket(
							CartographyTableMode.EXPLORER, UglyCrutchesClient.lastBlockInteractedWith));
				},
				Tooltip.create(CartographyExplorerMapContainerMenu.EXPLORER_CONTAINER_TITLE));
		switch (curScreenMode) {
			case VANILLA -> vanillaModeButton.active = false;
			case EXPLORER -> explorerModeButton.active = false;
		}
		
		return new Button[] { vanillaModeButton, explorerModeButton };
	}
}
