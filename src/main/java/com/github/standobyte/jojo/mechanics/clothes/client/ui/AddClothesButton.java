package com.github.standobyte.jojo.mechanics.clothes.client.ui;

import com.github.standobyte.jojo.client.ui.screen_widgets.ImageButton2;
import com.github.standobyte.jojo.client.ui.utils.DynamicButtonVisibility;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.network.c2s.ClNoParamsPacket;
import com.github.standobyte.jojo.network.c2s.ClNoParamsPacket.PacketType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class AddClothesButton {
	public static final GuiIcon BUTTON = new GuiIcon(JojoMod.resLoc("textures/gui/container/clothes/clothes_button.png"), 16, 16);
	public static final GuiIcon BUTTON_HOVERED = new GuiIcon(JojoMod.resLoc("textures/gui/container/clothes/clothes_button_hovered.png"), 16, 16);
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void addButton(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screen instanceof EffectRenderingInventoryScreen containerScreen) {
			Component buttonName = Component.translatable("jojo_ripples.menu.player.clothes");
			switch (containerScreen) {
				case InventoryScreen survivalScreen -> {
					Button clothesButton = new ImageButton2(
							containerScreen.getGuiLeft() + 153, containerScreen.getGuiTop() + 63, 16, 16, 
							BUTTON, BUTTON, BUTTON_HOVERED, BUTTON_HOVERED, 
							b -> {
								PacketDistributor.sendToServer(ClNoParamsPacket.of(PacketType.OPEN_CLOTHES));
							},
							Tooltip.create(buttonName)) {
						
						@Override
						public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
							setX(containerScreen.getGuiLeft() + 153);
							setY(containerScreen.getGuiTop() + 63);
							super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
						}
					};
					event.addListener(clothesButton);
				}
				case PlayerClothesScreen clothesScreen -> {
					Component goBackName = Component.translatable("container.inventory");
					Button goBackButton = new ImageButton2(
							containerScreen.getGuiLeft() + 153, containerScreen.getGuiTop() + 63, 16, 16, 
							BUTTON, BUTTON, BUTTON_HOVERED, BUTTON_HOVERED, 
							b -> {
								Minecraft mc = Minecraft.getInstance();
								mc.setScreen(new InventoryScreen(mc.player));
							},
							Tooltip.create(goBackName));
					event.addListener(goBackButton);
				}
				case CreativeModeInventoryScreen creativeScreen -> {
					Button clothesButton = new ImageButton2(
							containerScreen.getGuiLeft() + 154, containerScreen.getGuiTop() + 32, 16, 16, 
							BUTTON, BUTTON, BUTTON_HOVERED, BUTTON_HOVERED, 
							b -> {
								PacketDistributor.sendToServer(ClNoParamsPacket.of(PacketType.OPEN_CLOTHES));
							},
							Tooltip.create(buttonName)) {
					};
					DynamicButtonVisibility.add(creativeScreen, clothesButton, creativeScreen::isInventoryOpen);
					event.addListener(clothesButton);
				}
				default -> {}
			}
		}
	}
	
}
