package com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.ui.hud_misc.OverlayMessage;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHudControlsElement;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.ModdedContainerClickType;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.input.ClientStandItemInputs;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.input.StandItemInput;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.client.ClientExternalContainerUI;
import com.github.standobyte.jojo.subsystems.entity_opencontainer.OpenContainerAsNonPlayer.ContainerOpenedAsNonPlayer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientStandHeldItemsUI extends ClientExternalContainerUI {
	public static final ResourceLocation TEXTURE = JojoMod.resLoc("textures/gui/container/stand_arm_slots.png");
	protected ConditionCheck clickableCheck = ConditionCheck.POSITIVE;
	protected OverlayMessage overlayMessage = new OverlayMessage();

	public ClientStandHeldItemsUI(AbstractContainerScreen<?> mainScreen, AbstractContainerMenu sideContainer) {
		super(mainScreen, sideContainer);
	}
	
	
	@SubscribeEvent
	public static void onScreenOpened(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		// FIXME stand slots UI with the creative inventory screen
		if (screen instanceof AbstractContainerScreen inventoryScreen && !(screen instanceof CreativeModeInventoryScreen)) {
			StandHandsContainerMenu standHandsContainer = PlayerExternalContainers.get(Minecraft.getInstance().player)
					.getContainerOfType(StandHandsContainerMenu.class);
			if (standHandsContainer != null) {
				ClientStandHeldItemsUI containerUI = new ClientStandHeldItemsUI(inventoryScreen, standHandsContainer);
				addToScreen(inventoryScreen, containerUI);
			}
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
			int standIconY = creativeScreen ? y - 16 : slotsY + 2;
			
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
			// FIXME tooltip renders below item durability bars
			renderTooltip(guiGraphics, mouseX, mouseY);
			
			int messageY = mainScreen.getGuiTop() + mainScreen.getYSize() + height + 2;
			overlayMessage.renderOverlayMessage(guiGraphics, guiGraphics.guiHeight() - messageY);
		}
	}

	@Override
	protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
		if (getCarriedItem().isEmpty() && this.hoveredSlot != null) {
			if (this.hoveredSlot.hasItem()) {
				super.renderTooltip(guiGraphics, x, y);
			}
			else {
				List<Component> slotTooltip = new ArrayList<>();
				Entity standEntity = ((StandHandsContainerMenu) sideContainer).standEntity;
				slotTooltip.add(Component.translatable("stand_hand_slot." + (hoveredSlot.index == 0 ? "main" : "off"), standEntity.getDisplayName()));
				if (Screen.hasShiftDown()) {
					slotTooltip.add(Component.translatable("stand_hand_slot.hint1").withStyle(ChatFormatting.GRAY));
					slotTooltip.add(Component.translatable("stand_hand_slot.hint2").withStyle(ChatFormatting.GRAY));
					slotTooltip.add(Component.translatable("stand_hand_slot.hint3").withStyle(ChatFormatting.GRAY));
					slotTooltip.add(Component.translatable("stand_hand_slot.hint4", 
							PowerHudControlsElement.getKeybindNoSpaceAtModifierPlus(ClientStandItemInputs.keyDrop))
							.withStyle(ChatFormatting.GRAY));
				}
				else {
					slotTooltip.add(Component.translatable("stand_hand_slot.shift").withStyle(ChatFormatting.DARK_GRAY));
				}
				guiGraphics.renderTooltip(mainScreen.getMinecraft().font, slotTooltip, Optional.empty(), ItemStack.EMPTY, x, y);
			}
		}
	}
	
	@Override
	protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot) {
		if (clickableCheck.isPositive()) {
			super.renderSlotHighlight(guiGraphics, slot);
		}
	}
	
	
	@Override
	public void tick() {
		super.tick();
		LivingEntity user = ClientProxy.getClientPlayer();
		StandEntity stand = ClientGlobals.playerStandEntity;
		if (user != null && stand != null) {
			AbstractContainerMenu mainContainer = mainScreen.getMenu();
			Entity actualEntity = ((ContainerOpenedAsNonPlayer) mainContainer).jojo_ripples$getActualEntity();
			clickableCheck = actualEntity != stand ? StandItemInput.distanceCondition(stand, user) : ConditionCheck.POSITIVE;
		}
		
		overlayMessage.tick();
	}

	@Override
	protected void slotClicked(Slot slot, AbstractContainerMenu container, int mouseButton, ClickType clickType) {
		if (clickableCheck.isPositive() || (
				clickType == ClickType.SWAP && mouseButton == Inventory.SLOT_OFFHAND ||
				clickType == ClickType.CLONE || 
				clickType == ClickType.THROW)
				) {
			super.slotClicked(slot, container, mouseButton, clickType);
		}
		else {
			setErrorMessage();
			return;
		}
	}
	
	protected void setErrorMessage() {
		Component message = clickableCheck.getWarning();
		if (message != null) {
			overlayMessage.setOverlayMessage(message.copy().withStyle(ChatFormatting.RED), false);
		}
	}
	
	
	@Nullable
	public static ModdedContainerClickType getStandQolClickType(ClickType interceptedClickType, 
			Screen containerScreen, AbstractContainerMenu mainContainer, 
			Slot slot, int slotId, int mouseButton) {
		if (slot != null
				&& interceptedClickType == ClickType.PICKUP 
				&& Screen.hasControlDown()) {
			ClientStandHeldItemsUI standHandsContainerUI = 
					((ExternalContainerScreenCrutches) containerScreen).jojo_ripples$getStandArmsExtContainer();
			if (standHandsContainerUI != null) {
				if (standHandsContainerUI.clickableCheck.isPositive()) {
					return ModdedContainerClickType.STAND_QUICK_MOVE;
				}
				else {
					standHandsContainerUI.setErrorMessage();
				}
			}
		}
		
		return null;
	}
	
	@Override
	protected ClickType mouseClickType(int mouseButton) {
		return Screen.hasControlDown() ? ClickType.QUICK_MOVE : super.mouseClickType(mouseButton);
	}

}
