package com.github.standobyte.jojo.init;

import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mc.item.component.StandWrittenOnDisc;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.itemtracking.OriginalItemPosComponent;
import com.github.standobyte.v1_21_4_stuff.itemmodel.__ItemModelComponent;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItemDataComponents {
	public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, JojoMod.MOD_ID);
	
	public static final Supplier<DataComponentType<StandWrittenOnDisc>> DISC_STAND = DATA_COMPONENT_TYPES.registerComponentType("disc_stand", 
			builder -> builder
			.persistent(StandWrittenOnDisc.CODEC)
			.networkSynchronized(StandWrittenOnDisc.STREAM_CODEC)
			/*
			 * "cacheEncoding caches the encoding result of the Codec such that any subsequent encodes uses the cached value if the component value hasn't changed. 
			 * This should only be used if the component value is expected to rarely or never change."
			 */
			.cacheEncoding());

	public static final Supplier<DataComponentType<ClothesDataComponent>> CLOTHES_PIECE = DATA_COMPONENT_TYPES.registerComponentType("clothes", 
			builder -> builder
			.persistent(ClothesDataComponent.CODEC)
			.networkSynchronized(ClothesDataComponent.STREAM_CODEC)
			.cacheEncoding());

	public static final Supplier<DataComponentType<UUID>> TRACKER_ID = DATA_COMPONENT_TYPES.registerComponentType("tracker_id", 
			builder -> builder
			.persistent(UUIDUtil.CODEC)
			.networkSynchronized(UUIDUtil.STREAM_CODEC));

	public static final Supplier<DataComponentType<OriginalItemPosComponent>> ORIGINAL_POS = DATA_COMPONENT_TYPES.registerComponentType("original_pos", 
			builder -> builder
			.persistent(OriginalItemPosComponent.CODEC)
			.networkSynchronized(OriginalItemPosComponent.STREAM_CODEC)
			.cacheEncoding());

	public static final Supplier<DataComponentType<ResourceLocation>> ITEM_MODEL = DATA_COMPONENT_TYPES.registerComponentType("item_model", 
			__ItemModelComponent.builder());
}