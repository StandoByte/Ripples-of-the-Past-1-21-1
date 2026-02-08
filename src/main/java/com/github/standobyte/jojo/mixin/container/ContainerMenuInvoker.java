package com.github.standobyte.jojo.mixin.container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public interface ContainerMenuInvoker {
	@Invoker("moveItemStackTo") boolean invokeMoveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);
}
