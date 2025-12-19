package com.github.standobyte.jojo.util;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.packet.fromserver.TrResetDeathTimePacket;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;

public class JojoModUtil {

	public static boolean canEntityDestroy(ServerLevel level, BlockPos blockPos, BlockState blockState, LivingEntity entity) {
		if (breakingBlocksEnabled(level)
				&& blockState.canEntityDestroy(level, blockPos, entity)
				&& EventHooks.onEntityDestroyBlock(entity, blockPos, blockState)) {
			Player player = null;
			if (entity instanceof Player) {
				player = (Player) entity;
			}
			else if (entity instanceof StandEntity) {
				LivingEntity standUser = ((StandEntity) entity).getUser();
				if (standUser instanceof Player) {
					player = (Player) standUser;
				}
			}
			return player == null || level.mayInteract(player, blockPos);
		}
		return false;
	}

	public static boolean breakingBlocksEnabled(Level level) {
		return true;
		// FIXME jojoAbilitiesBreakBlocks gamerule
//		return level.getGameRules().getBoolean(ModGamerules.BREAK_BLOCKS);
	}

	public static void blockCatchFire(Level level, BlockPos blockPos, BlockState blockState, @Nullable Direction face, @Nullable LivingEntity igniter) {
		blockState.onCaughtFire(level, blockPos, face, igniter);
		boolean blockGotRemoved = blockState.getBlock() instanceof TntBlock;
		if (blockGotRemoved) {
			CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, blockPos, blockState, 
					Optional.ofNullable(level.getBlockEntity(blockPos)), Collections.emptyList());
			level.removeBlock(blockPos, false);
		}
	}

	@Deprecated
	public static boolean destroyBlock(Level level, BlockPos blockPos, boolean dropBlock, @Nullable Entity entity) {
//		BlockState oldState = dropBlock ? null /*no need to call it in this case*/ : level.getBlockState(blockPos);
//		boolean res = level.destroyBlock(blockPos, dropBlock, entity);
////		if (res && !dropBlock) {
////			CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, blockPos, oldState, 
////					Optional.ofNullable(level.getBlockEntity(blockPos)), 
////					Collections.emptyList());
////		}
//		return res;
		return level.destroyBlock(blockPos, dropBlock, entity);
	}

	public static boolean dropBrokenBlock(LivingEntity entity) {
		return !(entity instanceof Player player && player.getAbilities().instabuild);
	}


	public static void onLivingResurrect(LivingEntity entity) {
		entity.deathTime = 0;
		Level level = entity.level();
		if (!entity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new TrResetDeathTimePacket(entity.getId()));
			if (entity instanceof ServerPlayer player) {
				if (!level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator()) {
					player.setExperienceLevels(0);
					player.setExperiencePoints(0);
				}
			}
		}
	}

}
