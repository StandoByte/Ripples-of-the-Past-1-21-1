package com.github.standobyte.jojo.mechanics.clothes.sewing;

import java.util.Map;

import com.github.standobyte.jojo.util.functions.BlockUtil.BoxShapeHorizontalRot;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SewingMachineBlock extends HorizontalDirectionalBlock implements EntityBlock {

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
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SewingMachineBlockEntity(pos, state);
	}


	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);
		
		return switch (direction) {
			case NORTH -> NORTH_SHAPE;
			case EAST -> EAST_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			default -> Shapes.block();
		};
	}

	protected static final BoxShapeHorizontalRot BASE = 		BoxShapeHorizontalRot.box(3, 0, 5.5, 15, 2, 10.5);
	protected static final BoxShapeHorizontalRot ARM =			BoxShapeHorizontalRot.box(3, 6.5, 6.5, 14, 9.5, 9.5);
	protected static final BoxShapeHorizontalRot PILLAR =		BoxShapeHorizontalRot.box(4, 2, 6, 7.5, 7.5, 10);
	protected static final BoxShapeHorizontalRot SEWING_HEAD =	BoxShapeHorizontalRot.box(12, 4.5, 6.5, 14, 6.5, 9.5);
	protected static final BoxShapeHorizontalRot NEEDLE =		BoxShapeHorizontalRot.box(13.3, 4.1, 7.5, 13.8, 5, 8);
	protected static final BoxShapeHorizontalRot WHEEL =		BoxShapeHorizontalRot.box(2, 5.5, 5.5, 3, 10.5, 10.5);
	protected static final BoxShapeHorizontalRot STRING_SPOOL =	BoxShapeHorizontalRot.box(7, 9.5, 7.5, 8, 11.4, 8.5);
	
	protected static final VoxelShape NORTH_SHAPE;
	protected static final VoxelShape EAST_SHAPE;
	protected static final VoxelShape SOUTH_SHAPE;
	protected static final VoxelShape WEST_SHAPE;
	static {
		Map<Direction, VoxelShape> rotated = BoxShapeHorizontalRot.or(BASE, ARM, PILLAR, SEWING_HEAD, NEEDLE, WHEEL, STRING_SPOOL);
		NORTH_SHAPE = rotated.get(Direction.NORTH);
		EAST_SHAPE = rotated.get(Direction.EAST);
		SOUTH_SHAPE = rotated.get(Direction.SOUTH);
		WEST_SHAPE = rotated.get(Direction.WEST);
	}

	
	public static VoxelShape box(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Block.box(
				Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), 
				Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
	}
	
}