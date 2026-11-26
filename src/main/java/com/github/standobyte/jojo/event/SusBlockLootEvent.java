package com.github.standobyte.jojo.event;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * SusBlockLootEvent is fired when a player brushes a block with BrushableBlockEntity 
 * (suspicious sand/gravel), when the loot table is about to be unpacked.<br>
 * <br>
 * This event is fired via the {@link ModEventHooks#onSusBlockLootTableUnpack(LivingEntity, ServerLevel, BlockPos)}.<br>
 * <br>
 * {@link #replacementItem} is an item that the vanilla archeology drop will be replaced with. 
 * If it's null - the vanilla item is not replaced with anything.
 * <br>
 * This event is not {@link ICancellableEvent}.<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 **/
public class SusBlockLootEvent extends Event {
	public final LivingEntity player;
	public final ServerLevel level;
	public final BlockPos blockPos;
	@Nullable public ItemStack replacementItem;
	public boolean sendMenacingParticles = false;
	
	public SusBlockLootEvent(LivingEntity player, ServerLevel level, BlockPos blockPos) {
		this.player = player;
		this.level = level;
		this.blockPos = blockPos;
	}

}
