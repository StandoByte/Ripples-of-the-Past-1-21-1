package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.network.s2c.StandEntitySoundPacket;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandEntityUnsummonAction extends SpecialEntityActionType {
	public static final int UNSUMMON_TICKS = 10;
	
	public static float alpha(float tick, float length) {
		float ratio = 1 - tick / (length - 2);
		return Mth.clamp(ratio, 0, 1);
	}

	public StandEntityUnsummonAction(ResourceLocation id) {
		super(null, id);
	}

	@Override
	public EntityActionInstance createActionObj() {
		return new StandUnsummonInstance(this);
	}

	public static class StandUnsummonInstance extends EntityActionInstance {
		protected boolean playedSound = false;

		public StandUnsummonInstance() {
			this(ModSpecialActions.STAND_UNSUMMON.get());
			this.setStartingPhase();
		}

		protected StandUnsummonInstance(EntityActionType ability) {
			super(ability);
			phasesLength.put(ActionPhase.PERFORM, UNSUMMON_TICKS);
			phasesLength.put(ActionPhase.RECOVERY, 999999); // this way the action stays on the client side and keeps setting stand alpha to 0
			standRegensStamina = true;
		}
		
		@Override
		public void actionPerformStart() {
		}
		
		@Override
		public void actionTick() {
			if (performer.level().isClientSide()) {
				float alpha = switch (phase) {
					default -> 1;
					case PERFORM -> {
						float unsummonLength = getAnimPhaseLength();
						yield unsummonLength > 2 ? alpha(getAnimPhaseTick(0), unsummonLength) : 1;
					}
					case RECOVERY -> 0;
				};
				if (alpha < 1) {
					((StandEntity) performer).multiplyTranslucency(alpha);
				}
			}
		}

		protected static final double OFFSET_REDUCE_PER_TICK = 0.1;
		protected static final double MIN_OFFSET = 0.2;
		@Override
		protected void _incPhaseTick() {
			if (performer instanceof StandEntity standEntity && canTickUnsummon(standEntity)) {
				if (!performer.level().isClientSide() && !playedSound) {
					LivingEntity user = getPowerUser();
					if (user != null) {
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new StandEntitySoundPacket(standEntity, ModSoundEvents.STAND_UNSUMMON, 1, 1));
						playedSound = true;
					}
				}
				
				Vec3 offsetVec = standEntity.offsetFromUser.relativeOffset;
				if (offsetVec != null) {
					double offsetDist = offsetVec.lengthSqr();
					if (offsetDist > MIN_OFFSET * MIN_OFFSET) {
						offsetDist = Math.sqrt(offsetDist);
						standEntity.offsetFromUser.relativeOffset = offsetVec.scale(
								Math.max((offsetDist - OFFSET_REDUCE_PER_TICK), MIN_OFFSET) / offsetDist);
					}
				}
				super._incPhaseTick();
			}
		}
		
		public static boolean canTickUnsummon(StandEntity standEntity) {
			return standEntity.isCloseToUser() || standEntity.isFollowingUser();
		}

		@Override
		public void actionPerformEnd() {
			LivingEntity user = getPowerUser();
			if (user != null && !user.level().isClientSide()) {
				StandPower userPower = StandPower.get(getPowerUser());
				if (userPower != null && userPower.hasPower()) {
					userPower.getPowerType().forceUnsummon(user, userPower);
				}
			}
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		
	}
	
}
