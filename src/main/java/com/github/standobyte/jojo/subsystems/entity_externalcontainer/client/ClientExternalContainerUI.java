package com.github.standobyte.jojo.subsystems.entity_externalcontainer.client;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.ScreenCrutches;
import com.github.standobyte.jojo.mixin.container.client.ContainerScreenInvoker;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.ModdedContainerClickType;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.ClientStandHeldItemsUI;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ClientExternalContainerUI implements GuiEventListener, Renderable, Tickable {
	protected AbstractContainerScreen<?> mainScreen;
	protected AbstractContainerMenu sideContainer;

	@Nullable protected Slot hoveredSlot;

	public ClientExternalContainerUI(AbstractContainerScreen<?> mainScreen, AbstractContainerMenu sideContainer) {
		this.mainScreen = mainScreen;
		this.sideContainer = sideContainer;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.hoveredSlot = findSlot(mouseX, mouseY);

		for (int i = 0; i < this.sideContainer.slots.size(); i++) {
			Slot slot = this.sideContainer.slots.get(i);
			if (slot.isActive()) {
				this.renderSlot(guiGraphics, slot);
			}

			if (this.isHovering(slot, (double)mouseX, (double)mouseY) && slot.isActive()) {
				this.hoveredSlot = slot;
				if (slot.isHighlightable()) {
					renderSlotHighlight(guiGraphics, slot);
				}
			}
		}
	}
	
	protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot) {
		int slotColor = -2130706433;
		AbstractContainerScreen.renderSlotHighlight(guiGraphics, slot.x, slot.y, 0, slotColor);
	}

	protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
		if (getCarriedItem().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			ItemStack itemstack = this.hoveredSlot.getItem();
			List<Component> itemTooltip = ((ContainerScreenInvoker) mainScreen).invokeGetTooltipFromContainerItem(itemstack);
			guiGraphics.renderTooltip(mainScreen.getMinecraft().font, itemTooltip, itemstack.getTooltipImage(), itemstack, x, y);
		}
	}

	protected boolean handleInput(InputConstants.Key key, Slot slot, AbstractContainerMenu menu) {
		if (slot == null) return false;

		Minecraft mc = mainScreen.getMinecraft();
		ItemStack carriedItem = getCarriedItem();

		if (carriedItem.isEmpty()) {
			if (mc.options.keySwapOffhand.isActiveAndMatches(key)) {
				this.slotClicked(slot, menu, Inventory.SLOT_OFFHAND, ClickType.SWAP);
				return true;
			}

			for (int i = 0; i < 9; i++) {
				if (mc.options.keyHotbarSlots[i].isActiveAndMatches(key)) {
					this.slotClicked(slot, menu, i, ClickType.SWAP);
					return true;
				}
			}
		}

		if (slot.hasItem()) {
			if (mc.options.keyPickItem.isActiveAndMatches(key)) {
				this.slotClicked(slot, menu, 0, ClickType.CLONE);
				return true;
			}
			else if (mc.options.keyDrop.isActiveAndMatches(key)) {
				boolean dropEntireStack = Screen.hasControlDown();
				this.slotClicked(slot, menu, dropEntireStack ? 1 : 0, ClickType.THROW);
				return true;
			}
		}
		else if (mc.options.keyDrop.isActiveAndMatches(key)) {
			return true;
		}

		if (key.getType() == InputConstants.Type.MOUSE) {
			return handleMouseClick(key, slot, menu);
		}

		return false;
	}
	
	protected boolean handleMouseClick(InputConstants.Key key, Slot slot, AbstractContainerMenu menu) {
		int mouseButton = key.getValue();
		boolean LMB = mouseButton == 0;
		boolean RMB = mouseButton == 1;
		if (LMB || RMB) {
			ClickType clickType = mouseClickType(mouseButton);
			this.slotClicked(slot, menu, key.getValue(), clickType);
			return true;
		}
		return false;
	}
	
	protected ClickType mouseClickType(int mouseButton) {
		return Screen.hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP;
	}


	protected void slotClicked(Slot slot, AbstractContainerMenu container, int mouseButton, ClickType clickType) {
		ClientExtendedInventoryClick.slotClicked(slot, slot.index, container, true, mouseButton, ModdedContainerClickType.fromVanilla(clickType));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		Slot slot = hoveredSlot;
		AbstractContainerMenu menu = this.sideContainer;
		if (slot != null) {
			InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
			return handleInput(key, slot, menu);
		}

		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Slot slot = this.findSlot(mouseX, mouseY);
		AbstractContainerMenu menu = this.sideContainer;
		if (slot != null) {
			InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(button);
			boolean clickedSlot = handleInput(mouseKey, slot, menu);
			if (clickedSlot) {
				((ExternalContainerScreenCrutches) mainScreen).jojo_ripples$preventMouseRelease();
			}
			return clickedSlot;
		}

		return false;
	}

	protected ItemStack getCarriedItem() {
		return mainScreen.getMenu().getCarried();
	}
	
	
	@Override
	public void tick() {}


	@Nullable
	protected Slot findSlot(double mouseX, double mouseY) {
		for (int i = 0; i < this.sideContainer.slots.size(); i++) {
			Slot slot = this.sideContainer.slots.get(i);

			if (this.isHovering(slot, mouseX, mouseY) && slot.isActive()) {
				return slot;
			}
		}
		return null;
	}

	protected boolean isHovering(Slot slot, double mouseX, double mouseY) {
		return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
	}

	protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
		mouseX -= mainScreen.getGuiLeft();
		mouseY -= mainScreen.getGuiTop();
		return mouseX >= x - 1
				&& mouseX < x + width + 1
				&& mouseY >= y - 1
				&& mouseY < y + height + 1;
	}


	@Override public void setFocused(boolean focused) {}
	@Override public boolean isFocused() { return mainScreen.isFocused(); }


	public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
		int x = slot.x;
		int y = slot.y;
		Minecraft mc = mainScreen.getMinecraft();
		ItemStack item = slot.getItem();
		String s = null;

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
		if (item.isEmpty() && slot.isActive()) {
			Pair<ResourceLocation, ResourceLocation> noItem = slot.getNoItemIcon();
			if (noItem != null) {
				TextureAtlasSprite noItemSprite = mc.getTextureAtlas(noItem.getFirst()).apply(noItem.getSecond());
				guiGraphics.blit(x, y, 0, 16, 16, noItemSprite);
			}
		}

		if (!item.isEmpty()) {
			int seed = slot.x + slot.y * mainScreen.getXSize();
			if (slot.isFake()) {
				guiGraphics.renderFakeItem(item, x, y, seed);
			}
			else {
				guiGraphics.renderItem(item, x, y, seed);
			}

			guiGraphics.renderItemDecorations(mc.font, item, x, y, s);
		}

		guiGraphics.pose().popPose();
	}
	
	
	public static void addToScreen(AbstractContainerScreen<?> mainScreen, ClientExternalContainerUI externalUI) {
		mainScreen.renderables.add(externalUI);

		@SuppressWarnings("unchecked")
		List<GuiEventListener> children = (List<GuiEventListener>) mainScreen.children();
		children.add(externalUI);
		
		ExternalContainerScreenCrutches mainScreen_ = (ExternalContainerScreenCrutches) mainScreen;
		mainScreen_.jojo_ripples$onAddedExternalContainerUI(externalUI);
		((ScreenCrutches) mainScreen).jojo_ripples$addTickable(externalUI);
	}

	
	public static interface ExternalContainerScreenCrutches {
		void jojo_ripples$onAddedExternalContainerUI(GuiEventListener child);
		
		void jojo_ripples$preventMouseRelease();
		@Nullable ClientStandHeldItemsUI jojo_ripples$getStandArmsExtContainer();
	}
	
}
