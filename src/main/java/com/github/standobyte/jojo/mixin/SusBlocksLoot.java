package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.adventure.SpawnArrowsInSusBlocks;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.event.ModEventHooks;
import com.github.standobyte.jojo.event.SusBlockLootEvent;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

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
	@Unique private boolean addMenacingParticles = false;

	@Inject(method = "unpackLootTable", at = @At("HEAD"))
	private void replaceUnpackedItem(Player player, CallbackInfo ci) {
		if (!rolledSpecialLoot && item.isEmpty()) {
			SusBlockLootEvent event = ModEventHooks.onSusBlockLootTableUnpack(player, (ServerLevel) level, worldPosition);
			boolean changed = false;
			if (event.replacementItem != null) {
				item = event.replacementItem;
				lootTable = null;
				changed = true;
			}
			if (event.sendMenacingParticles) {
				addMenacingParticles = event.sendMenacingParticles;
				changed = true;
			}
			if (changed) {
				setChanged();
			}
		}
		rolledSpecialLoot = true;
	}

	@WrapMethod(method = "saveAdditional")
	private void addCustomFieldsToSaved(CompoundTag tag, HolderLookup.Provider registries, Operation<Void> original) {
		original.call(tag, registries);
		addFieldsToNBT(tag);
	}
	
	// I SWEAR it worked without this before
	@WrapMethod(method = "getUpdateTag")
	private CompoundTag addCustomFieldsToSynched(HolderLookup.Provider registries, Operation<CompoundTag> original) {
		CompoundTag nbt = original.call(registries);
		addFieldsToNBT(nbt);
		return nbt;
	}
	
	@Unique
	private void addFieldsToNBT(CompoundTag tag) {
		tag.putBoolean(JojoMod.MOD_ID + ":rolledSpecialLoot", rolledSpecialLoot);
		tag.putBoolean(JojoMod.MOD_ID + ":gogogogogo", addMenacingParticles);
	}

	@WrapMethod(method = "loadAdditional")
	private void loadCustomFields(CompoundTag tag, HolderLookup.Provider registries, Operation<Void> original) {
		original.call(tag, registries);
		rolledSpecialLoot = tag.getBoolean(JojoMod.MOD_ID + ":rolledSpecialLoot");
		addMenacingParticles = tag.getBoolean(JojoMod.MOD_ID + ":gogogogogo");
		if (level != null && level.isClientSide() && addMenacingParticles) {
			SpawnArrowsInSusBlocks.onItemSynchedToClient_addParticles(this, item);
		}
	}
	
	
}
