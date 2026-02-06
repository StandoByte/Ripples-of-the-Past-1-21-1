package com.github.standobyte.jojo.mechanics.standhelditems.moveset;

import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class TossStandItemAbility extends Ability {

	public TossStandItemAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		usageGroup = AbilityUsageGroup.UTILITY;
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		if (super.isAbilityAvailable(context)) {
			StandEntity standEntity = StandUtil.getSummonedStand(context);
			if (standEntity != null && standEntity.isManuallyControlled()) {
				ItemStack lItem = standEntity.getOffhandItem();
				ItemStack rItem = standEntity.getMainHandItem();
				return !lItem.isEmpty() || !rItem.isEmpty();
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
	
	@Override
	public void writeExtraInput(FriendlyByteBuf serverboundBuf, LivingEntity user, boolean isClientPlayer) {
		if (isClientPlayer) {
			boolean ctrl = InputHandler.getInstance().getCurModifier() == KeyModifier.CONTROL;
			serverboundBuf.writeBoolean(ctrl);
		}
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandEntity standEntity = StandUtil.getSummonedStand(user);
			if (standEntity != null) {
				boolean ctrl = extraClientInput.readBoolean();
				if (!standEntity.getMainHandItem().isEmpty()) {
					standEntity.tossItem(InteractionHand.MAIN_HAND, !ctrl);
				}
				else {
					standEntity.tossItem(InteractionHand.OFF_HAND, !ctrl);
				}
			}
		}
	}
}
