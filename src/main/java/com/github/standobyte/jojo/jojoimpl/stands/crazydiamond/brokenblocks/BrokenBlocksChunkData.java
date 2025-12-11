package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.brokenblocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModBlockEntities;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;
import com.github.standobyte.jojo.util.entitycomponent.ComponentUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class BrokenBlocksChunkData {
	public final LevelChunk chunk;

	public boolean loadedNBT = false;
	public final Map<BlockPos, PrevBlockInfo> brokenBlocks = new HashMap<>();
	public final List<PrevBlockInfo> blocksToSync = new ArrayList<>();
//	public Set<ServerPlayer> syncedTo = new HashSet<>();

	public BrokenBlocksChunkData(LevelChunk chunk) {
		this.chunk = chunk;
	}

	@Nullable
	public PrevBlockInfo saveBrokenBlock(BlockPos pos, BlockState state, Optional<BlockEntity> tileEntity, List<ItemStack> drops) {
		// FIXME remember blocks with inventory
		if (tileEntity.filter(te -> te instanceof Container || te.getType() == ModBlockEntities._PLACEHOLDER_STONE_MASK.get()).isPresent()) return null;

		PrevBlockInfo blockInfo = new PrevBlockInfo(pos, state, drops, false);
		saveBrokenBlock(blockInfo);
		return blockInfo;
	}

	protected void saveBrokenBlock(PrevBlockInfo prevBlock) {
		brokenBlocks.put(prevBlock.pos, prevBlock);
		if (!chunk.getLevel().isClientSide()) {
			blocksToSync.add(prevBlock);
		}
	}

	public void removeBrokenBlock(BlockPos blockPos) {
		brokenBlocks.remove(blockPos);
		if (!chunk.getLevel().isClientSide()) {
			blocksToSync.add(PrevBlockInfo.clientInstance(blockPos, Blocks.AIR.defaultBlockState()));
		}
	}

	public void reset() {
		brokenBlocks.clear();
		Level level = chunk.getLevel();
		if (!level.isClientSide()) {
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, chunk.getPos(), new BrokenChunkBlocksPacket(Collections.emptyList(), true));
		}
	}

	public void tick() {
		Level level = chunk.getLevel();
		if (!level.isClientSide()) {
			if (loadedNBT) {
				Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = brokenBlocks.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
					if (CrazyDRestoreTerrainAbility.blockCanBePlaced(chunk.getLevel(), entry.getKey(), entry.getValue().state)) {
						blocksToSync.add(entry.getValue());
					}
					else {
						it.remove();
					}
				}
				loadedNBT = false;
			}

			else {
//				Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = brokenBlocks.entrySet().iterator();
//				while (it.hasNext()) {
//					Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
//					if (entry.getValue().forget()) {
//						it.remove();
//						blocksToSync.add(PrevBlockInfo.clientInstance(entry.getKey(), Blocks.AIR.defaultBlockState()));
//					}
//				}
			}

			if (!blocksToSync.isEmpty()) {
				PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, chunk.getPos(), new BrokenChunkBlocksPacket(new ArrayList<>(blocksToSync), false));
				blocksToSync.clear();
//				syncedTo = ((ServerChunkProvider) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false)
//						.collect(Collectors.toSet());
			}
		}
	}

	// FIXME fix the blocks resetting on client after being synced
	public void onChunkLoad(ServerPlayer player) {
//		if (!chunk.getLevel().isClientSide() && !syncedTo.contains(player) && !brokenBlocks.isEmpty()) {
//			PacketDistributor.sendToClient(new BrokenChunkBlocksPacket(brokenBlocks.values(), true), player);
//			syncedTo.add(player);
//		}
	}

	public PrevBlockInfo getBrokenBlockAt(BlockPos blockPos) {
		return brokenBlocks.get(blockPos);
	}

	public Stream<PrevBlockInfo> getBrokenBlocks() {
		return brokenBlocks.values().stream();
	}

	public boolean wasBlockBroken(BlockPos pos) {
		return brokenBlocks.containsKey(pos);
	}


	public CompoundTag save(HolderLookup.Provider registries) {
		CompoundTag nbt = new CompoundTag();
		boolean saveDataConfig = false; // JojoModConfig.getCommonConfigInstance(false).saveDestroyedBlocks.get();
		if (saveDataConfig) {
			ListTag blocksBroken = new ListTag();
			for (PrevBlockInfo block : brokenBlocks.values()) {
				blocksBroken.add(block.toNBT(registries));
			}
			nbt.put("Blocks", blocksBroken);
		}
		return nbt;
	}

	public void load(CompoundTag nbt, HolderLookup.Provider registries) {
		boolean saveDataConfig = false; // JojoModConfig.getCommonConfigInstance(false).saveDestroyedBlocks.get();
		if (saveDataConfig
				&& nbt.contains("Blocks", Tag.TAG_LIST)) {
			nbt.getList("Blocks", Tag.TAG_COMPOUND).forEach(blockNBT -> {
				PrevBlockInfo block = PrevBlockInfo.fromNBT((CompoundTag) blockNBT, registries);
				if (block != null) {
					brokenBlocks.put(block.pos, block);
				}
			});
		}
		loadedNBT = true;
	}

	
	
	public static BrokenBlocksChunkData getChunkData(Level level, BlockPos blockPos) {
		ChunkAccess chunkAccess = level.getChunk(blockPos);
		if (chunkAccess instanceof LevelChunk chunk) {
			return chunk.getData(ModDataAttachmentTypes.BROKEN_BLOCKS);
		}
		return null;
	}
	
	public static BrokenBlocksChunkData getChunkData(LevelChunk chunk) {
		return chunk.getData(ModDataAttachmentTypes.BROKEN_BLOCKS);
	}
	
	@Nullable
	public static BrokenBlocksChunkData getExistingData(Level level, BlockPos blockPos) {
		ChunkAccess chunkAccess = level.getChunk(blockPos);
		if (chunkAccess instanceof LevelChunk chunk) {
			return ComponentUtil.getExistingDataOrNull(chunk, ModDataAttachmentTypes.BROKEN_BLOCKS);
		}
		return null;
	}
	
	@Nullable
	public static BrokenBlocksChunkData getExistingData(LevelChunk chunk) {
		return ComponentUtil.getExistingDataOrNull(chunk, ModDataAttachmentTypes.BROKEN_BLOCKS);
	}


	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Post event) {
		Level level = event.getLevel();
		if (!level.isClientSide()) {
			((ServerLevel) level).getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
				LevelChunk chunk = chunkHolder.getTickingChunk();
				if (chunk != null) {
					BrokenBlocksChunkData data = getExistingData(chunk);
					if (data != null) data.tick();
				}
			});
		}
	}
}
