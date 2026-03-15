package com.github.standobyte.jojoimpl.powers.hamon.client.particle.custom;

import com.github.standobyte.jojo.client.particle.type.custom.EntityPosParticle;
import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HamonGliderChargingParticle extends EntityPosParticle {
	private final LivingEntity livingEntity;
	private final Vec3 randomOffset;
	private final InteractionHand hand;

	private HamonGliderChargingParticle(ClientLevel level, LivingEntity entity, InteractionHand hand) {
		super(level, entity, false);
		this.livingEntity = entity;
		this.hand = hand;
		this.randomOffset = new Vec3(
				(random.nextDouble() - 0.5), 
				random.nextDouble() * 0.5, 
				(random.nextDouble() - 0.5)).scale(0.5);
		setLifetime(3);
		initPos();
	}

//	@Override
//	public void tick() {
//		if (livingEntity == null || !livingEntity.isAlive()
//				|| livingEntity.getVehicle() == null
//				|| livingEntity.getVehicle().getType() != ModEntityTypes.LEAVES_GLIDER.get()) {
//			remove();
//			return;
//		}
//		super.tick();
//	}
//
//	@Override
//	protected int getLightColor(float partialTick) {
//		return 0xF000F0;
//	}

	@Override
	protected Vec3 getNextTickPos(Vec3 entityPos) {
		return livingEntity != null ? entityPos.add(randomOffset).add(new Vec3(
				livingEntity.getBbWidth() * 0.6 * (livingEntity.getMainArm() == (hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT) ? -1 : 1), 
				livingEntity.getBbHeight(), 
				0)
				.yRot(-livingEntity.yBodyRot * MathUtil.DEG_TO_RAD)) : null;
	}

//	public static EntityPosParticle createCustomParticle(ClientLevel level, LivingEntity entity, InteractionHand hand) {
//		EntityPosParticle particle = new HamonGliderChargingParticle(level, entity, hand);
//		particle.pickSprite(CustomParticlesHelper.getSavedSpriteSet(ModParticles.HAMON_SPARK.get()));
//		particle.setLifetime(5);
//		return particle;
//	}

}
