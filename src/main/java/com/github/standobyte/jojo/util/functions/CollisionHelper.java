package com.github.standobyte.jojo.util.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.util.objects_java.ReuseableStream;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionHelper {

	public static BlockCollisionResult collideBoundingBox(Vec3 pVec, AABB pCollisionBox, LevelReader pLevel, CollisionContext pSelectionContext) {
		BlockCollisionResult collision = new BlockCollisionResult();
		double x = pVec.x;
		double y = pVec.y;
		double z = pVec.z;
		collision.movementX = x;
		collision.movementY = y;
		collision.movementZ = z;
		if (y != 0) {
			y = collide(Direction.Axis.Y, pCollisionBox, pLevel, y, pSelectionContext, collision.blocks);
			if (y != 0) {
				pCollisionBox = pCollisionBox.move(0, y, 0);
			}
		}

		boolean zAxisFirst = Math.abs(x) < Math.abs(z);
		if (zAxisFirst && z != 0) {
			z = collide(Direction.Axis.Z, pCollisionBox, pLevel, z, pSelectionContext, collision.blocks);
			//if (z != 0) {
			//	pCollisionBox = pCollisionBox.move(0, 0, z);
			//}
		}

		if (x != 0) {
			x = collide(Direction.Axis.X, pCollisionBox, pLevel, x, pSelectionContext, collision.blocks);
			//if (!flag && x != 0) {
			//	pCollisionBox = pCollisionBox.move(x, 0, 0);
			//}
		}

		if (!zAxisFirst && z != 0) {
			z = collide(Direction.Axis.Z, pCollisionBox, pLevel, z, pSelectionContext, collision.blocks);
		}

		collision.x = x;
		collision.y = y;
		collision.z = z;
		return collision;
	}

	protected static double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, 
			LevelReader pLevelReader, double pDesiredOffset, CollisionContext pSelectionContext, Collection<Pair<BlockPos, VoxelShape>> blockPosCollide) {
		return collide(pCollisionBox, pLevelReader, pDesiredOffset, pSelectionContext, AxisCycle.between(pMovementAxis, Direction.Axis.Z), blockPosCollide);
	}

	public static double collide(AABB pCollisionBox, LevelReader pLevelReader, 
			double pDesiredOffset, CollisionContext pSelectionContext, AxisCycle pRotationAxis, Collection<Pair<BlockPos, VoxelShape>> blockPosCollide) {
		if (!(pCollisionBox.getXsize() < 1.0E-6D) && !(pCollisionBox.getYsize() < 1.0E-6D) && !(pCollisionBox.getZsize() < 1.0E-6D)) {
			if (Math.abs(pDesiredOffset) < 1.0E-7D) {
				return 0.0D;
			} else {
				AxisCycle axisrotation = pRotationAxis.inverse();
				Direction.Axis direction$axis = axisrotation.cycle(Direction.Axis.X);
				Direction.Axis direction$axis1 = axisrotation.cycle(Direction.Axis.Y);
				Direction.Axis direction$axis2 = axisrotation.cycle(Direction.Axis.Z);
				BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
				int i = Mth.floor(pCollisionBox.min(direction$axis) - 1.0E-7D) - 1;
				int j = Mth.floor(pCollisionBox.max(direction$axis) + 1.0E-7D) + 1;
				int k = Mth.floor(pCollisionBox.min(direction$axis1) - 1.0E-7D) - 1;
				int l = Mth.floor(pCollisionBox.max(direction$axis1) + 1.0E-7D) + 1;
				double d0 = pCollisionBox.min(direction$axis2) - 1.0E-7D;
				double d1 = pCollisionBox.max(direction$axis2) + 1.0E-7D;
				boolean flag = pDesiredOffset > 0.0D;
				int i1 = flag ? Mth.floor(pCollisionBox.max(direction$axis2) - 1.0E-7D) - 1 : Mth.floor(pCollisionBox.min(direction$axis2) + 1.0E-7D) + 1;
				int j1 = lastC(pDesiredOffset, d0, d1);
				int k1 = flag ? 1 : -1;
				int l1 = i1;

				boolean collision = false;
				double minOffsetCollided = pDesiredOffset;
				double desiredOffset_abs = Math.abs(pDesiredOffset);
				double minOffset_abs = desiredOffset_abs;

				Collection<Pair<BlockPos, VoxelShape>> collidedWith = new ArrayList<>();
				while(true) {
					if (flag) {
						if (l1 > j1) {
							break;
						}
					} else if (l1 < j1) {
						break;
					}

					for(int i2 = i; i2 <= j; ++i2) {
						for(int j2 = k; j2 <= l; ++j2) {
							int k2 = 0;
							if (i2 == i || i2 == j) {
								++k2;
							}

							if (j2 == k || j2 == l) {
								++k2;
							}

							if (l1 == i1 || l1 == j1) {
								++k2;
							}

							if (k2 < 3) {
								blockpos.set(axisrotation, i2, j2, l1);
								BlockState blockstate = pLevelReader.getBlockState(blockpos);
								if ((k2 != 1 || blockstate.hasLargeCollisionShape()) && (k2 != 2 || blockstate.is(Blocks.MOVING_PISTON))) {
									VoxelShape collisionShape = blockstate.getCollisionShape(pLevelReader, blockpos, pSelectionContext);
									double offsetCollided = collisionShape.collide(direction$axis2, pCollisionBox.move(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ()), pDesiredOffset);
									double offset_abs = Math.abs(offsetCollided);
									if (offset_abs < 1.0E-7D) {
										offsetCollided = 0;
										offset_abs = 0;
									}
									if (!collision && offset_abs < desiredOffset_abs) {
										collision = true;
									}

									if (offset_abs <= minOffset_abs) {
										if (offset_abs < minOffset_abs) {
											collidedWith.clear();
											minOffsetCollided = offsetCollided;
											minOffset_abs = offset_abs;
										}
										else if (collision) {
											collidedWith.add(Pair.of(blockpos.immutable(), collisionShape));
										}
									}
									j1 = lastC(minOffsetCollided, d0, d1);
								}
							}
						}
					}

					l1 += k1;
				}

				if (!collidedWith.isEmpty()) {
					blockPosCollide.addAll(collidedWith);
				}
				return minOffsetCollided;
			}
		} else {
			return pDesiredOffset;
		}
	}

	protected static int lastC(double pDesiredOffset, double pMin, double pMax) {
		return pDesiredOffset > 0.0D ? Mth.floor(pMax + pDesiredOffset) + 1 : Mth.floor(pMin + pDesiredOffset) - 1;
	}

	public static class BlockCollisionResult {
		public Collection<Pair<BlockPos, VoxelShape>> blocks = new ArrayList<>();
		public double movementX;
		public double movementY;
		public double movementZ;
		public double x;
		public double y;
		public double z;
	}



	public static Collection<BlockPos> getBlocksOutlineTowards(AABB collisionBox, Vec3 vec, LevelReader world, boolean sort) {
		List<BlockPos> blocks = new ArrayList<>();
		double vecLengthSqr = vec.lengthSqr();
		Vec3 center1 = collisionBox.getCenter();

		if (vecLengthSqr == 0) {
			int x1 = Mth.floor(collisionBox.minX);
			int y1 = Mth.floor(collisionBox.minY);
			int z1 = Mth.floor(collisionBox.minZ);
			int x2 = Mth.ceil(collisionBox.maxX);
			int y2 = Mth.ceil(collisionBox.maxY);
			int z2 = Mth.ceil(collisionBox.maxZ);
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = z1; z <= z2; z++) {
						blocks.add(new BlockPos(x, y, z));
					}
				}
			}
		}
		else {
			BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
			AABB box2 = collisionBox.move(vec);
			int x1 = Mth.floor(Math.min(collisionBox.minX, box2.minX));
			int y1 = Mth.floor(Math.min(collisionBox.minY, box2.minY));
			int z1 = Mth.floor(Math.min(collisionBox.minZ, box2.minZ));
			int x2 = Mth.ceil(Math.max(collisionBox.maxX, box2.maxX));
			int y2 = Mth.ceil(Math.max(collisionBox.maxY, box2.maxY));
			int z2 = Mth.ceil(Math.max(collisionBox.maxZ, box2.maxZ));
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = z1; z <= z2; z++) {
						blockPos.set(x, y, z);
						if (!world.isEmptyBlock(blockPos)) {
							Vec3 blockRelPos = Vec3.atCenterOf(blockPos).subtract(center1);
							double projScale = vec.dot(blockRelPos) / vecLengthSqr;
							Vec3 projOnAxisVec = vec.scale(projScale);
							if (collisionBox.move(projOnAxisVec).intersects(
									blockPos.getX(),     blockPos.getY(),     blockPos.getZ(), 
									blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1)) {
								blocks.add(blockPos.immutable());
							}
						}
					}
				}
			}
		}

		if (sort) {
			return blocks.stream()
					.sorted(Comparator.comparingDouble(blockPos -> blockPos.distToCenterSqr(center1.x, center1.y, center1.z)))
					.collect(Collectors.toList());
		}
		return blocks;
	}



	public static Stream<Pair<Entity, VoxelShape>> getEntityCollisions(Level level, @Nullable Entity pEntity, AABB pArea, Predicate<Entity> pFilter) {
		if (pArea.getSize() < 1.0E-7D) {
			return Stream.empty();
		} else {
			AABB AABB = pArea.inflate(1.0E-7D);
			return level.getEntities(pEntity, AABB, pFilter.and(target -> {
				return target.isPickable();
			})).stream().map(entity -> Pair.of(entity, Shapes.create(entity.getBoundingBox())));
		}
	}

	public static void collideEntities(AABB aabb, Vec3 movementVec, Level level, 
			ReuseableStream<VoxelShape> worldBorderCollision, ReuseableStream<Pair<Entity, VoxelShape>> potentialEntityCollisions, 
			CollisionContext selectionContext, Collection<Entity> destination) {
		double x = movementVec.x;
		double y = movementVec.y;
		double z = movementVec.z;

		if (y != 0) {
			y = collideEntitiesAxis(Direction.Axis.Y, aabb, level, y, 
					worldBorderCollision, potentialEntityCollisions, 
					selectionContext, destination);
			if (y != 0) {
				aabb = aabb.move(0, y, 0);
			}
		}

		boolean zFirst = Math.abs(x) < Math.abs(z);
		if (zFirst && z != 0) {
			z = collideEntitiesAxis(Direction.Axis.Z, aabb, level, z, 
					worldBorderCollision, potentialEntityCollisions, 
					selectionContext, destination);
			//if (z != 0) {
			//	aabb = aabb.move(0, 0, z);
			//}
		}

		if (x != 0) {
			x = collideEntitiesAxis(Direction.Axis.X, aabb, level, x, 
					worldBorderCollision, potentialEntityCollisions, 
					selectionContext, destination);
			//if (!zFirst && x != 0) {
			//	aabb = aabb.move(x, 0, 0);
			//}
		}

		if (!zFirst && z != 0) {
			z = collideEntitiesAxis(Direction.Axis.Z, aabb, level, z, 
					worldBorderCollision, potentialEntityCollisions, 
					selectionContext, destination);
		}
	}

	protected static double collideEntitiesAxis(Direction.Axis movementAxis, AABB collisionBox, Level level, double desiredOffset, 
			ReuseableStream<VoxelShape> worldBorderCollision, ReuseableStream<Pair<Entity, VoxelShape>> potentialEntityCollisions, 
			CollisionContext pSelectionContext, Collection<Entity> destination) {
		if (!(collisionBox.getXsize() < 1.0E-6D) && !(collisionBox.getYsize() < 1.0E-6D) && !(collisionBox.getZsize() < 1.0E-6D)) {
			if (Math.abs(desiredOffset) < 1.0E-7D) {
				return 0;
			} else {
				AxisCycle pRotationAxis = AxisCycle.between(movementAxis, Direction.Axis.Z);
				AxisCycle axisrotation = pRotationAxis.inverse();
				Direction.Axis direction$axis2 = axisrotation.cycle(Direction.Axis.Z);

				MutableDouble worldBorderCollideOffset = new MutableDouble(desiredOffset);
				worldBorderCollision.getStream().forEach(voxelShape -> {
					worldBorderCollideOffset.setValue(voxelShape.collide(direction$axis2, collisionBox, worldBorderCollideOffset.doubleValue()));
				});
				desiredOffset = worldBorderCollideOffset.doubleValue();

				double maxOffset = desiredOffset;
				MutableDouble collidedOffset = new MutableDouble(maxOffset);
				potentialEntityCollisions.getStream().forEach(entityVoxelShape -> {
					double entityCollideResult = entityVoxelShape.getRight().collide(direction$axis2, collisionBox, collidedOffset.doubleValue());
					if (entityCollideResult != maxOffset) {
						destination.add(entityVoxelShape.getLeft());
						collidedOffset.setValue(entityCollideResult);
					}
				});

				return desiredOffset;
			}
		} else {
			return desiredOffset;
		}
	}
}
