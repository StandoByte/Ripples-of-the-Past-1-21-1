package com.github.standobyte.jojo.adventure.npc.ai.player_crutch;

import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoveControl2 extends MoveControl {
	protected _MobAIVanillaClassesHelper playerAi;
	protected LivingEntity entity;

	public MoveControl2(_MobAIVanillaClassesHelper playerAi, LivingEntity entity) {
		super(entity instanceof Mob mob ? mob : null);
		this.playerAi = playerAi;
		this.entity = entity;
	}

	@Override
	public void tick() {
		if (this.operation == MoveControl.Operation.STRAFE) {
			float entitySpeed = (float)entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
			float speed = (float)this.speedModifier * entitySpeed;
			float forward = this.strafeForwards;
			float right = this.strafeRight;
			float length = Mth.sqrt(forward * forward + right * right);
			if (length < 1.0F) {
				length = 1.0F;
			}

			length = speed / length;
			forward *= length;
			right *= length;
			float f5 = Mth.sin(entity.getYRot() * MathUtil.DEG_TO_RAD);
			float f6 = Mth.cos(entity.getYRot() * MathUtil.DEG_TO_RAD);
			float f7 = forward * f6 - right * f5;
			float f8 = right * f6 + forward * f5;
			if (!this.isWalkable(f7, f8)) {
				this.strafeForwards = 1.0F;
				this.strafeRight = 0.0F;
			}

			entity.setSpeed(speed);
			_MobAIVanillaClassesHelper.setZza(entity, this.strafeForwards);
			_MobAIVanillaClassesHelper.setXxa(entity, this.strafeRight);
			this.operation = MoveControl.Operation.WAIT;
		} else if (this.operation == MoveControl.Operation.MOVE_TO) {
			this.operation = MoveControl.Operation.WAIT;
			double d0 = this.wantedX - entity.getX();
			double d1 = this.wantedZ - entity.getZ();
			double d2 = this.wantedY - entity.getY();
			double d3 = d0 * d0 + d2 * d2 + d1 * d1;
			if (d3 < 2.5E-7F) {
				_MobAIVanillaClassesHelper.setZza(entity, 0.0F);
				return;
			}

			float f9 = (float)(Mth.atan2(d1, d0) * 180.0F / (float)Math.PI) - 90.0F;
			entity.setYRot(this.rotlerp(entity.getYRot(), f9, 90.0F));
			entity.setSpeed((float)(this.speedModifier * entity.getAttributeValue(Attributes.MOVEMENT_SPEED)));
			BlockPos blockpos = entity.blockPosition();
			BlockState blockstate = entity.level().getBlockState(blockpos);
			VoxelShape voxelshape = blockstate.getCollisionShape(entity.level(), blockpos);
			if (d2 > (double)entity.maxUpStep() && d0 * d0 + d1 * d1 < (double)Math.max(1.0F, entity.getBbWidth())
					|| !voxelshape.isEmpty()
					&& entity.getY() < voxelshape.max(Direction.Axis.Y) + (double)blockpos.getY()
					&& !blockstate.is(BlockTags.DOORS)
					&& !blockstate.is(BlockTags.FENCES)) {
				playerAi.jumpControl.jump();
				this.operation = MoveControl.Operation.JUMPING;
			}
		} else if (this.operation == MoveControl.Operation.JUMPING) {
			entity.setSpeed((float)(this.speedModifier * entity.getAttributeValue(Attributes.MOVEMENT_SPEED)));
			if (entity.onGround()) {
				this.operation = MoveControl.Operation.WAIT;
			}
		} else {
			_MobAIVanillaClassesHelper.setZza(entity, 0.0F);
		}
	}

	protected boolean isWalkable(float relativeX, float relativeZ) {
		PathNavigation pathNavigation = playerAi.navigation;
		if (pathNavigation != null) {
			WalkNodeEvaluator2 nodeEvaluator = (WalkNodeEvaluator2) pathNavigation.getNodeEvaluator();
			if (nodeEvaluator != null && nodeEvaluator.getPathType(entity, BlockPos.containing(
					entity.getX() + relativeX, entity.getBlockY(), entity.getZ() + relativeZ))
					!= PathType.WALKABLE) {
				return false;
			}
		}

		return true;
	}

}
