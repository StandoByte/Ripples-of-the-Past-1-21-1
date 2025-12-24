package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.sewing.SewingMachineBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, JojoMod.MOD_ID);


	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SewingMachineBlockEntity>> SEWING_MACHINE = BLOCK_ENTITY_TYPES.register("sewing_machine", key -> {
		return BlockEntityType.Builder.of(SewingMachineBlockEntity::new, ModBlocks.SEWING_MACHINE.get()).build(null);
	});

	public static final Supplier<BlockEntityType<?>> _PLACEHOLDER_STONE_MASK = () -> null;
}
