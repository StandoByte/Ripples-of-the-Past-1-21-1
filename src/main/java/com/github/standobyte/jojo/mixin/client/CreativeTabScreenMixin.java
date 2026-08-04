package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.CustomRenderCreativeTab;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeTabScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
	
	public CreativeTabScreenMixin(ItemPickerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Shadow public static CreativeModeTab selectedTab;
	@Shadow private float scrollOffs;

	@Inject(method = "render", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;"
					+ "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
			shift = At.Shift.AFTER))
	public void renderTabExtra(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (selectedTab instanceof CustomRenderCreativeTab tab) {
			//int rowScrolled = menu.getRowIndexForScroll(scrollOffs);

			int calculateRowCount = Mth.positiveCeilDiv(menu.items.size(), 9) - 5;
			int getRowIndexForScroll = Math.max((int)((double)(scrollOffs * (float)calculateRowCount) + 0.5), 0);

			tab.render(guiGraphics, mouseX, mouseY, partialTick, 
					(CreativeModeInventoryScreen) (Object) this, getRowIndexForScroll);
		}
	}
}
