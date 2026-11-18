package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.init.ModPotterySherds;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;

@Mixin(DecoratedPotPatterns.class)
public class DecoratedPotPatternsMixin {

	// how come they keep making random shit full on data-driven, while other random shit is stored in immutable maps
	@WrapMethod(method = "Lnet/minecraft/world/level/block/entity/DecoratedPotPatterns;"
			+ "getPatternFromItem("
			+ "Lnet/minecraft/world/item/Item;"
			+ ")Lnet/minecraft/resources/ResourceKey;")
	private static ResourceKey<DecoratedPotPattern> getPatternFromMod(Item item, Operation<ResourceKey<DecoratedPotPattern>> original) {
		ResourceKey<DecoratedPotPattern> sherd = ModPotterySherds.MOD_ITEM_TO_POT_TEXTURE.get(item);
		return sherd != null ? sherd : original.call(item);
	}
}
