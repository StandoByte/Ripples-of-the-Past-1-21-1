package com.github.standobyte.jojo.client.particle.type.custom;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class EntityOffsetParticle extends EntityPosParticle {
	protected Vec3 offset;
	@Nullable private final LivingEntity entityLiving;

	protected EntityOffsetParticle(ClientLevel level, Entity entity, 
			double x, double y, double z) {
		super(level, entity, false);
		setPos(x, y, z);
		this.entityLiving = entity instanceof LivingEntity living ? living : null;
		this.offset = new Vec3(x, y, z).subtract(entity.position())
				.yRot(getYRot() * MathUtil.DEG_TO_RAD);
		xo = x;
		yo = y;
		zo = z; 
	}

	private float getYRot() {
		return entityLiving != null ? entityLiving.yBodyRot : entity.getYRot();
	}

	@Override
	protected Vec3 getNextTickPos(Vec3 entityPos) {
		tickSpeed();
		return entity != null ? entityPos.add(offset
				.yRot(-getYRot() * MathUtil.DEG_TO_RAD)) : null;
	}

	protected void tickSpeed() {
		yd -= 0.04 * gravity;
		offset = offset.add(xd, yd, zd);
		xd *= 0.98;
		yd *= 0.98;
		zd *= 0.98;
		if (onGround) {
			xd *= 0.7;
			zd *= 0.7;
		}
	}

}
