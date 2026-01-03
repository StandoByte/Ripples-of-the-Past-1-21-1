package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.brokenblocks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PrevBlockInfo {
	public final BlockPos pos;
	public final BlockState state;

	public final List<ItemStack> drops;
	private int xp = 0;
	public List<WeakReference<EntityMadeFromBlock>> blockShards;

	public final boolean alwaysKeepNBT;
//	private int tickCount = 0;

	public PrevBlockInfo(BlockPos pos, BlockState state, List<ItemStack> drops, boolean alwaysKeepNBT) {
		this.pos = pos;
		this.state = state;
		this.alwaysKeepNBT = alwaysKeepNBT;
		
		this.drops = new ArrayList<>(drops.size());
		for (ItemStack droppedItem : drops) {
			boolean foundMatching = false;
			for (ItemStack recordedCost : this.drops) {
				if (CrazyDRestoreTerrainAbility.stacksMatch(recordedCost, droppedItem)) {
					recordedCost.setCount(recordedCost.getCount() + droppedItem.getCount());
					foundMatching = true;
					break;
				}
			}
			if (!foundMatching) {
				this.drops.add(droppedItem.copy());
			}
		}
	}

	public static PrevBlockInfo clientInstance(BlockPos pos, BlockState state) {
		return new PrevBlockInfo(pos, state, new ArrayList<>(), false);
	}

	public void setDroppedXp(int xp) {
		this.xp = xp;
	}

	public int getDroppedXp() {
		return xp;
	}

	public void withEntities(EntityMadeFromBlock... blockShardEntities) {
		this.blockShards = Arrays.stream(blockShardEntities).map(WeakReference::new).collect(Collectors.toList());
	}


	@Nullable
	public CompoundTag toNBT(HolderLookup.Provider registries, boolean keepEverything) {
		if (!(keepEverything || this.alwaysKeepNBT)) {
			return null;
		}
		
		CompoundTag nbt = new CompoundTag();
		BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).ifSuccess(
				tag -> nbt.put("Pos", tag));
		BlockState.CODEC.encodeStart(NbtOps.INSTANCE, state).ifSuccess(
				tag -> nbt.put("State", tag));
		nbt.putBoolean("Keep", alwaysKeepNBT);
		
		ListTag itemsNBT = new ListTag();
		for (ItemStack stack : drops) {
			itemsNBT.add(stack.save(registries));
		}
		nbt.put("Drops", itemsNBT);
		nbt.putInt("Xp", xp);

		return nbt;
	}

	@Nullable
	public static PrevBlockInfo fromNBT(CompoundTag nbt, HolderLookup.Provider registries, boolean loadEverything) {
		if (!(
				nbt.contains("Pos") &&
				nbt.contains("State") && 
				nbt.contains("Drops", Tag.TAG_LIST))) {
			return null;
		}
		
		boolean keepNBT = nbt.getBoolean("Keep");
		if (!(keepNBT || loadEverything)) return null;

		List<ItemStack> drops = new ArrayList<>();
		ListTag dropsNBT = nbt.getList("Drops", Tag.TAG_COMPOUND);
		for (Tag nbtElement : dropsNBT) {
			CompoundTag itemNBT = (CompoundTag) nbtElement;
			ItemStack item = ItemStack.parseOptional(registries, itemNBT);
			if (!item.isEmpty()) {
				drops.add(item);
			}
		}
		
		BlockPos pos = BlockPos.CODEC.parse(NbtOps.INSTANCE, nbt.get("Pos")).result().orElse(null);
		if (pos == null) return null;
		BlockState state = BlockState.CODEC.parse(NbtOps.INSTANCE, nbt.get("State")).result().orElse(null);
		if (state == null) return null;

		PrevBlockInfo block = new PrevBlockInfo(pos, state, drops, keepNBT);
		block.xp = nbt.getInt("Xp");
		return block;
	}
	
	public static final StreamCodec<RegistryFriendlyByteBuf, PrevBlockInfo> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, block -> block.pos, 
			ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), block -> block.state, 
			PrevBlockInfo::clientInstance);

	public void toBuf(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeVarInt(Block.getId(state));
	}

	public static PrevBlockInfo fromBuf(FriendlyByteBuf buf) {
		return new PrevBlockInfo(buf.readBlockPos(), Block.stateById(buf.readVarInt()), new ArrayList<>(), false);
	}
}