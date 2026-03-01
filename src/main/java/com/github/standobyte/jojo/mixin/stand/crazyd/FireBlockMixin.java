package com.github.standobyte.jojo.mixin.stand.crazyd;

import java.util.Collections;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FireBlock.class)
public class FireBlockMixin {

	@Inject(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	public void jojoOnFireRemovedBlock(Level pLevel, BlockPos pPos, int pChance, RandomSource pRandom, int pAge, Direction face, CallbackInfo ci) {
		cdRememberBurntBlock(pLevel, pPos);
	}

	@Inject(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
	public void jojoOnFireReplacedBlock(Level pLevel, BlockPos pPos, int pChance, RandomSource pRandom, int pAge, Direction face, CallbackInfo ci) {
		cdRememberBurntBlock(pLevel, pPos);
	}

	private static void cdRememberBurntBlock(Level world, BlockPos blockPos) {
		BlockState blockState = world.getBlockState(blockPos);
		CrazyDRestoreTerrainAbility.rememberBrokenBlock(world, blockPos, blockState, 
				Optional.ofNullable(world.getBlockEntity(blockPos)), Collections.emptyList());
	}

	// FIXME crossfire hurricane deleting blocks

}
