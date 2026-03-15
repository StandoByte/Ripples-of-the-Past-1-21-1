package com.github.standobyte.jojoimpl.stands.crazydiamond.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.standobyte.jojo.client.ui.marker.MarkerRenderer;
import com.github.standobyte.jojo.subsystems.itemtracking.OriginalItemPosMarker;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.BrokenBlocksChunkData;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.PrevBlockInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class CrazyDOriginPosAnchorMarker extends MarkerRenderer {

	public CrazyDOriginPosAnchorMarker(Minecraft mc) {
		super("block_anchor", mc);
		renderThroughBlocks = false;
		useStandSkinColor = true;
	}

	@Override
	protected boolean shouldRender() {
		return TranslucentBlockRenderHelper.toRender() != null;
	}

	@Override
	protected void updatePositions(List<MarkerInstance> list, float partialTick) {
		Level level = mc.level;
		Map<ChunkPos, BrokenBlocksChunkData> cacheByChunk = new HashMap<>();
		
		Set<BlockPos> exclude = HashSet.newHashSet(4);
		OriginalItemPosMarker.iterateHeldItemsOriginalPos((blockPos, item) -> exclude.add(blockPos));
		
		for (BlockPos blockPos : TranslucentBlockRenderHelper.highlightedBlocks) {
			if (exclude.contains(blockPos)) continue;
			
			BrokenBlocksChunkData chunkData = cacheByChunk.computeIfAbsent(new ChunkPos(blockPos), 
					chunkPos -> BrokenBlocksChunkData.getChunkData(level, blockPos));
			PrevBlockInfo block = chunkData.getBrokenBlockAt(blockPos);
			if (block != null && block.hasAnchorDrop) {
				list.add(new MarkerInstance(blockMarkerPos(blockPos)));
			}
		}
	}
}
