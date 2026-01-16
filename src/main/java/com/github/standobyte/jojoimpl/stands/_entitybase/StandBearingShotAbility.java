package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.mc.entity.projectile.ThrownNuggetBearingEntity;
import com.github.standobyte.jojo.mechanics.entity_like_player.useitem.StandCallbackWhenShooting;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

public class StandBearingShotAbility extends StandEntityAbility {

	public StandBearingShotAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandBearingShot::new);
		usageGroup = AbilityUsageGroup.UTILITY;
		isSubAbility = true;
		setButtonHoldPhase(ActionPhase.PERFORM);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 10);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		if (super.isAbilityAvailable(context)) {
			StandEntity standEntity = StandUtil.getSummonedStand(context);
			if (standEntity != null) {
				ItemStack rItem = standEntity.getMainHandItem();
				return rItem.is(Tags.Items.NUGGETS);
			}
		}
		return false;
	}
	
	@Override
	public AbilityInputState cl_abilityInputState(Power<?> context) {
		AbilityInputState state = super.cl_abilityInputState(context);
		state.setFlag(AbilityInputState.WITH_ITEM_HELD, true);
		return state;
	}
	
	
	public static class StandBearingShot extends EntityActionInstance {

		public StandBearingShot(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				setPhaseStart(ActionPhase.RECOVERY);
				syncPhaseChanges();
				
				StandEntity stand = performer instanceof StandEntity s ? s : null;
				setStandOffset(new Vec3(0, 0, 1.5), StandOffsetFromUser.Rotations.HEAD_XY, true);
				if (stand != null) {
					stand.updatePosition(stand.getUser());
					stand.offsetFromUser.syncToTracking();
				}
				
				Level level = level();
				if (level instanceof ServerLevel serverLevel) {
					ItemStack itemStack = performer.getMainHandItem();
					level.playSound(null, performer.getX(), performer.getY(), performer.getZ(),
							ModSoundEvents.BEARING_SHOT.get(), SoundSource.NEUTRAL, 0.5F, 1.0f);
					StandCallbackWhenShooting.shootProjectileWithStandStats(ThrownNuggetBearingEntity::new, 
							serverLevel, itemStack, performer, stand, 0, ThrownNuggetBearingEntity.shotVelocity(itemStack), 1.0f);
					
					itemStack.consume(1, performer);
					StandPower standPower = StandPower.get(getPowerUser());
					standPower.consumeStamina(10);
				}
			}
		}

		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return this.phase != ActionPhase.RECOVERY;
		}
		
	}
}
