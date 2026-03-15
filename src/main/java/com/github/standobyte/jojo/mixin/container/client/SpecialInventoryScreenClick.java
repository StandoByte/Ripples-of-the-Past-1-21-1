package com.github.standobyte.jojo.mixin.container.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.entity_externalcontainer.ModdedContainerClickType;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.client.ClientExtendedInventoryClick;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.client.ClientExternalContainerUI.ExternalContainerScreenCrutches;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public abstract class SpecialInventoryScreenClick extends Screen implements ExternalContainerScreenCrutches {
	@Shadow @Final protected AbstractContainerMenu menu;

	protected SpecialInventoryScreenClick(Component title) {
		super(title);
	}

	@Inject(method = "slotClicked", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick("
					+ "IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"), 
			cancellable = true)
	private void onInventoryClick(Slot slot, int slotId, int mouseButton, ClickType clickType, CallbackInfo ci) {
		ModdedContainerClickType moddedClickType = ModdedContainerClickType.getClientModdedClick(this, menu, slot, slotId, mouseButton, clickType);
		if (moddedClickType != null) {
			ClientExtendedInventoryClick.slotClicked(slot, slotId, menu, false, mouseButton, moddedClickType);
			ci.cancel();
		}
	}

}
