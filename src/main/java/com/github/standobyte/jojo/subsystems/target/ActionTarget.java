package com.github.standobyte.jojo.subsystems.target;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods._Vec3;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.entity.PartEntity;

public class ActionTarget {
	private final TargetType type;
	private final BlockPos blockPos;
	private final Direction face;
	private Entity entity;
	private final int entityId;
	private Optional<Vec3> clipPos = Optional.empty();

	public static final ActionTarget EMPTY = new ActionTarget();

	private ActionTarget() {
		type = TargetType.EMPTY;
		this.blockPos = null;
		this.face = null;
		this.entity = null;
		this.entityId = -1;
	}

	public ActionTarget(@Nonnull BlockPos blockPos, @Nonnull Direction face) {
		type = TargetType.BLOCK;
		this.blockPos = blockPos;
		this.face = face;
		this.entity = null;
		this.entityId = -1;
	}

	public ActionTarget(@Nonnull Entity entity) {
		if (entity != null) {
			type = TargetType.ENTITY;
			this.blockPos = null;
			this.face = null;
			this.entity = entity;
			this.entityId = entity.getId();
		}
		else {
			type = TargetType.EMPTY;
			this.blockPos = null;
			this.face = null;
			this.entity = null;
			this.entityId = -1;
		}
	}
	
	public ActionTarget withClipPos(Optional<Vec3> clipPos) {
		if (this != EMPTY) {
			this.clipPos = clipPos;
		}
		return this;
	}

	public ActionTarget(int entityId, Level level) {
		this(level.getEntity(entityId));
	}

	public static ActionTarget fromVanilla(HitResult result) {
		switch (result.getType()) {
		case BLOCK:
			BlockHitResult blockResult = (BlockHitResult) result;
			return new ActionTarget(blockResult.getBlockPos(), blockResult.getDirection());
		case ENTITY:
			return new ActionTarget(((EntityHitResult) result).getEntity());
		default:
			return ActionTarget.EMPTY;
		}
	}

	public TargetType getType() {
		return type;
	}
	
	public boolean isEmpty(Level level) {
		return switch (type) {
			case BLOCK -> {
				BlockState blockState = level.getBlockState(blockPos);
				yield blockState.isEmpty();
			}
			case ENTITY -> {
				if (entity == null) {
					resolveEntityId(level);
				}
				yield entity == null || entity.isRemoved();
			}
			default -> true;
		};
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public Direction getFace() {
		return face;
	}

	public Entity getEntity() {
		return entity;
	}
	
	public Entity getMainEntity() {
		return entity instanceof PartEntity dragonPart ? dragonPart.getParent() : entity;
	}
	
	public Optional<Vec3> getClipPos() {
		return clipPos;
	}

	public Optional<AABB> getBoundingBox(Level level) {
		AABB aabb = null;
		switch (type) {
		case ENTITY:
			aabb = getEntity().getBoundingBox();
			break;
		case BLOCK:
			BlockState blockState = level.getBlockState(blockPos);
			VoxelShape blockShape = blockState.getShape(level, blockPos);
			if (!blockShape.isEmpty()) {
				aabb = blockShape.bounds().move(blockPos);
			}
			break;
		default:
			break;
		}
		return Optional.ofNullable(aabb);
	}


	public ActionTarget copy() {
		switch (type) {
		case EMPTY:
			return ActionTarget.EMPTY;
		case BLOCK:
			return new ActionTarget(blockPos, face).withClipPos(clipPos);
		case ENTITY:
			return new ActionTarget(entityId).withClipPos(clipPos);
		default:
			return null;
		}
	}

	public static final StreamCodec<? super FriendlyByteBuf, ActionTarget> STREAM_CODEC_UNRESOLVED_ENTITY_ID = new StreamCodec<>() {
		
		@Override public ActionTarget decode(FriendlyByteBuf buffer) {
			TargetType type = buffer.readEnum(TargetType.class);
			Optional<Vec3> clipPos = NetworkUtil.readOptional(buffer, _Vec3.STREAM_CODEC);
			return switch (type) {
				case ENTITY -> new ActionTarget(buffer.readInt()).withClipPos(clipPos);
				case BLOCK -> new ActionTarget(buffer.readBlockPos(), buffer.readEnum(Direction.class)).withClipPos(clipPos);
				default -> ActionTarget.EMPTY;
			};
		}
		
		@Override public void encode(FriendlyByteBuf buffer, ActionTarget value) {
			TargetType type = value.getType();
			buffer.writeEnum(type);
			NetworkUtil.writeOptional(value.clipPos, buffer, _Vec3.STREAM_CODEC);
			switch (type) {
				case ENTITY -> {
					buffer.writeInt(value.entityId);
				}
				case BLOCK -> {
					buffer.writeBlockPos(value.getBlockPos());
					buffer.writeEnum(value.getFace());
				}
				default -> {}
			}
		}
	};

	public static ActionTarget decode(FriendlyByteBuf buf, Level level) {
		ActionTarget target = STREAM_CODEC_UNRESOLVED_ENTITY_ID.decode(buf);
		return target.resolveEntityId(level);
	}

	/**
	 * Caches a reference to the target entity if it's found in the level.
	 * @return Empty target if the entity was not found in the level, otherwise returns this.
	 */
	public ActionTarget resolveEntityId(Level level) {
		if (this.getType() == TargetType.ENTITY && this.entity == null) {
			this.entity = level.getEntity(this.entityId);
			return this.entity != null ? this : ActionTarget.EMPTY;
		}
		return this;
	}

	private ActionTarget(int entityIdOnly) {
		type = TargetType.ENTITY;
		this.blockPos = null;
		this.face = null;
		this.entity = null;
		this.entityId = entityIdOnly;
	}

	@Override
	public boolean equals(Object object) {
		return this == object || object instanceof ActionTarget && this.sameTarget((ActionTarget) object);
	}
	
	@Override
	public int hashCode() {
		return this == EMPTY ? 0 : Objects.hash(type, blockPos, face, entity, entityId, clipPos);
	}

	public boolean sameTarget(ActionTarget target) {
		if (target != null && this.type == target.type) {
			switch (type) {
			case BLOCK:
				return this.blockPos.equals(target.blockPos);
			case ENTITY:
				int idThis = this.entity != null ? this.entity.getId() : this.entityId;
				int idThat = target.entity != null ? target.entity.getId() : target.entityId;
				return idThis == idThat;
			default:
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String str = "ActionTarget";
		switch (type) {
		case EMPTY:
			str += " (EMPTY)";
			break;
		case ENTITY:
			str += " (ENTITY - ";
			str += entity != null ? entity.getName().getString() : "null (uh-oh)";
			str += ")";
			break;
		case BLOCK:
			str += " (BLOCK - ";
			str += blockPos != null ? "{" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + "}" : "null (uh-oh)";
			str += " / ";
			str += face != null ? face.getName() : "null";
			str += ")";
			break;
		}
		return str;
	}

	public static enum TargetType {
		EMPTY,
		BLOCK,
		ENTITY
	}
	
	
	public Vec3 getCenterPos() {
		return switch (getType()) {
			case ENTITY -> {
				Entity targetEntity = getEntity();
				yield new Vec3(targetEntity.getX(), targetEntity.getY(0.5), targetEntity.getZ());
			}
			case BLOCK -> {
				yield Vec3.atCenterOf(getBlockPos());
			}
			default -> null;
		};
	}
	
}
