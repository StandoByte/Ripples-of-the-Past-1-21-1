package com.github.standobyte.jojo.mixin.container.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenInvoker {
	@Invoker("getTooltipFromContainerItem") public List<Component> invokeGetTooltipFromContainerItem(ItemStack stack); 
}
