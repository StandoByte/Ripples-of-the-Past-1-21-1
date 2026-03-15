package com.github.standobyte.jojo.subsystems;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.BlockBreaking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ServerBlockDestroyTracker {
	public final ServerLevel level;
	protected static AtomicInteger counter = new AtomicInteger();
	public Map<BlockPos, BlockDestroy> blockDestroy = new HashMap<>();

	public ServerBlockDestroyTracker(ServerLevel level) {
		this.level = level;
	}

	public static BlockBreakResult addBlockDestroyProgress(ServerLevel level, @Nullable Entity entity, 
			BlockPos blockPos, BlockState blockState, float progress/*, int ticksBeforeRevert*/) {
		return addBlockDestroyProgress(level, entity, blockPos, blockState, progress, true);
	}

	public static BlockBreakResult addBlockDestroyProgress(ServerLevel level, @Nullable Entity entity, 
			BlockPos blockPos, BlockState blockState, float progress/*, int ticksBeforeRevert*/, boolean removeOnFull) {
		BlockBreakResult res = BlockBreakResult.instance;
		res.progressNew = 0;
		res.progressAdded = 0;
		
		float hardness = blockState.getDestroySpeed(level, blockPos);
		if (hardness < 0) { // unbreakable blocks like bedrock
			return res;
		}
		
		ServerBlockDestroyTracker tracker = level.getData(ModDataAttachmentTypes.BLOCK_DESTROY.get());
		if (tracker == null) {
			return res;
		}
		
		BlockDestroy blockProgress = tracker.blockDestroy.computeIfAbsent(blockPos, 
				pos -> new BlockDestroy(counter.getAndIncrement() % 16383 /* 2 bytes of varint */));
//		blockProgress.ticksBeforeRevert = ticksBeforeRevert;
		blockProgress.ticksBeforeRevert = 40;
		
		float prevDestroy = blockProgress.getProgress();
		float newDestroy = blockProgress.curProgress + progress;
		if (!removeOnFull) {
			newDestroy = Math.min(newDestroy, 0.9999f);
		}
		boolean remove = blockProgress.setAndSyncProgress(newDestroy, blockPos, level);
		if (remove) {
			tracker.blockDestroy.remove(blockPos);
		}
		res.progressNew = blockProgress.curProgress;
		res.progressAdded = blockProgress.curProgress - prevDestroy;
		return res;
	}
	
	public static class BlockBreakResult {
		public float progressNew;
		public float progressAdded;
		
		protected static BlockBreakResult instance = new BlockBreakResult();
	}

	public void tickPost() {
		var iter = blockDestroy.entrySet().iterator();
		while (iter.hasNext()) {
			var blockEntry = iter.next();
			BlockPos blockPos = blockEntry.getKey();
			BlockDestroy progress = blockEntry.getValue();
			BlockState blockState = level.getBlockState(blockPos);
			if (blockState.isAir()) {
				progress.curProgress = 0;
				sync(blockPos, progress, level);
				iter.remove();
			}
			else {
				if (progress.ticksBeforeRevert > 0) {
					--progress.ticksBeforeRevert;
				}
				else {
					float reductionPerTick = 1;//0.01f;
					boolean remove = progress.setAndSyncProgress(progress.curProgress - reductionPerTick, blockPos, level);
					if (remove) {
						iter.remove();
					}
				}
			}

		}
	}

	@ApiStatus.Internal
	public static class BlockDestroy implements BlockBreaking {
		public final int id;
		public float curProgress;
		public int ticksBeforeRevert;

		protected BlockDestroy(int id) {
			this.id = id;
		}
		
		@Override
		public float getProgress() {
			return curProgress;
		}
		
		@Override
		public boolean setAndSyncProgress(float value, BlockPos blockPos, ServerLevel level) {
			int prevProgress = this.getVanillaProgressValue();
			this.curProgress = Mth.clamp(value, 0, 1);
			int newProgress = this.getVanillaProgressValue();
			if (newProgress != prevProgress) {
				sync(blockPos, this, level);
			}
			return this.curProgress <= 0 || this.curProgress >= 1;
		}

		public static final int VANILLA_SCALE = 10;
		public int getVanillaProgressValue() {
			return curProgress <= 0 ? -1 : Mth.clamp((int) (curProgress * VANILLA_SCALE), 0, VANILLA_SCALE);
		}
	}
	
	protected static void sync(BlockPos blockPos, BlockDestroy progress, ServerLevel level) {
		for (ServerPlayer serverplayer : level.getServer().getPlayerList().getPlayers()) {
			if (serverplayer != null && serverplayer.level() == level) {
				double d0 = (double)blockPos.getX() - serverplayer.getX();
				double d1 = (double)blockPos.getY() - serverplayer.getY();
				double d2 = (double)blockPos.getZ() - serverplayer.getZ();
				if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0) {
					serverplayer.connection.send(new ClientboundBlockDestructionPacket(
							progress.id, blockPos, progress.getVanillaProgressValue()));
				}
			}
		}
	}
	
	
	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		Level level = event.getLevel();
		if (!level.isClientSide()) {
			var attachmentType = ModDataAttachmentTypes.BLOCK_DESTROY.get();
			if (level.hasData(attachmentType)) {
				ServerBlockDestroyTracker tracker = level.getData(attachmentType);
				tracker.tickPost();
			}
		}
	}
	
	public static float getBlockDestroyProgress(ServerLevel level, BlockPos blockPos) {
		var attachmentType = ModDataAttachmentTypes.BLOCK_DESTROY.get();
		if (level.hasData(attachmentType)) {
			ServerBlockDestroyTracker tracker = level.getData(attachmentType);
			BlockDestroy progress = tracker.blockDestroy.get(blockPos);
			if (progress != null) {
				return progress.curProgress;
			}
		}
		return 0;
	}

}
