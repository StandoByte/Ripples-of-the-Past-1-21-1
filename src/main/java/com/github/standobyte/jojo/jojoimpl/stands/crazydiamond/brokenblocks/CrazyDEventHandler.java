package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.brokenblocks;

import java.util.Collections;
import java.util.Optional;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class CrazyDEventHandler {

	// the case of creative players breaking blocks isn't covered by LevelDestroyBlockMixin
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onCreativePlayerBlockDestroy(BlockEvent.BreakEvent event) {
		LevelAccessor _level = event.getLevel();
		if (!_level.isClientSide()) {
			Level world = (Level) _level;
			BlockPos pos = event.getPos();

			if (event.getPlayer().getAbilities().instabuild) {
				CrazyDRestoreTerrainAbility.rememberBrokenBlock(world, 
						pos, event.getState(), Optional.ofNullable(world.getBlockEntity(pos)), 
						Collections.emptyList());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void recordDroppedXp(BlockDropsEvent event) {
		ServerLevel level = event.getLevel();
		if (!level.isClientSide()) {
			int xp = event.getDroppedExperience();
			if (xp > 0) {
				BlockPos blockPos = event.getPos();
				BrokenBlocksChunkData chunkData = BrokenBlocksChunkData.getExistingData(level, blockPos);
				if (chunkData != null) {
					PrevBlockInfo blockInfo = chunkData.getBrokenBlockAt(blockPos);
					if (blockInfo != null) {
						blockInfo.setDroppedXp(xp);
					}
				}
			}
		}
	}
}
