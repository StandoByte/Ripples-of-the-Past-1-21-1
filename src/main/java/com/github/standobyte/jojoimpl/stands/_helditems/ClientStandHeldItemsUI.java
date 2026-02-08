package com.github.standobyte.jojoimpl.stands._helditems;

import java.util.Objects;

import com.github.standobyte.jojo.client.ui.powerhud.PowerHud;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.externalcontainer.PlayerExternalContainers;
import com.github.standobyte.jojo.mechanics.externalcontainer.client.ClientExternalContainerUI;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientStandHeldItemsUI extends ClientExternalContainerUI {
	public static final ResourceLocation TEXTURE = JojoMod.resLoc("textures/gui/container/stand_arm_slots.png");

	public ClientStandHeldItemsUI(AbstractContainerScreen<?> mainScreen, AbstractContainerMenu sideContainer) {
		super(mainScreen, sideContainer);
	}
	
	
	@SubscribeEvent
	public static void onScreenOpened(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screen instanceof AbstractContainerScreen inventoryScreen) {
			PlayerExternalContainers extraContainers = PlayerExternalContainers.get(Minecraft.getInstance().player);
			extraContainers.getAllContainers().stream()
			.map(container -> container instanceof StandHandsContainerMenu ? (StandHandsContainerMenu) container : null)
			.filter(Objects::nonNull)
			.findFirst().ifPresent(standHandsContainer -> {
				ClientStandHeldItemsUI containerUI = new ClientStandHeldItemsUI(inventoryScreen, standHandsContainer);
				addToScreen(inventoryScreen, containerUI);
			});
		}
	}
	

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		StandHandsContainerMenu standHandsContainer = ((StandHandsContainerMenu) sideContainer);
		StandEntity stand = standHandsContainer.standEntity;
		if (stand != null) {
			boolean creativeScreen = mainScreen.getClass() == CreativeModeInventoryScreen.class;
			PoseStack pose = guiGraphics.pose();

			int screenHeight = mainScreen.getYSize();
			if (mainScreen.getClass() == ContainerScreen.class /*AKA chest*/) screenHeight -= 1; // why the fuck

			int width = creativeScreen ? 43 : 50;
			int height = creativeScreen ? 32 : 26;
			int x = creativeScreen ? -width + 3 : mainScreen.getXSize() - width;
			int y = creativeScreen ? screenHeight - height : screenHeight - 4;
			int leftSlotX = x + 8;
			int rightSlotX = x + 26;
			int slotsY = y + (creativeScreen ? 8 : 2);
			int texV = creativeScreen ? 96 : 0;
			int standIconX = creativeScreen ? (leftSlotX + rightSlotX) / 2 : x - 16;
			int standIconY = creativeScreen ? y - 16 : slotsY;
			
			Slot leftHand = standHandsContainer.getLeftHandSlot();
			leftHand.x = leftSlotX;
			leftHand.y = slotsY;
			Slot rightHand = standHandsContainer.getRightHandSlot();
			rightHand.x = rightSlotX;
			rightHand.y = slotsY;

			pose.pushPose();
			pose.translate(mainScreen.getGuiLeft(), mainScreen.getGuiTop(), 0);
			BlitFloat.blit(pose, mainScreen.getMinecraft(), TEXTURE, 
					x, y, width, height, 0, 
					0, texV, width, height, 256, 256, 
					BlitFloat.NO_TINT);

			PowerHud.renderClientStandIcon(pose, standIconX, standIconY);

			super.render(guiGraphics, mouseX, mouseY, partialTick);
			pose.popPose();
			renderTooltip(guiGraphics, mouseX, mouseY);
		}
	}

}
