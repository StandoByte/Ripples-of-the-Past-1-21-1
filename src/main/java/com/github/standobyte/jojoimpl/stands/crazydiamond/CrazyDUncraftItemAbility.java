package com.github.standobyte.jojoimpl.stands.crazydiamond;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.mc.ContainerSlotInput;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CrazyDUncraftItemAbility extends StandEntityAbility {

	public CrazyDUncraftItemAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, ItemUncraft::new);
		usageGroup = AbilityUsageGroup.INVENTORY;
		setButtonHoldPhase(ActionPhase.PERFORM);
	}

	@Override
	public AbilityInputState cl_abilityInputState(Power<?> context) {
		AbilityInputState state = AbilityInputState.init();
		state.setFlag(AbilityInputState.ONLY_IN_CONTAINER, true);
		return state;
	}
	
	@Override
	public void writeExtraInput(FriendlyByteBuf serverboundBuf, LivingEntity user, boolean isClientPlayer) {
		if (isClientPlayer) {
			ContainerSlotInput hoveredItem = ContainerSlotInput.cl_HoveredSlot();
			NetworkUtil.writeOptionally(hoveredItem, serverboundBuf, ContainerSlotInput.STREAM_CODEC);
		}
	}


	// TODO (item repair) CD heal particles on the model of the item being repaired
	// TODO (item repair) sounds
	public static class ItemUncraft extends EntityActionInstance {
		private Optional<ContainerSlotInput> inputInvSlot = Optional.empty();
		private ItemStack repairedStack = ItemStack.EMPTY;

		public ItemUncraft(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void extraClientInput(FriendlyByteBuf input) {
			inputInvSlot = NetworkUtil.readOptional(input, ContainerSlotInput.STREAM_CODEC);
		}

		// TODO (item repair) does not work in CreativeModeInventoryScreen
		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			if (inputInvSlot != null && inputInvSlot.isPresent() && powerUser.getEntity(level()) instanceof Player player) {
				repairedStack = ContainerSlotInput.getItem(inputInvSlot.get(), player);
			}
		}

		@Override
		public void actionTick() {
//			Level level = level();
//			if (!level.isClientSide()) {
//			}
		}

		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				setPhaseStart(ActionPhase.RECOVERY);
				syncPhaseChanges();
			}
		}

		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}

	}

}
