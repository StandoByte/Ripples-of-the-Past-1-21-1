package com.github.standobyte.jojo.mechanics.standarrow;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class MeteoriteCoreBlock extends Block {

	public MeteoriteCoreBlock(Properties properties) {
		super(properties);
	}

	@Override
	public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, 
			@Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
		return 30;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, (new AABB(x, y, z, x, y, z)).inflate(2))) {
			StandVirusEffect.onNearbyVirusSource(entity);
		}
		level.scheduleTick(pos, this, 10);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		double d0 = (double)((float)pos.getX() + random.nextFloat() * 4F - 2F);
		double d1 = (double)((float)pos.getY() + random.nextFloat() * 4F - 2F);
		double d2 = (double)((float)pos.getZ() + random.nextFloat() * 4F - 2F);
		level.addParticle(ModParticles.METEORITE_VIRUS.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}

	@Deprecated
	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, 
			LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		level.scheduleTick(currentPos, this, 10);
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		level.scheduleTick(pos, this, 10);
	}
}
