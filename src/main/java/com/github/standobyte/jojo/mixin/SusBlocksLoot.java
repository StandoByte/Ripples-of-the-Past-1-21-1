package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.adventure.SpawnArrowsInSusBlocks;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(BrushableBlockEntity.class)
public abstract class SusBlocksLoot extends BlockEntity {
	public SusBlocksLoot(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	@Shadow private ItemStack item = ItemStack.EMPTY;
	@Shadow private ResourceKey<LootTable> lootTable;
	@Unique private boolean rolledSpecialLoot = false;

	@Inject(method = "unpackLootTable", at = @At("HEAD"))
	public void replaceUnpackedItem(Player player, CallbackInfo ci) {
		if (!rolledSpecialLoot && this.item.isEmpty()) {
			ItemStack arrow = SpawnArrowsInSusBlocks.standArrowToPut(player, (ServerLevel) level, worldPosition);
			if (arrow != null) {
				this.item = arrow;
				this.lootTable = null;
			}
		}
		rolledSpecialLoot = true;
	}

	@Inject(method = "saveAdditional", at = @At("TAIL"))
	public void rotp$saveAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
		tag.putBoolean(JojoMod.MOD_ID + ":rolledSpecialLoot", rolledSpecialLoot);
	}

	@Inject(method = "loadAdditional", at = @At("TAIL"))
	public void rotp$loadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
		rolledSpecialLoot = tag.getBoolean(JojoMod.MOD_ID + ":rolledSpecialLoot");
		if (level != null && level.isClientSide()) {
			SpawnArrowsInSusBlocks.onItemSynchedToClient(this, item);
		}
	}
	
}
