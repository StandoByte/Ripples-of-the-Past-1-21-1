package com.github.standobyte.jojo.mixin.client.keybindui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.github.standobyte.jojo.client.ui.VanillaKeybindsEntryModification;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;

@Mixin(KeyBindsList.class)
public abstract class KeyBindsListMixin extends ContainerObjectSelectionList<KeyBindsList.Entry> {

	public KeyBindsListMixin(Minecraft minecraft, int width, int height, int y, int itemHeight) {
		super(minecraft, width, height, y, itemHeight);
	}

	@WrapOperation(
			method = "Lnet/minecraft/client/gui/screens/options/controls/KeyBindsList;<init>("
			+ "Lnet/minecraft/client/gui/screens/options/controls/KeyBindsScreen;"
			+ "Lnet/minecraft/client/Minecraft;)V", 
			at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/gui/screens/options/controls/KeyBindsList;addEntry("
					+ "Lnet/minecraft/client/gui/components/AbstractSelectionList$Entry;)I"))
	public int onEntryAdd(KeyBindsList list, AbstractSelectionList.Entry<?> entry, Operation<Integer> original) {
		if (entry instanceof KeyBindsList.KeyEntry keyEntry) {
			if (VanillaKeybindsEntryModification.replaceEntry(list, keyEntry)) {
				return children().size() - 1;
			}
		}
		
		return original.call(list, entry);
	}
}
