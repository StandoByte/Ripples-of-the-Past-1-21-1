package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.core.packet.fromserver.StandEntitySoundPacket;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

// XXX (gradual unsummon) remove the hud as soon as you press the button
// XXX (gradual unsummon) move the stand inside the user
public class StandEntityUnsummonAction extends SpecialEntityActionType {

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
			phasesLength.put(ActionPhase.PERFORM, 2f); // set to 5 when the todos are done
		}
		
		@Override
		public void actionPerformStart() {
		}
		
		@Override
		public void actionTick() {
			if (performer.level().isClientSide()) {
				float unsummonLength = getAnimPhaseLength();
				if (unsummonLength > 2) {
					float ratio = 1 - getAnimPhaseTick(0) / (unsummonLength - 2);
					((StandEntity) performer).multiplyTranslucency(Mth.clamp(ratio, 0, 1));
				}
			}
		}
		
		@Override
		protected void _incPhaseTick() {
			if (performer instanceof StandEntity standEntity && (standEntity.isCloseToUser() || standEntity.isFollowingUser())) {
				if (!performer.level().isClientSide() && !playedSound) {
					LivingEntity user = getPowerUser();
					if (user != null) {
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new StandEntitySoundPacket(standEntity, ModSoundEvents.STAND_UNSUMMON, 1, 1));
						playedSound = true;
					}
				}
				super._incPhaseTick();
			}
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
