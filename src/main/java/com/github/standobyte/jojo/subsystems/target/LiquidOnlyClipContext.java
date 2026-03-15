package com.github.standobyte.jojo.subsystems.target;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidOnlyClipContext extends ClipContext {

	public LiquidOnlyClipContext(Vec3 from, Vec3 to, ClipContext.Fluid liquid,
			Entity collidingEntity) {
		super(from, to, null, liquid, collidingEntity);
	}
	
	@Override
	public VoxelShape getBlockShape(BlockState blockState, BlockGetter world, BlockPos blockPos) {
		return Shapes.empty();
	}
}
