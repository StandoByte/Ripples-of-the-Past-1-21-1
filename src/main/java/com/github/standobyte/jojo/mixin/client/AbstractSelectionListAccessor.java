package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.components.AbstractSelectionList;

@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccessor<E extends AbstractSelectionList.Entry<E>> {
	@Invoker("addEntry") int invokeAddEntry(E addEntry);
}
