package com.github.standobyte.jojo.adventure.npc.ai.player_crutch;

import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation2 extends GroundPathNavigation {
	protected _MobAIVanillaClassesHelper playerAi;
	protected LivingEntity entity;
    protected PathFinder2 pathFinder;

	public GroundPathNavigation2(_MobAIVanillaClassesHelper playerAi, LivingEntity entity) {
		super(entity instanceof Mob mob ? mob : null, entity.level());
		this.playerAi = playerAi;
		this.entity = entity;

		WalkNodeEvaluator2 nodeEvaluator = new WalkNodeEvaluator2();
		this.nodeEvaluator = nodeEvaluator;
		this.nodeEvaluator.setCanPassDoors(true);
		int maxVisitedNodes = Mth.floor(mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0);
		this.pathFinder = new PathFinder2(nodeEvaluator, maxVisitedNodes);
	}

	@Nullable
	@Override
	protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy) {
		return this.createPath(targets, regionOffset, offsetUpward, accuracy, (float)entity.getAttributeValue(Attributes.FOLLOW_RANGE));
	}

	@Nullable
	@Override
	protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange) {
		if (targets.isEmpty()) {
			return null;
		} else if (entity.getY() < (double)this.level.getMinBuildHeight()) {
			return null;
		} else if (!this.canUpdatePath()) {
			return null;
		} else if (this.path != null && !this.path.isDone() && targets.contains(this.targetPos)) {
			return this.path;
		} else {
			this.level.getProfiler().push("pathfind");
			BlockPos blockpos = offsetUpward ? entity.blockPosition().above() : entity.blockPosition();
			int i = (int)(followRange + (float)regionOffset);
			PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.level, blockpos.offset(-i, -i, -i), blockpos.offset(i, i, i));
			PathFinder2 pathFinder = this.pathFinder;
			Path path = pathFinder.findPath(pathnavigationregion, entity, targets, followRange, accuracy, this.maxVisitedNodesMultiplier);
			this.level.getProfiler().pop();
			if (path != null && path.getTarget() != null) {
				this.targetPos = path.getTarget();
				this.reachRange = accuracy;
				this.resetStuckTimeout();
			}

			return path;
		}
	}

	@Override
	public void tick() {
		this.tick++;
		if (this.hasDelayedRecomputation) {
			this.recomputePath();
		}

		if (!this.isDone()) {
			if (this.canUpdatePath()) {
				this.followThePath();
			} else if (this.path != null && !this.path.isDone()) {
				Vec3 vec3 = this.getTempMobPos();
				Vec3 vec31 = this.path.getNextEntityPos(entity);
				if (vec3.y > vec31.y && !entity.onGround() && Mth.floor(vec3.x) == Mth.floor(vec31.x) && Mth.floor(vec3.z) == Mth.floor(vec31.z)) {
					this.path.advance();
				}
			}

			if (!this.isDone()) {
				Vec3 vec32 = this.path.getNextEntityPos(entity);
				MoveControl moveControl = playerAi.moveControl;
				moveControl.setWantedPosition(vec32.x, this.getGroundY(vec32), vec32.z, this.speedModifier);
			}
		}
	}

	@Override
	protected void followThePath() {
		Vec3 vec3 = this.getTempMobPos();
		this.maxDistanceToWaypoint = entity.getBbWidth() > 0.75F ? entity.getBbWidth() / 2.0F : 0.75F - entity.getBbWidth() / 2.0F;
		Vec3i vec3i = this.path.getNextNodePos();
		double d0 = Math.abs(entity.getX() - ((double)vec3i.getX() + ((int)(entity.getBbWidth() + 1)) / 2D)); //Forge: Fix MC-94054. The casting to int must match what is done in Path#getEntityPosAtNode
		double d1 = Math.abs(entity.getY() - (double)vec3i.getY());
		double d2 = Math.abs(entity.getZ() - ((double)vec3i.getZ() + ((int)(entity.getBbWidth() + 1)) / 2D)); //Forge: Fix MC-94054. The casting to int must match what is done in Path#getEntityPosAtNode
		boolean flag = d0 <= (double)this.maxDistanceToWaypoint && d2 <= (double)this.maxDistanceToWaypoint && d1 < 1.0D; //Forge: Fix MC-94054
		if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
			this.path.advance();
		}

		this.doStuckDetection(vec3);
	}

	protected boolean shouldTargetNextNodeInDirection(Vec3 vec) {
		if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
			return false;
		} else {
			Vec3 vec3 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
			if (!vec.closerThan(vec3, 2.0)) {
				return false;
			} else if (this.canMoveDirectly(vec, this.path.getNextEntityPos(entity))) {
				return true;
			} else {
				Vec3 vec31 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
				Vec3 vec32 = vec3.subtract(vec);
				Vec3 vec33 = vec31.subtract(vec);
				double d0 = vec32.lengthSqr();
				double d1 = vec33.lengthSqr();
				boolean flag = d1 < d0;
				boolean flag1 = d0 < 0.5;
				if (!flag && !flag1) {
					return false;
				} else {
					Vec3 vec34 = vec32.normalize();
					Vec3 vec35 = vec33.normalize();
					return vec35.dot(vec34) < 0.0;
				}
			}
		}
	}

	@Override
	protected void doStuckDetection(Vec3 positionVec3) {
		if (this.tick - this.lastStuckCheck > 100) {
			float f = entity.getSpeed() >= 1.0F ? entity.getSpeed() : entity.getSpeed() * entity.getSpeed();
			float f1 = f * 100.0F * 0.25F;
			if (positionVec3.distanceToSqr(this.lastStuckCheckPos) < (double)(f1 * f1)) {
				this.isStuck = true;
				this.stop();
			} else {
				this.isStuck = false;
			}

			this.lastStuckCheck = this.tick;
			this.lastStuckCheckPos = positionVec3;
		}

		if (this.path != null && !this.path.isDone()) {
			Vec3i vec3i = this.path.getNextNodePos();
			long i = this.level.getGameTime();
			if (vec3i.equals(this.timeoutCachedNode)) {
				this.timeoutTimer = this.timeoutTimer + (i - this.lastTimeoutCheck);
			} else {
				this.timeoutCachedNode = vec3i;
				double d0 = positionVec3.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
				this.timeoutLimit = entity.getSpeed() > 0.0F ? d0 / (double)entity.getSpeed() * 20.0 : 0.0;
			}

			if (this.timeoutLimit > 0.0 && (double)this.timeoutTimer > this.timeoutLimit * 3.0) {
				this.timeoutPath();
			}

			this.lastTimeoutCheck = i;
		}
	}

	@Override
	public boolean shouldRecomputePath(BlockPos pos) {
		if (this.hasDelayedRecomputation) {
			return false;
		} else if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
			Node node = this.path.getEndNode();
			Vec3 vec3 = new Vec3(((double)node.x + entity.getX()) / 2.0, ((double)node.y + entity.getY()) / 2.0, ((double)node.z + entity.getZ()) / 2.0);
			return pos.closerToCenterThan(vec3, (double)(this.path.getNodeCount() - this.path.getNextNodeIndex()));
		} else {
			return false;
		}
	}



	@Override
	protected PathFinder createPathFinder(int maxVisitedNodes) {
		this.nodeEvaluator = new WalkNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
	}

	@Override
	protected boolean canUpdatePath() {
		return entity.onGround() || entity.isInLiquid() || entity.isPassenger();
	}

	@Override
	protected Vec3 getTempMobPos() {
		return new Vec3(entity.getX(), (double)this.getSurfaceY(), entity.getZ());
	}

	protected int getSurfaceY() {
		if (entity.isInWater() && this.canFloat()) {
			int i = entity.getBlockY();
			BlockState blockstate = this.level.getBlockState(BlockPos.containing(entity.getX(), (double)i, entity.getZ()));
			int j = 0;

			while (blockstate.is(Blocks.WATER)) {
				blockstate = this.level.getBlockState(BlockPos.containing(entity.getX(), (double)(++i), entity.getZ()));
				if (++j > 16) {
					return entity.getBlockY();
				}
			}

			return i;
		} else {
			return Mth.floor(entity.getY() + 0.5);
		}
	}

	@Override
	protected void trimPath() {
		super.trimPath();
		if (this.avoidSun) {
			if (this.level.canSeeSky(BlockPos.containing(entity.getX(), entity.getY() + 0.5, entity.getZ()))) {
				return;
			}

			for (int i = 0; i < this.path.getNodeCount(); i++) {
				Node node = this.path.getNode(i);
				if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
					this.path.truncateNodes(i);
					return;
				}
			}
		}
	}

}
