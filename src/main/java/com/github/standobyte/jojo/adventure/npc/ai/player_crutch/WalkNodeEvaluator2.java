package com.github.standobyte.jojo.adventure.npc.ai.player_crutch;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;

public class WalkNodeEvaluator2 extends WalkNodeEvaluator {
	protected LivingEntity entity;
	
	// NodeEvaluator stuff

	@Deprecated @Override public void prepare(PathNavigationRegion level, Mob mob) {}
	public void prepare(PathNavigationRegion level, LivingEntity entity) {
		this.currentContext = createContext(level, entity.level(), entity.blockPosition());
		this.entity = entity;
		this.nodes.clear();
		this.entityWidth = Mth.floor(entity.getBbWidth() + 1.0F);
		this.entityHeight = Mth.floor(entity.getBbHeight() + 1.0F);
		this.entityDepth = Mth.floor(entity.getBbWidth() + 1.0F);
		
		//entity.onPathfindingStart(); // Sniffer stuff:
//		if (entity.isOnFire() || entity.isInWater()) {
//			entity.setPathfindingMalus(PathType.WATER, 0.0F);
//		}
	}

	@Override
	public void done() {
		//entity.onPathfindingDone(); // Sniffer stuff:
//		entity.setPathfindingMalus(PathType.WATER, -1.0F);
		
		this.pathTypesByPosCacheByMob.clear();
		this.collisionCache.clear();
		this.currentContext = null;
		this.entity = null;
	}

	@Deprecated @Override public PathType getPathType(Mob mob, BlockPos pos) { return null; }
	public PathType getPathType(LivingEntity entity, BlockPos pos) {
		return this.getPathType(createContext(entity.level(), entity.level(), entity.blockPosition()), pos.getX(), pos.getY(), pos.getZ());
	}

	@Deprecated @Override public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) { return null; }
	public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, LivingEntity entity) {
		Set<PathType> set = this.getPathTypeWithinMobBB(context, x, y, z);
		if (set.contains(PathType.FENCE)) {
			return PathType.FENCE;
		} else if (set.contains(PathType.UNPASSABLE_RAIL)) {
			return PathType.UNPASSABLE_RAIL;
		} else {
			PathType pathtype = PathType.BLOCKED;

			for (PathType pathtype1 : set) {
				if (_MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype1) < 0.0F) {
					return pathtype1;
				}

				if (_MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype1) >= _MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype)) {
					pathtype = pathtype1;
				}
			}

			return this.entityWidth <= 1
					&& pathtype != PathType.OPEN
					&& _MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype) == 0.0F
					&& this.getPathType(context, x, y, z) == PathType.OPEN
					? PathType.OPEN : pathtype;
		}
	}


	public static PathfindingContext createContext(CollisionGetter region, Level level, BlockPos mobPosition) {
		Mob crutch = new Mob(EntityType.ZOMBIE, level) {};
		crutch.setPosRaw(mobPosition.getX(), mobPosition.getY(), mobPosition.getZ());
		return new PathfindingContext(region, crutch);
	}


	// WalkNodeEvaluator stuff

	@Override
	public Node getStart() {
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		int y = entity.getBlockY();
		BlockState blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(entity.getX(), y, entity.getZ()));
		if (!entity.canStandOnFluid(blockstate.getFluidState())) {
			if (this.canFloat() && entity.isInWater()) {
				while (true) {
					if (!blockstate.is(Blocks.WATER) && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
						y--;
						break;
					}

					blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(entity.getX(), ++y, entity.getZ()));
				}
			} else if (entity.onGround()) {
				y = Mth.floor(entity.getY() + 0.5);
			} else {
				blockpos$mutableblockpos.set(entity.getX(), entity.getY() + 1.0, entity.getZ());

				while (blockpos$mutableblockpos.getY() > this.currentContext.level().getMinBuildHeight()) {
					y = blockpos$mutableblockpos.getY();
					blockpos$mutableblockpos.setY(blockpos$mutableblockpos.getY() - 1);
					BlockState blockstate1 = this.currentContext.getBlockState(blockpos$mutableblockpos);
					if (!blockstate1.isAir() && !blockstate1.isPathfindable(PathComputationType.LAND)) {
						break;
					}
				}
			}
		} else {
			while (entity.canStandOnFluid(blockstate.getFluidState())) {
				blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(entity.getX(), ++y, entity.getZ()));
			}

			y--;
		}

		BlockPos blockpos = entity.blockPosition();
		if (!this.canStartAt(blockpos$mutableblockpos.set(blockpos.getX(), y, blockpos.getZ()))) {
			AABB aabb = entity.getBoundingBox();
			if (this.canStartAt(blockpos$mutableblockpos.set(aabb.minX, y, aabb.minZ))
					|| this.canStartAt(blockpos$mutableblockpos.set(aabb.minX, y, aabb.maxZ))
					|| this.canStartAt(blockpos$mutableblockpos.set(aabb.maxX, y, aabb.minZ))
					|| this.canStartAt(blockpos$mutableblockpos.set(aabb.maxX, y, aabb.maxZ))) {
				return this.getStartNode(blockpos$mutableblockpos);
			}
		}

		return this.getStartNode(new BlockPos(blockpos.getX(), y, blockpos.getZ()));
	}

	@Override
	protected Node getStartNode(BlockPos pos) {
		Node node = this.getNode(pos);
		node.type = this.getCachedPathType(node.x, node.y, node.z);
		node.costMalus = _MobAIVanillaClassesHelper.getPathfindingMalus(entity, node.type);
		return node;
	}

	@Override
	protected boolean canStartAt(BlockPos pos) {
		PathType pathtype = this.getCachedPathType(pos.getX(), pos.getY(), pos.getZ());
		return pathtype != PathType.OPEN && _MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype) >= 0.0F;
	}

	@Override
	public int getNeighbors(Node[] outputArray, Node p_node) {
		int i = 0;
		int j = 0;
		PathType pathtype = this.getCachedPathType(p_node.x, p_node.y + 1, p_node.z);
		PathType pathtype1 = this.getCachedPathType(p_node.x, p_node.y, p_node.z);
		if (_MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype) >= 0.0F && pathtype1 != PathType.STICKY_HONEY) {
			j = Mth.floor(Math.max(1.0F, entity.maxUpStep()));
		}

		double d0 = this.getFloorLevel(new BlockPos(p_node.x, p_node.y, p_node.z));

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Node node = this.findAcceptedNode(p_node.x + direction.getStepX(), p_node.y, p_node.z + direction.getStepZ(), j, d0, direction, pathtype1);
			this.reusableNeighbors[direction.get2DDataValue()] = node;
			if (this.isNeighborValid(node, p_node)) {
				outputArray[i++] = node;
			}
		}

		for (Direction direction1 : Direction.Plane.HORIZONTAL) {
			Direction direction2 = direction1.getClockWise();
			if (this.isDiagonalValid(p_node, this.reusableNeighbors[direction1.get2DDataValue()], this.reusableNeighbors[direction2.get2DDataValue()])) {
				Node node1 = this.findAcceptedNode(
						p_node.x + direction1.getStepX() + direction2.getStepX(),
						p_node.y,
						p_node.z + direction1.getStepZ() + direction2.getStepZ(),
						j,
						d0,
						direction1,
						pathtype1
						);
				if (this.isDiagonalValid(node1)) {
					outputArray[i++] = node1;
				}
			}
		}

		return i;
	}

	protected boolean isDiagonalValid(Node root, @Nullable Node xNode, @Nullable Node zNode) {
		if (zNode == null || xNode == null || zNode.y > root.y || xNode.y > root.y) {
			return false;
		} else if (xNode.type != PathType.WALKABLE_DOOR && zNode.type != PathType.WALKABLE_DOOR) {
			boolean flag = zNode.type == PathType.FENCE && xNode.type == PathType.FENCE && entity.getBbWidth() < 0.5f;
			return (zNode.y < root.y || zNode.costMalus >= 0.0F || flag) && (xNode.y < root.y || xNode.costMalus >= 0.0F || flag);
		} else {
			return false;
		}
	}

	@Nullable
	@Override
	protected Node findAcceptedNode(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel, Direction direction, PathType pathType) {
		Node node = null;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		double d0 = this.getFloorLevel(blockpos$mutableblockpos.set(x, y, z));
		if (d0 - nodeFloorLevel > this.getMobJumpHeight()) {
			return null;
		} else {
			PathType pathtype = this.getCachedPathType(x, y, z);
			float f = _MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype);
			if (f >= 0.0F) {
				node = this.getNodeAndUpdateCostToMax(x, y, z, pathtype, f);
			}

			if (doesBlockHavePartialCollision(pathType) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
				node = null;
			}

			if (pathtype != PathType.WALKABLE && (!this.isAmphibious() || pathtype != PathType.WATER)) {
				if ((node == null || node.costMalus < 0.0F)
						&& verticalDeltaLimit > 0
						&& (pathtype != PathType.FENCE || this.canWalkOverFences())
						&& pathtype != PathType.UNPASSABLE_RAIL
						&& pathtype != PathType.TRAPDOOR
						&& pathtype != PathType.POWDER_SNOW) {
					node = this.tryJumpOn(x, y, z, verticalDeltaLimit, nodeFloorLevel, direction, pathType, blockpos$mutableblockpos);
				} else if (!this.isAmphibious() && pathtype == PathType.WATER && !this.canFloat()) {
					node = this.tryFindFirstNonWaterBelow(x, y, z, node);
				} else if (pathtype == PathType.OPEN) {
					node = this.tryFindFirstGroundNodeBelow(x, y, z);
				} else if (doesBlockHavePartialCollision(pathtype) && node == null) {
					node = this.getClosedNode(x, y, z, pathtype);
				}

				return node;
			} else {
				return node;
			}
		}
	}

    protected static boolean doesBlockHavePartialCollision(PathType pathType) {
        return pathType == PathType.FENCE || pathType == PathType.DOOR_WOOD_CLOSED || pathType == PathType.DOOR_IRON_CLOSED;
    }

	protected double getMobJumpHeight() {
		return Math.max(1.125, entity.maxUpStep());
	}

	@Nullable
	protected Node tryJumpOn(
			int x,
			int y,
			int z,
			int verticalDeltaLimit,
			double nodeFloorLevel,
			Direction direction,
			PathType pathType,
			BlockPos.MutableBlockPos pos
			) {
		Node node = this.findAcceptedNode(x, y + 1, z, verticalDeltaLimit - 1, nodeFloorLevel, direction, pathType);
		if (node == null) {
			return null;
		} else if (entity.getBbWidth() >= 1.0F) {
			return node;
		} else if (node.type != PathType.OPEN && node.type != PathType.WALKABLE) {
			return node;
		} else {
			double d0 = (double)(x - direction.getStepX()) + 0.5;
			double d1 = (double)(z - direction.getStepZ()) + 0.5;
			double radius = (double)entity.getBbWidth() / 2.0;
			AABB aabb = new AABB(
					d0 - radius,
					this.getFloorLevel(pos.set(d0, (double)(y + 1), d1)) + 0.001,
					d1 - radius,
					d0 + radius,
					(double)entity.getBbHeight() + this.getFloorLevel(pos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002,
					d1 + radius);
			return this.hasCollisions(aabb) ? null : node;
		}
	}

	@Nullable
	protected Node tryFindFirstNonWaterBelow(int x, int y, int z, @Nullable Node node) {
		y--;

		while (y > entity.level().getMinBuildHeight()) {
			PathType pathtype = this.getCachedPathType(x, y, z);
			if (pathtype != PathType.WATER) {
				return node;
			}

			node = this.getNodeAndUpdateCostToMax(x, y, z, pathtype, _MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype));
			y--;
		}

		return node;
	}

	protected Node tryFindFirstGroundNodeBelow(int x, int y, int z) {
		for (int i = y - 1; i >= entity.level().getMinBuildHeight(); i--) {
			if (y - i > entity.getMaxFallDistance()) {
				return this.getBlockedNode(x, i, z);
			}

			PathType pathtype = this.getCachedPathType(x, i, z);
			float f = _MobAIVanillaClassesHelper.getPathfindingMalus(entity, pathtype);
			if (pathtype != PathType.OPEN) {
				if (f >= 0.0F) {
					return this.getNodeAndUpdateCostToMax(x, i, z, pathtype, f);
				}

				return this.getBlockedNode(x, i, z);
			}
		}

		return this.getBlockedNode(x, y, z);
	}

	protected boolean hasCollisions(AABB boundingBox) {
		return this.collisionCache.computeIfAbsent(boundingBox, p_330163_ -> !this.currentContext.level().noCollision(entity, boundingBox));
	}

	@Override
	protected PathType getCachedPathType(int x, int y, int z) {
		return this.pathTypesByPosCacheByMob.computeIfAbsent(
				BlockPos.asLong(x, y, z),
				posHash -> this.getPathTypeOfMob(this.currentContext, x, y, z, entity));
	}

	@Override
	public Set<PathType> getPathTypeWithinMobBB(PathfindingContext context, int x, int y, int z) {
		EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);

		for (int i = 0; i < this.entityWidth; i++) {
			for (int j = 0; j < this.entityHeight; j++) {
				for (int k = 0; k < this.entityDepth; k++) {
					int l = i + x;
					int i1 = j + y;
					int j1 = k + z;
					PathType pathtype = this.getPathType(context, l, i1, j1);
					BlockPos blockpos = entity.blockPosition();
					boolean flag = this.canPassDoors();
					if (pathtype == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && flag) {
						pathtype = PathType.WALKABLE_DOOR;
					}

					if (pathtype == PathType.DOOR_OPEN && !flag) {
						pathtype = PathType.BLOCKED;
					}

					if (pathtype == PathType.RAIL
							&& this.getPathType(context, blockpos.getX(), blockpos.getY(), blockpos.getZ()) != PathType.RAIL
							&& this.getPathType(context, blockpos.getX(), blockpos.getY() - 1, blockpos.getZ()) != PathType.RAIL) {
						pathtype = PathType.UNPASSABLE_RAIL;
					}

					enumset.add(pathtype);
				}
			}
		}

		return enumset;
	}

}
