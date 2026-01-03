package com.github.standobyte.jojo.mixin.stand.crazyd;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Level.class)
public abstract class LevelDestroyBlockMixin {
	private BlockState jojo_ripples$oldBlockState;
	@Nullable private BlockEntity jojo_ripples$oldBlockEntity;

	@Shadow public abstract BlockEntity getBlockEntity(BlockPos pos);

	@Inject(method = "destroyBlock", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;"
					+ "getFluidState("
					+ "Lnet/minecraft/core/BlockPos;"
					+ ")Lnet/minecraft/world/level/material/FluidState;"),
			require = 1,
			locals = LocalCapture.CAPTURE_FAILSOFT)
	public void jojo_ripples$rememberOldBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft, CallbackInfoReturnable<Boolean> ci, 
			BlockState blockstate) {
		if (!dropBlock) {
			jojo_ripples$oldBlockState = blockstate;
			if (jojo_ripples$oldBlockState != null) {
				jojo_ripples$oldBlockEntity = jojo_ripples$oldBlockState.hasBlockEntity() ? this.getBlockEntity(pos) : null;
			}
		}
	}

	@Inject(method = "destroyBlock", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;"
					+ "gameEvent("
					+ "Lnet/minecraft/core/Holder;"
					+ "Lnet/minecraft/core/BlockPos;"
					+ "Lnet/minecraft/world/level/gameevent/GameEvent$Context;"
					+ ")V"),
			require = 1)
	public void jojo_ripples$onBlockDestroyNoDrops(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft, CallbackInfoReturnable<Boolean> ci) {
		if (!dropBlock && jojo_ripples$oldBlockState != null) {
			Level level = (Level) (Object) this;
			CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, pos, 
					jojo_ripples$oldBlockState, 
					Optional.ofNullable(jojo_ripples$oldBlockEntity), 
					Collections.emptyList(), false);
		}
		jojo_ripples$oldBlockState = null;
		jojo_ripples$oldBlockEntity = null;
	}
}
