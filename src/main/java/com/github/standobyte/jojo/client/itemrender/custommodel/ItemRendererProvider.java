package com.github.standobyte.jojo.client.itemrender.custommodel;

import java.util.function.Supplier;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.util.Lazy;

public class ItemRendererProvider<T extends BlockEntityWithoutLevelRenderer> implements IClientItemExtensions {
	protected Supplier<T> itemRenderer;

	public ItemRendererProvider(Supplier<T> itemRenderer) {
		this(Lazy.of(itemRenderer));
	}

	public ItemRendererProvider(Lazy<T> itemRenderer) {
		this.itemRenderer = itemRenderer;
	}

	@Override
	public T getCustomRenderer() {
		return itemRenderer.get();
	}

	
	public static class BlockItemRendererProvider<T extends BlockEntityWithoutLevelRenderer, BE extends BlockEntity> extends ItemRendererProvider<T> implements BlockEntityRendererProvider<BE> {
		protected BlockEntityRendererProvider<BE> function;
		protected T blockRendererCast;

		public BlockItemRendererProvider(BlockEntityRendererProvider<BE> function) {
			super(null);
			this.itemRenderer = () -> this.blockRendererCast;
			this.function = function;
		}

		@Override
		public BlockEntityRenderer<BE> create(Context context) {
			BlockEntityRenderer<BE> blockRenderer = function.create(context);
			this.blockRendererCast = (T) blockRenderer;
			return blockRenderer;
		}
		
	}
}
