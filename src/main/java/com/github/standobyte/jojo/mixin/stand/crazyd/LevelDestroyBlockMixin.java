package com.github.standobyte.jojo.mixin.stand.crazyd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Level.class)
public abstract class LevelDestroyBlockMixin {
	private Map<BlockPos, Pair<BlockState, Optional<BlockEntity>>> jojo_ripples$oldBlockTmp = new HashMap<>();

	@Shadow public abstract BlockEntity getBlockEntity(BlockPos pos);

	@Inject(method = "destroyBlock", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;"
					+ "getFluidState("
					+ "Lnet/minecraft/core/BlockPos;"
					+ ")Lnet/minecraft/world/level/material/FluidState;"),
			locals = LocalCapture.CAPTURE_FAILSOFT)
	public void jojo_ripples$rememberOldBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft, CallbackInfoReturnable<Boolean> ci, 
			BlockState blockstate) {
		if (!dropBlock && blockstate != null) {
			BlockEntity blockEntity = blockstate.hasBlockEntity() ? this.getBlockEntity(pos) : null;
			jojo_ripples$oldBlockTmp.put(pos.immutable(), Pair.of(blockstate, Optional.ofNullable(blockEntity)));
		}
	}

	@Inject(method = "destroyBlock", at = @At(
			value = "RETURN",
			ordinal = 1),
			require = 1)
	public void jojo_ripples$onBlockDestroyNoDrops(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft, CallbackInfoReturnable<Boolean> ci) {
		if (!dropBlock) {
			Pair<BlockState, Optional<BlockEntity>> oldBlock = jojo_ripples$oldBlockTmp.remove(pos);
			if (ci.getReturnValueZ() && oldBlock != null) {
				Level level = (Level) (Object) this;
				CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, pos, 
						oldBlock.getLeft(), 
						oldBlock.getRight(), 
						Collections.emptyList());
			}
		}
	}
}
