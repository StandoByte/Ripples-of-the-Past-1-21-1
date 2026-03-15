package com.github.standobyte.jojo.client.itemrender.custommodel;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ISTERItemCaptureEntity extends ItemOverrides {

	public ISTERItemCaptureEntity() {
		super();
	}

	@Override
	public BakedModel resolve(BakedModel model, ItemStack item, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
		BlockEntityWithoutLevelRenderer ister = IClientItemExtensions.of(item).getCustomRenderer();
		if (ister instanceof ISTERWithEntity) {
			((ISTERWithEntity) ister).setEntity(entity);
		}
		return model;
	}
}
