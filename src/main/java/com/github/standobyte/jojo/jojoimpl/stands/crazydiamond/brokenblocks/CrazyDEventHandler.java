package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.brokenblocks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class CrazyDEventHandler {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
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
	public static void recordBlockDrops(BlockDropsEvent event) {
		ServerLevel level = event.getLevel();
		if (!level.isClientSide()) {
			BlockPos blockPos = event.getPos();

			List<ItemStack> generatedLoot = event.getDrops().stream()
					.map(ItemEntity::getItem).map(ItemStack::copy)
					.toList();
			BlockState blockState = event.getState();
			Optional<BlockEntity> tileEntity = Optional.ofNullable(level.getBlockEntity(blockPos));
			boolean blockLootGamerule = level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
			PrevBlockInfo blockInfo = CrazyDRestoreTerrainAbility.rememberBrokenBlock(
					level, blockPos, blockState, tileEntity, 
					blockLootGamerule ? generatedLoot : Collections.emptyList());
			
			int xp = event.getDroppedExperience();
			if (xp > 0) {
				blockInfo.setDroppedXp(xp);
			}
		}
	}
}
