package com.github.standobyte.jojo.jojoimpl.stands.starplatinum;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.jojoimpl.JojoDefinitions;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class StarInhaleAbility extends StandEntityAbility {

    public StarInhaleAbility(AbilityType<?> abilityType, AbilityId abilityId) {
    	super(abilityType, abilityId, InhaleAbilityInstance::new);
    	setDefaultPhaseLength(ActionPhase.WINDUP, 2);
    	setDefaultPhaseLength(ActionPhase.PERFORM, 120);
    	setDefaultPhaseLength(ActionPhase.RECOVERY, 20);
    }

    public static class InhaleAbilityInstance extends EntityActionInstance {
        public InhaleAbilityInstance(EntityActionType ability) {
            super(ability);
        }

        private static final double RANGE = 12.0;

        @Override
        public void onActionSet(@Nullable EntityActionInstance prevAction) {
            super.onActionSet(prevAction);
            setStandOffset(0, 1.5, StandOffsetFromUser.Rotations.HEAD_XY, true);
        }

        @Override
        public void actionTick() {
            if (getPhase() != ActionPhase.PERFORM) {
                return;
            }

            Level level = level();
            if (!(performer instanceof StandEntity standEntity)) {
                return;
            }
            LivingEntity user = getPowerUser();

            Vec3 mouthPos = standEntity.position()
                    .add(0, standEntity.getBbHeight() * 0.9F, 0)
                    .add(new Vec3(0, standEntity.getBbHeight() / 16F, standEntity.getBbWidth() * 0.5F)
                            .xRot(-standEntity.getXRot() * MathUtil.DEG_TO_RAD)
                            .yRot(-standEntity.getYRot() * MathUtil.DEG_TO_RAD));

            Vec3 spLookVec = standEntity.getLookAngle();
            level.getEntities(standEntity, standEntity.getBoundingBox().inflate(RANGE, RANGE, RANGE),
                    entity -> spLookVec.dot(entity.position().subtract(standEntity.position()).normalize()) > 0.886
                            && standEntity.hasLineOfSight(entity)
                            && entity.distanceToSqr(standEntity) > 0.5
                            && !entity.is(user)
            ).forEach(entity -> {
                double distance = entity.distanceTo(standEntity);

                double efficiency = 1.0;
                Vec3 suctionVec = mouthPos.subtract(entity.getBoundingBox().getCenter())
                        .normalize().scale(0.5 * efficiency);

                entity.setDeltaMovement(distance > 2 ?
                        entity.getDeltaMovement().add(suctionVec.scale(1 / distance))
                        : suctionVec.scale(Math.max(distance - 1, 0)));

                if (!level.isClientSide() && distance < 4 && entity instanceof LivingEntity livingEntity) {
                	suffocateTick(livingEntity, standEntity, 0.025f);
                }
            });

            if (level.isClientSide()) {
                for (int i = 0; i < MathUtil.fractionRandomInc(2.5); i++) {
                    spawnAirStreamParticle(level, mouthPos, spLookVec);
                }
            }
        }

        private void spawnAirStreamParticle(Level level, Vec3 mouthPos, Vec3 lookVec) {
            Vec3 particlePos = mouthPos.add(lookVec.scale(RANGE)
                    .xRot((float) ((Math.random() * 2 - 1) * Math.PI / 6))
                    .yRot((float) ((Math.random() * 2 - 1) * Math.PI / 6)));
            Vec3 vecToStand = mouthPos.subtract(particlePos).normalize().scale(0.75);
            level.addParticle(ModParticles.AIR_STREAM.get(), particlePos.x, particlePos.y, particlePos.z, vecToStand.x, vecToStand.y, vecToStand.z);
        }

        @Override
        public void onButtonStopHold() {
            setPhaseStart(ActionPhase.RECOVERY);
            syncPhaseChanges();
        }

        @Override
        public void onSetPhase(ActionPhase newPhase) {
            super.onSetPhase(newPhase);
            Level level = level();
            if (!level.isClientSide()) {
                return;
            }

            if (newPhase == ActionPhase.PERFORM) {
                if (performer instanceof StandEntity stand) {
                    EntityStoppableSoundInstance soundInstance = new EntityStoppableSoundInstance(
                            ModSoundEvents.STAR_PLATINUM_INHALE.get(),
                            stand.getSoundSource(),
                            1.0F,
                            2.0F,
                            stand,
                            this.id,
                            () -> getPhase() != ActionPhase.PERFORM
                    );
                    ClientsideSoundsHelper.playNonVanillaClassSound(soundInstance);
                }
            }
        }
        
        @Override
    	public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
    		return true;
    	}
    }

    public static void suffocateTick(LivingEntity target, @Nullable Entity damageSourceEntity, float airReductionSpeed) {
    	if (target.canBreatheUnderwater() || target instanceof Player player && JojoDefinitions.isUndeadOrVampiric(player)
    			|| JojoDefinitions.isDyingBody(target) || target instanceof IronGolem) return;

    	if (target.getAirSupply() > 0) {
    		// TODO suffocation interaction with hamon
//    		PlayerPower power = PlayerPower.get(target);
//    		if (power != null && power.getPowerType() == ModPlayerPowers.HAMON) {
//    			Optional<HamonData> hamonOptional = Optional.empty();
//    			if (hamonOptional.isPresent()) {
//    				HamonData hamon = hamonOptional.get();
//    				speed /= 1 + hamonOptional.get().getBreathingLevel() * 0.04F;
//    				hamon.suffocateTick(speed);
//    			}
//    		}
    	

    		int airReduction = MathUtil.fractionRandomInc((double) target.getMaxAirSupply() * Mth.clamp(airReductionSpeed, 0.0, 1.0)) + 4;
    		target.setAirSupply(Math.max(target.getAirSupply() - airReduction, -18));
    	}
    	else {
            var damageType = DamageUtil.type(target.level(), ModDamageTypes.SUFFOCATION);
            DamageSource dmgSource = new DamageSource(damageType, damageSourceEntity);
            target.hurt(dmgSource, 1F);
    	}
    }

}