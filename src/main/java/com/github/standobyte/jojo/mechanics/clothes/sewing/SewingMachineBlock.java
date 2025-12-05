package com.github.standobyte.jojo.mechanics.clothes.sewing;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SewingMachineBlock extends HorizontalDirectionalBlock {

	public SewingMachineBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}
	
	public static final MapCodec<SewingMachineBlock> CODEC = simpleCodec(SewingMachineBlock::new);
	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }

	@Override
	public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult rayTrace) {
		if (!level.isClientSide) {
			player.openMenu(blockState.getMenuProvider(level, blockPos));
//			player.awardStat(ModCustomStats.INTERACT_WITH_SEWING_MACHINE);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return new SimpleMenuProvider((id, inventory, player) -> {
			return new SewingMachineContainer(id, inventory, ContainerLevelAccess.create(level, blockPos));
		}, CommonComponents.EMPTY);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);
		switch (direction) {
			case NORTH:
				return NORTH_SHAPE;
			case EAST:
				return EAST_SHAPE;
			case SOUTH:
				return SOUTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			default:
				return BASE;
		}
	}

	protected static final VoxelShape BASE = box(0, 0, 0, 16, 4, 16);

	protected static final VoxelShape NORTH_ARM =          box(3, 8, 6.5D, 14, 11, 9.5D);
	protected static final VoxelShape NORTH_PILLAR =       box(4, 4, 6.5D, 7, 8, 9.5D);
	protected static final VoxelShape NORTH_SEWING_HEAD =  box(12, 5, 6.5D, 14, 8, 9.5D);
	protected static final VoxelShape NORTH_NEEDLE =       box(13.3D, 4.1D, 7.5D, 13.8D, 5, 8);
	protected static final VoxelShape NORTH_WHEEL =        box(2, 7, 5.5D, 3, 12, 10.5D);
	protected static final VoxelShape NORTH_STRING_SPOOL = box(5, 11, 7.5D, 6, 13, 8.5D);

	protected static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, NORTH_ARM, NORTH_PILLAR, NORTH_SEWING_HEAD, NORTH_NEEDLE, NORTH_WHEEL, NORTH_STRING_SPOOL);

	protected static final VoxelShape EAST_ARM =          box(6.5D, 8, 3, 9.5D, 11, 14);
	protected static final VoxelShape EAST_PILLAR =       box(6.5D, 4, 4, 9.5D, 8, 7);
	protected static final VoxelShape EAST_SEWING_HEAD =  box(6.5D, 5, 12, 9.5D, 8, 14);
	protected static final VoxelShape EAST_NEEDLE =       box(8.5D, 4.1D, 13.3D, 8, 5, 13.8D);
	protected static final VoxelShape EAST_WHEEL =        box(5.5D, 7, 2, 10.5D, 12, 3);
	protected static final VoxelShape EAST_STRING_SPOOL = box(7.5D, 11, 5, 8.5D, 13, 6);

	protected static final VoxelShape EAST_SHAPE = Shapes.or(BASE, EAST_ARM, EAST_PILLAR, EAST_SEWING_HEAD, EAST_NEEDLE, EAST_WHEEL, EAST_STRING_SPOOL);

	protected static final VoxelShape SOUTH_ARM =          box(13, 8, 6.5D, 2, 11, 9.5D);
	protected static final VoxelShape SOUTH_PILLAR =       box(12, 4, 6.5D, 9, 8, 9.5D);
	protected static final VoxelShape SOUTH_SEWING_HEAD =  box(4, 5, 6.5D, 2, 8, 9.5D);
	protected static final VoxelShape SOUTH_NEEDLE =       box(2.7D, 4.1D, 8.5D, 2.2D, 5, 8);
	protected static final VoxelShape SOUTH_WHEEL =        box(14, 7, 5.5D, 13, 12, 10.5D);
	protected static final VoxelShape SOUTH_STRING_SPOOL = box(11, 11, 7.5D, 10, 13, 8.5D);

	protected static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, SOUTH_ARM, SOUTH_PILLAR, SOUTH_SEWING_HEAD, SOUTH_NEEDLE, SOUTH_WHEEL, SOUTH_STRING_SPOOL);

	protected static final VoxelShape WEST_ARM =          box(9.5D, 8, 13, 6.5D, 11, 2);
	protected static final VoxelShape WEST_PILLAR =       box(9.5D, 4, 12, 6.5D, 8, 9);
	protected static final VoxelShape WEST_SEWING_HEAD =  box(9.5D, 5, 4, 6.5D, 8, 2);
	protected static final VoxelShape WEST_NEEDLE =       box(7.5D, 4.1D, 2.7D, 8, 5, 2.2D);
	protected static final VoxelShape WEST_WHEEL =        box(10.5D, 7, 14, 5.5D, 12, 13);
	protected static final VoxelShape WEST_STRING_SPOOL = box(8.5D, 11, 11, 7.5D, 13, 10);

	protected static final VoxelShape WEST_SHAPE = Shapes.or(BASE, WEST_ARM, WEST_PILLAR, WEST_SEWING_HEAD, WEST_NEEDLE, WEST_WHEEL, WEST_STRING_SPOOL);

	
	public static VoxelShape box(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Block.box(
				Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), 
				Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
	}
}