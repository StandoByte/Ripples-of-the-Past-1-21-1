package com.github.standobyte.jojoimpl.stands._entitybase;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.customobjects.DamageSourceModified;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil.StandStat;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.subsystems.ServerBlockDestroyTracker;
import com.github.standobyte.jojo.subsystems.entity_grab.LivingComponentGrab;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;
import com.github.standobyte.jojo.subsystems.target.AimingEntity;
import com.github.standobyte.jojo.util.functions.JojoModUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StandEntityBarrageAbility extends StandEntityAbility {

	public StandEntityBarrageAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandEntityBarrage::new);
		usageGroup = AbilityUsageGroup.COMBAT;
		setDefaultPhaseLength(ActionPhase.PERFORM, StandStatFormulas.getBarrageMaxDuration(8));
		setDefaultPhaseLength(ActionPhase.RECOVERY, 10);
		noFinisherBarDecay = true;
	}
	
	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		double standAttackSpeed = StandUtil.getPhysicalStatValue((StandPower) context, StandStat.ATTACK_SPEED);
		float hits = StandStatFormulas.getBarrageHitsPerSecond(standAttackSpeed);
		if (hits <= 0) {
			return ConditionCheck.createNegative("stand_too_slow");
		}
		return super.checkSpecificConditions(context);
	}
	
	@Override
	public Ability replaceWithSubAbility(Power<?> context, AvailableAbilities abilities) {
		StandPower standPower = PowerClass.STAND.cast(context);
		if (standPower != null) {
			StandEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				if (LivingComponentGrab.getEntityGrabbedBy(standEntity) != null) {
					return abilities.getContextVariation("grab_barrage");
				}
			}
		}
		
		return super.replaceWithSubAbility(context, abilities);
	}
	
	
	@Override
	public void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		super.initActionFromConfig(action, level, powerUser, performer);
		if (!level.isClientSide() && performer instanceof StandEntity stand) {
//			if (powerUser instanceof Player player && player.getAbilities().instabuild) {
//				action.phasesLength.put(ActionPhase.PERFORM, 999999);
//				action.phasesLength.put(ActionPhase.RECOVERY, 0);
//			}
//			else {
				action.phasesLength.put(ActionPhase.PERFORM, StandStatFormulas.getBarrageMaxDuration(stand.getDurability()));
//			}
		}
	}
	
	public static class StandEntityBarrage extends EntityActionInstance {
		public int hitsThisTick;
		@Nullable protected Vec3 hitSoundPos;

		public StandEntityBarrage(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(0, 1.5, StandOffsetFromUser.Rotations.HEAD_XY, true);
			aimAs = AimingEntity.STAND;
			Level level = performer.level();
			if (performer instanceof StandEntity stand) {
				if (level.isClientSide()) {
					if (!stand.isArmsOnlyMode()) {
						ClientsideSoundsHelper.playNonVanillaClassSound(new EntityStoppableSoundInstance(ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_BARRAGE_CRY.get(), stand), 
								stand.getSoundSource(), 1, 1, stand, level.random.nextLong(), 
								() -> this.isOver() || this.phase != ActionPhase.PERFORM));
					}
				}
			}
		}
		
		@Override
		public void onSetPhase(ActionPhase newPhase) {
			userWalkSpeed = newPhase == ActionPhase.PERFORM ? 0.6f : 1;
		}

		@Override
		public void actionTick() {
			Level level = performer.level();
			
			if (isGrabVariation() && !level.isClientSide() && getPhase() == ActionPhase.PERFORM) {
				LivingEntity grabbedEntity = LivingComponentGrab.getEntityGrabbedBy(performer);
				if (grabbedEntity == null || !grabbedEntity.isAlive()) {
					setPhaseStart(ActionPhase.RECOVERY);
					syncPhaseChanges();
					return;
				}
			}
			
			if (getPhase() == ActionPhase.PERFORM && performer instanceof StandEntity stand) {
				hitsThisTick = (int) getHitsPerTick(stand);
				
				StandPower standPower = StandPower.get(getPowerUser());
				if (level.isClientSide()) {
					if (ClientGlobals.canHearStands) {
						level.playLocalSound(stand.getX(), stand.getEyeY(), stand.getZ(), ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_BARRAGE_SWING.get(), stand), 
								stand.getSoundSource(), 1, 1, false);
					}
				}
				else {
					ActionTarget target = getPunchTarget(stand);

					if (StandEntityPunchAbility.playHitSound(target, level)) {
						hitSoundPos = target.getCenterPos();
					}
					if (curPhaseTick % 3 == 0 && hitSoundPos != null) {
						StandUtil.broadcastSound((ServerLevel) level, hitSoundPos, 
								ModSoundEvents.STAND_PUNCH_BARRAGE, true, standPower, 
								stand.getSoundSource(), 
								0.75F, 
								1.8F - (float) stand.getAttackDamage() * 0.05F + stand.getRandom().nextFloat() * 0.2F);
						hitSoundPos = null;
					}
					
					switch (target.getType()) {
						case ENTITY -> hitEntity(target, level, stand);
						case BLOCK -> hitBlock(target, level, stand);
						default -> {}
					}
					punchedTarget = target;
				}
				if (standPower != null) {
					standPower.consumeStamina(4, true);
				}
			}
		}
		
		@Override
		public void onButtonStopHold() {
			startRecovery();
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return cancellingAbility != this.ability;
		}
		
		protected float getHitsPerTick(StandEntity stand) {
			float hitsPerSec = StandStatFormulas.getBarrageHitsPerSecond(stand.getAttackSpeed());
			float hitsPerTick = hitsPerSec / 20;
			int curTick = (curPhaseTick - 1) % 20 + 1; // 1~20
			float value = (hitsPerTick * curTick) - (int) (hitsPerTick * (curTick - 1));
			if (isGrabVariation()) {
				value *= 0.5f; // the left arm is busy, duh
			}
			return value;
		}
		
		protected ActionTarget getPunchTarget(StandEntity stand) {
			if (isGrabVariation()) {
				return new ActionTarget(LivingComponentGrab.getEntityGrabbedBy(stand));
			}
			return StandEntityPunchAbility.aimAtPunchTarget(stand);
		}
		
		protected void hitEntity(ActionTarget target, Level level, StandEntity stand) {
			Entity targetEntity = target.getMainEntity();
			if (targetEntity != null) {
				DamageSource dmgSource = makePunchDamageSource();
				((DamageSourceModified) dmgSource).jojo_ripples$modifyKnockback(0, 0.1f);
				float dmgAmount = StandStatFormulas.getBarrageHitDamage(stand.getAttackDamage()) * hitsThisTick;
				standEntityAttack(stand, targetEntity, dmgSource, dmgAmount);
				
				stand.addFinisherMeter(0.005f * hitsThisTick);
			}
		}
		
		protected void hitBlock(ActionTarget target, Level level, StandEntity stand) {
			BlockPos blockPos = target.getBlockPos();
			BlockState blockState = level.getBlockState(blockPos);
			if (JojoModUtil.canEntityDestroy(level, blockPos, blockState, stand)) {
				double standStrength = stand.getAttackDamage();
				double standSpeed = stand.getAttackSpeed();
				
				float blockHardnessForStand = StandStatFormulas.getBlockHardness(standStrength, blockState, level, blockPos);
				if (blockHardnessForStand >= 0) {
					float standEfficiency = StandStatFormulas.getBarrageBlockMiningEfficiency(standStrength, standSpeed);
					float destroyProgress = standEfficiency / (blockHardnessForStand * 100);
					
					boolean breakBlock = blockHardnessForStand == 0 || ServerBlockDestroyTracker.addBlockDestroyProgress((ServerLevel) level, stand, 
							blockPos, blockState, destroyProgress).progressNew >= 1;
							if (breakBlock) {
								boolean dropBlock = !isUserCreative();
								level.destroyBlock(blockPos, dropBlock, stand);
							}
				}
				
				if (blockHardnessForStand != 0) {
					if (curPhaseTick % 2 == 0) {
						SoundType blockSounds = blockState.getSoundType(level, blockPos, stand);
						level.playSound(null, blockPos, blockSounds.getHitSound(), SoundSource.BLOCKS, 
								(blockSounds.getVolume() + 1.0F) / 8.0F, blockSounds.getPitch() * 0.5F);
					}
				}
			}
		}
		
	}

}
