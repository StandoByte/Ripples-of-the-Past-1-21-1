package com.github.standobyte.jojoimpl.stands.hierophant;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.text.StandSkinComponent;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class HierophantPuppetAbility extends StandEntityAbility {

	public HierophantPuppetAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, PuppetingAction::new);
	}
	
	
	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime, BufferingState bufferingState) {
		if (!level.isClientSide()) {
			StandPower power = PowerClass.STAND.get(user);
			if (power != null) {
				// Toggle back from the mob control instantly, without a Stand action
				List<HierophantPuppetEffect> effects = power.userStandEffects.getEffectsOfType(ModStandAbilities.EFFECT_HG_PUPPET.get()).toList();
				if (!effects.isEmpty()) {
					effects.forEach(StandEffectInstance::remove);
					return null;
				}
			}
		}
		
		return super.onKeyPress(level, user, extraClientInput, inputMethod, clickHoldResolveTime, bufferingState);
	}
	
	
	// The action is only created when we click an entity we want Hierophant to puppet
	public static class PuppetingAction extends EntityActionInstance {
		public LivingEntity targetEntity;

		public PuppetingAction(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			keepStandAimedAtTarget();
			if (standRotationTarget != null && standRotationTarget.getEntity() instanceof LivingEntity targetLiving) {
				this.targetEntity = targetLiving;
			}
		}
		
		@Override
		public void actionPerformEnd() {
			Level level = level();
			if (!level.isClientSide()) {
				LivingEntity standUser = getPowerUser();
				StandPower power = StandPower.get(standUser);
				if (power != null) {
					power.userStandEffects.getEffectsOfType(ModStandAbilities.EFFECT_HG_PUPPET.get())
						.forEach(StandEffectInstance::remove);
					
					if (targetEntity != null) {
						HierophantPuppetEffect newEffect = ModStandAbilities.EFFECT_HG_PUPPET.get().create(level);
						power.userStandEffects.addEffect(newEffect.withTarget(targetEntity));
					}
				}
			}
		}

	}

	
	public static boolean hasPuppetUnderControl(StandPower userPower) {
		return userPower.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_HG_PUPPET.get()).isPresent();
	}

	protected String releaseSpriteName;
	protected String releaseTlKey;
	
	@Override
	protected void initVariationAssets() {
		this.releaseSpriteName = this.spriteName + "_release";
		this.releaseTlKey = tlKey(abilityId, ".release");
	}
	
	@Override
	public String getSpriteName(Power<?> context) {
		if (hasPuppetUnderControl(PowerClass.STAND.cast(context))) {
			return releaseSpriteName;
		}
		return super.getSpriteName(context);
	}

	@Override
	public Component getName(Power<?> context) {
		if (hasPuppetUnderControl(PowerClass.STAND.cast(context))) {
			return StandSkinComponent.translatable(context, releaseTlKey);
		}
		return super.getName(context);
	}

}
