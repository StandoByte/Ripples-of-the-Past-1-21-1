package com.github.standobyte.jojo.powersystem.standpower.entity;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.s2c.TrSyncStandOffsetPacket;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.subsystems.entity_grab.LivingComponentGrab;
import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandOffsetFromUser {
	private StandEntity standEntity;
	
	public Vec3 idleOffset;
	public Rotations idleRotations;
	
	@Nullable public Vec3 grabIdleOffset;
	
	private Vec3 relativeOffset;
	private Rotations rotations;
	@Nullable public EntityActionType standAbility;
	
	private Vec3 prevAbsoluteOffset;
	private Rotations prevRotations;
	private float prevBodyRotDiff;
	private int changedTimestamp;
	
	public static StandOffsetFromUser createDefault(StandEntity standEntity) {
		StandOffsetFromUser offset = new StandOffsetFromUser(standEntity, new Vec3(0.75, standEntity.Y_OFFSET, -0.75), Rotations.BODY);
		offset.grabOffset(new Vec3(-1, standEntity.Y_OFFSET, 1.5));
		return offset;
	}
	
	public StandOffsetFromUser(StandEntity standEntity, Vec3 idleOffset, Rotations idleRotations) {
		this.standEntity = standEntity;
		this.idleOffset = idleOffset;
		this.idleRotations = idleRotations;
		setOffset(idleOffset, idleRotations);
	}
	
	public StandOffsetFromUser grabOffset(Vec3 offset) {
		this.grabIdleOffset = offset;
		return this;
	}
	
	public void setOffset(Vec3 offset, Rotations rotations) {
		if (this.relativeOffset == null || this.rotations == null || 
				offset.x != this.relativeOffset.x || offset.y != this.relativeOffset.y || offset.z != this.relativeOffset.z || 
				rotations != this.rotations) {
			LivingEntity userEntity = standEntity.getUser();
			if (userEntity != null) {
				this.prevAbsoluteOffset = getAbsoluteOffset(userEntity, false);
				this.prevBodyRotDiff = userEntity.yBodyRot - userEntity.getYRot();
			}
			else {
				this.prevAbsoluteOffset = null;
			}
			this.prevRotations = this.rotations != null ? this.rotations : rotations;
			
			this.relativeOffset = offset;
			this.rotations = rotations;
			this.changedTimestamp = standEntity.tickCount;
		}
	}
	
	public void syncToTracking() {
		if (!standEntity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(standEntity, new TrSyncStandOffsetPacket(standEntity.getId(), relativeOffset, rotations));
		}
	}
	
	public void resetToIdle() {
		setOffset(idleOffset, idleRotations);
		this.standAbility = null;
	}
	
	public boolean isIdle() {
		return rotations == idleRotations && relativeOffset.equals(idleOffset);
	}
	
	public Vec3 getPosition(LivingEntity userEntity) {
		Vec3 offset = getAbsoluteOffset(userEntity, standEntity.level().isClientSide());
		if (userEntity.isBaby()) {
			offset = offset.scale(userEntity.getAgeScale());
		}
		return AlignBy.EYE_POS.align(userEntity, standEntity, offset);
	}
	
	public Vec3 getAbsoluteOffset(LivingEntity userEntity, boolean lerp) {
		if (grabIdleOffset != null && LivingComponentGrab.getEntityGrabbedBy(standEntity) != null) {
			// FIXME (grab & throw) lerp the grab idle offset
			return relativeToAbsolute(grabIdleOffset, Rotations.HEAD, userEntity);
		}
		
		if (lerp && prevAbsoluteOffset == null) {
			prevAbsoluteOffset = getAbsoluteOffset(userEntity, false);
		}
		Vec3 absoluteOffset = relativeToAbsolute(relativeOffset, rotations, userEntity);
		
		if (lerp) {
			double lerpAmount = getLerpAmount();
			absoluteOffset = new Vec3(
					Mth.lerp(lerpAmount, prevAbsoluteOffset.x, absoluteOffset.x),
					Mth.lerp(lerpAmount, prevAbsoluteOffset.y, absoluteOffset.y),
					Mth.lerp(lerpAmount, prevAbsoluteOffset.z, absoluteOffset.z));
		}
		
		return absoluteOffset;
	}
	
	public static Vec3 relativeToAbsolute(Vec3 relativeVec, Rotations rotations, LivingEntity origin) {
		Vec3 absoluteOffset = relativeVec;
		if (rotations == Rotations.HEAD_XY) {
			absoluteOffset = relativeVec.xRot(-origin.getXRot() * MathUtil.DEG_TO_RAD);
		}
		float userYRot = rotations == Rotations.BODY ? origin.yBodyRot : origin.getYRot();
		absoluteOffset = absoluteOffset.yRot(-userYRot * MathUtil.DEG_TO_RAD);
		return absoluteOffset;
	}
	
	public void copyRotation(LivingEntity userEntity, boolean lerp) {
		standEntity.setYRot(userEntity.getYRot());
		standEntity.setXRot(userEntity.getXRot());
		standEntity.yRotO = userEntity.yRotO;
		standEntity.yHeadRot = userEntity.yHeadRot;
		standEntity.yHeadRotO = userEntity.yHeadRotO;
		
		// this shit so ass
		boolean isBodyRot = rotations == Rotations.BODY;
		float bodyRotAmount = isBodyRot ? 1 : 0;
		if (lerp) {
			boolean wasBodyRot = prevRotations == Rotations.BODY;
			if (isBodyRot != wasBodyRot) {
				float lerpAmount = getLerpAmount();
				bodyRotAmount = isBodyRot ? lerpAmount : (1 - lerpAmount);
			}
		}
		
		if (bodyRotAmount == 1) {
			standEntity.yBodyRot = userEntity.yBodyRot;
			standEntity.yBodyRotO = userEntity.yBodyRotO;
		}
		else if (bodyRotAmount == 0) {
			standEntity.yBodyRot = userEntity.getYRot();
			standEntity.yBodyRotO = userEntity.yRotO;
		}
		else {
			standEntity.yBodyRot = Mth.lerp(bodyRotAmount, userEntity.getYRot(), userEntity.yBodyRot);
			standEntity.yBodyRotO = standEntity.yBodyRot + prevBodyRotDiff / (isBodyRot ? -LERP_TIME : LERP_TIME);
		}
	}
	
//	public float getLerpAmount() {
//		return getLerpAmount(0);
//	}
	
	public float getLerpAmount(/*int tickOffset*/) {
		int timeDiff = (standEntity.tickCount/* + tickOffset*/) - changedTimestamp;
		return Mth.clamp((float) timeDiff / LERP_TIME, 0, 1);
	}
	
	public static final int LERP_TIME = 4;
	
	
	public enum Rotations {
		HEAD,
		BODY,
		HEAD_XY
	}
	
	public enum AlignBy {
		CENTER,
		BOTTOM,
		EYE_POS;
		
		public Vec3 align(Entity entity1, Entity entity2, Vec3 offset) {
			return switch (this) {
				case CENTER -> {
					Vec3 userCenter = entity1.getBoundingBox().getCenter();
					Vec3 standCenter = userCenter.add(offset);
					Vec3 pos = standCenter.subtract(0, entity2.getBoundingBox().getYsize() / 2, 0);
					yield pos;
				}
				case BOTTOM -> {
					Vec3 userPos = entity1.position();
					Vec3 standPos = userPos.add(offset);
					yield standPos;
				}
				case EYE_POS -> {
					Vec3 userEyePos = entity1.getEyePosition();
					Vec3 standEyePos = userEyePos.add(offset);
					Vec3 pos = standEyePos.subtract(0, entity2.getEyeHeight(), 0);
					yield pos;
				}
			};
		}
	}
	
	
	public Vec3 getRelativeOffset() {
		return relativeOffset;
	}
}
