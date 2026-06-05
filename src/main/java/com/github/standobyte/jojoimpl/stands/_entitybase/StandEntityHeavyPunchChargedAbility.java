package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.config.RotpConfig;
import com.github.standobyte.jojo.customobjects.DamageSourceModified;
import com.github.standobyte.jojo.customobjects.explosion.CustomExplosion;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;
import com.github.standobyte.jojo.subsystems.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.subsystems.target.AimingEntity;
import com.github.standobyte.jojo.subsystems.target.HitResultUtil;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility.HeavyPunchExplosion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class StandEntityHeavyPunchChargedAbility extends StandEntityAbility {

	public StandEntityHeavyPunchChargedAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandEntityChargedHeavy::new);
		usageGroup = AbilityUsageGroup.COMBAT;
		setDefaultPhaseLength(ActionPhase.BUTTON_CHARGE, 21);
		setButtonHoldPhase(ActionPhase.WINDUP);
		setDefaultPhaseLength(ActionPhase.PERFORM, 7);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 12);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		return super.isAbilityAvailable(context) && StandUtil.getStandGrabTarget(context) == null;
	}


	@Override
	public void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		super.initActionFromConfig(action, level, powerUser, performer);
		if (!level.isClientSide() && performer instanceof StandEntity stand) {
			action.phasesLength.put(ActionPhase.BUTTON_CHARGE, StandStatFormulas.getChargedHeavyButtonWindup(
					stand.getAttackSpeed(), stand.getFinisherMeter()));
			action.phasesLength.put(ActionPhase.PERFORM, StandStatFormulas.getChargedHeavyPunchWindup(
					stand.getAttackSpeed(), stand.getFinisherMeter()));
		}
	}
	
	public static class StandEntityChargedHeavy extends EntityActionInstance {
		protected float buttonChargeRatio;

		public StandEntityChargedHeavy(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
		}
		
		@Override
		public void onButtonStopHold() {
			switch (getPhase()) {
				case BUTTON_CHARGE -> {
					phasesLength.put(ActionPhase.WINDUP, 0f);
					syncPhaseChanges();
				}
				case WINDUP -> {
					setPhaseStart(ActionPhase.PERFORM);
					syncPhaseChanges();
				}
				default -> {}
			}
		}
		
		@Override
		public void onSetPhase(ActionPhase newPhase) {
			if (getPhase() == ActionPhase.BUTTON_CHARGE && newPhase != ActionPhase.BUTTON_CHARGE) {
				this.buttonChargeRatio = getPhaseRatio();
			}
		}
		
		@Override
		public void actionPerformStart() {
			if (performer instanceof StandEntity stand) {
				setStandOffset(0, Math.max(stand.offsetFromUser.getRelativeOffset().z, 0) + 2,
						StandOffsetFromUser.Rotations.HEAD_XY,
						false);
				
				Level level = performer.level();
				if (level.isClientSide() && ClientGlobals.canHearStands) {
					ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_PUNCH_HEAVY_SWING.get(), stand), 
							stand.getSoundSource(), 1, 1, stand, stand.level()));

					ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_PUNCH_HEAVY_CRY.get(), stand), 
							stand.getSoundSource(), 1, 1, stand, stand.level()));
				}
			}
			aimAs = AimingEntity.STAND;
		}
		
		@Override
		public void actionPerformEnd() {
			Level level = level();
			if (performer instanceof StandEntity stand) {
				ActionTarget target = HitResultUtil.clipEntityLook(stand, entity -> StandEntityPunchAbility.canStandHit(stand, entity), 0);
				if (!level.isClientSide()) {
					StandPower standPower = StandPower.get(getPowerUser());
					
					if (StandEntityPunchAbility.playHitSound(target, level)) {
						StandUtil.broadcastSound((ServerLevel) level, target.getCenterPos(), 
								ModSoundEvents.STAND_PUNCH_HEAVY_CHARGED, true, standPower, 
								stand.getSoundSource(), 1, 1);
					}
					DamageSource dmgSource = makePunchDamageSource();
					float dmgAmount = StandStatFormulas.getChargedHeavyAttackDamage(stand.getAttackDamage());
					float explRadius = Math.min((float) stand.getAttackDamage() * 0.25f, 10);
					
					switch (target.getType()) {
						case ENTITY -> hitEntity(target, level, stand, dmgSource, dmgAmount, explRadius);
						case BLOCK -> hitBlock(target, level, stand, dmgSource, dmgAmount, explRadius);
						default -> {}
					}
					
					punchedTarget = target;
					standPower.consumeStamina(100);
					stand.consumeFinisherMeter(1.0001f);
				}
				if (target.getType() == TargetType.ENTITY) {
					standRotationTarget = target;
				}
				else {
					aimAs = AimingEntity.CAMERA_ENTITY;
				}
			}
		}
		
		protected void hitEntity(ActionTarget target, Level level, StandEntity stand, 
				DamageSource dmgSource, float dmgAmount, float explRadius) {
			Entity targetEntity = target.getMainEntity();
			if (targetEntity instanceof LivingEntity targetLiving) {
                addKnockback(dmgSource);
				standEntityAttack(stand, targetLiving, dmgSource, dmgAmount);
			}
		}
		
		protected void addKnockback(DamageSource dmgSource) {
			DamageSourceModified knockback = (DamageSourceModified) dmgSource;
			knockback.jojo_ripples$modifyKnockback(2.5f, 1);
		}
		
		protected void hitBlock(ActionTarget target, Level level, StandEntity stand, 
				DamageSource dmgSource, float dmgAmount, float explRadius) {
			BlockPos blockPos = target.getBlockPos();
			Direction face = target.getFace();
			Vec3 pos = Vec3.atCenterOf(blockPos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.6));
			DamageSource aoeDmgSource = dmgSource;
			float aoeDmg = dmgAmount * 0.5f;
			HeavyPunchExplosion explosion = new HeavyPunchExplosion(level, stand, 
					new ActionTarget(blockPos, face), stand.getLookAngle(), 
					aoeDmgSource, 
					pos.x, pos.y, pos.z, 
					explRadius, false, 
					RotpConfig.canStandBreakBlocks(StandUtil.getStandUser(stand)) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP)
					.aoeDamage(aoeDmg)
					.createBlockShards(stand.getAttackDamage(), stand.getPrecision());
			CustomExplosion.explode(explosion);
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return phase.ordinal() < ActionPhase.PERFORM.ordinal();
		}
		
	}

}
