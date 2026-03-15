package com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.subsystems.ServerBlockDestroyTracker.BlockDestroy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

public abstract class BlockBreakingVanilla implements BlockBreaking {
	public final BlockPos blockPos;
	public float curProgress;
	public final Entity entity;
	
	public BlockBreakingVanilla(BlockPos blockPos, float curProgress, Entity player) {
		this.blockPos = blockPos;
		this.curProgress = curProgress;
		this.entity = player;
	}
	
	@Override
	public float getProgress() {
		return curProgress;
	}
	
	
	@Nullable
	public static PlayerBreakingBlock getBlockBeingBrokenByPlayer(ServerPlayer player) {
		ServerPlayerGameMode handler = player.gameMode;
		if (handler.isDestroyingBlock) {
			return new PlayerBreakingBlock(handler.destroyPos, 
					(float) handler.lastSentState / BlockDestroy.VANILLA_SCALE,
					player, handler);
		}
		return null;
	}
	
	public static class PlayerBreakingBlock extends BlockBreakingVanilla {
		public final ServerPlayerGameMode handler;
		
		public PlayerBreakingBlock(BlockPos blockPos, float curProgress, Entity player, ServerPlayerGameMode handler) {
			super(blockPos, curProgress, player);
			this.handler = handler;
		}
		
		@Override
		public boolean setAndSyncProgress(float progress, BlockPos blockPos, ServerLevel level) {
			handler.lastSentState = (int) (progress * BlockDestroy.VANILLA_SCALE);
			entity.level().destroyBlockProgress(entity.getId(), blockPos, handler.lastSentState);
			this.curProgress = handler.lastSentState;
			return false;
		}
	}
	
	
	@Nullable
	public static ZombieBreakingDoor getDoorBeingBrokenByZombie(Mob zombieMob) {
		for (WrappedGoal goalEntry : zombieMob.goalSelector.availableGoals) {
			Goal goal = goalEntry.getGoal();
			if (goalEntry.isRunning() && goal instanceof BreakDoorGoal breakingDoor) {
				return new ZombieBreakingDoor(breakingDoor.doorPos, 
						(float) breakingDoor.lastBreakProgress / BlockDestroy.VANILLA_SCALE, 
						zombieMob, breakingDoor);
			}
		}
		return null;
	}
	
	public static class ZombieBreakingDoor extends BlockBreakingVanilla {
		public final BreakDoorGoal handler;
		
		public ZombieBreakingDoor(BlockPos blockPos, float curProgress, Entity mob, BreakDoorGoal handler) {
			super(blockPos, curProgress, mob);
			this.handler = handler;
		}

		@Override
		public boolean setAndSyncProgress(float progress, BlockPos blockPos, ServerLevel level) {
			handler.lastBreakProgress = (int) (progress * BlockDestroy.VANILLA_SCALE);
			entity.level().destroyBlockProgress(entity.getId(), blockPos, handler.lastBreakProgress);
			this.curProgress = handler.lastBreakProgress;
			return false;
		}
	}
}
