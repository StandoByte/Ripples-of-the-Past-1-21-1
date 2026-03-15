package com.github.standobyte.jojo.client.itemrender.custommodel;

import java.util.function.Supplier;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ItemRendererProvider implements IClientItemExtensions {
	protected Supplier<BlockEntityWithoutLevelRenderer> itemRenderer;
	
	public ItemRendererProvider(Supplier<BlockEntityWithoutLevelRenderer> itemRenderer) {
		this.itemRenderer = itemRenderer;
	}

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return itemRenderer.get();
	}

}
