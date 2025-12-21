package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.sewing.SewingMachineBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JojoMod.MOD_ID);


	public static final DeferredBlock<Block> SEWING_MACHINE = BLOCKS.register("sewing_machine", 
			() -> new SewingMachineBlock(Block.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
}
