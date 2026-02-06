package com.github.standobyte.jojoimpl.stands._helditems.abilities;

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

public class SwapStandHandItemsAbility extends Ability {

	public SwapStandHandItemsAbility(AbilityType<?> abilityType, AbilityId abilityId) {
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
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandEntity standEntity = StandUtil.getSummonedStand(user);
			if (standEntity != null) {
				ItemStack lItem = standEntity.getOffhandItem();
				ItemStack rItem = standEntity.getMainHandItem();
				standEntity.setItemInHand(InteractionHand.OFF_HAND, rItem);
				standEntity.setItemInHand(InteractionHand.MAIN_HAND, lItem);
			}
		}
	}
}
