package com.github.standobyte.jojo.powersystem.standpower.entity;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.util.functions.DamageUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class StandLinkDamageSource extends DamageSource {
	public final Entity standEntity;
	public final DamageSource actualSource;

	public StandLinkDamageSource(Level level, Entity standEntity, DamageSource actualSource) {
		super(DamageUtil.type(level, ModDamageTypes.STAND_HEALTH_LINK));
		this.standEntity = standEntity;
		this.actualSource = actualSource;
	}


	@Override
	public String toString() {
		return "DamageSource (" + this.type().msgId() + " (" + actualSource.getMsgId() + "))";
	}

	@Nullable
	public Entity getDirectEntity() {
		return actualSource.getDirectEntity();
	}

	@Nullable
	public Entity getEntity() {
		return actualSource.getEntity();
	}

	@Override
	public Component getLocalizedDeathMessage(LivingEntity dead) {
		return actualSource.getLocalizedDeathMessage(dead);
	}

	@Override
	public String getMsgId() {
		return actualSource.getMsgId();
	}

	@Override
	@Nullable
	public Vec3 getSourcePosition() {
		return null;
	}

	@Override
	public float getFoodExhaustion() {
		return actualSource.getFoodExhaustion();
	}

}
