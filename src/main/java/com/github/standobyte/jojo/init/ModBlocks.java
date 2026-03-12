package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mc.block.MeteoriteCoreBlock;
import com.github.standobyte.jojo.mechanics.clothes.sewing.SewingMachineBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JojoMod.MOD_ID);


	public static final DeferredBlock<Block> SEWING_MACHINE = BLOCKS.register("sewing_machine", 
			() -> new SewingMachineBlock(Block.Properties.ofFullCopy(Blocks.IRON_BLOCK)
					.requiresCorrectToolForDrops()));

	public static final DeferredBlock<Block> METEORIC_IRON = BLOCKS.register("meteoric_iron", 
			() -> new Block(Block.Properties.ofFullCopy(Blocks.IRON_BLOCK)
					.requiresCorrectToolForDrops()));

	public static final DeferredBlock<MeteoriteCoreBlock> METEORITE_CORE = BLOCKS.register("meteorite_core", 
			() -> new MeteoriteCoreBlock(Block.Properties.ofFullCopy(Blocks.IRON_BLOCK)
					.strength(10.0F, 3.0F).requiresCorrectToolForDrops()));
}
