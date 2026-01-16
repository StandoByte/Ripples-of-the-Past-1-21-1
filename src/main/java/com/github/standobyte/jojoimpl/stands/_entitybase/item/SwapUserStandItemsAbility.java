package com.github.standobyte.jojoimpl.stands._entitybase.item;

import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.ui.powerhud.PowerHud;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.mc.ContainerSlotInput;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SwapUserStandItemsAbility extends Ability {

	public SwapUserStandItemsAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		usageGroup = AbilityUsageGroup.UTILITY;
	}
	
	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		StandEntity stand = StandUtil.getSummonedStand(context);
		LivingEntity user = context.getUser();
		if (stand == null || user == null) return ConditionCheck.NEGATIVE;
		
		if (!stand.isFollowingUser() && MathUtil.getAABBDistance(stand.getBoundingBox(), user.getBoundingBox()) > 4.5) {
			return ConditionCheck.createNegative("stand_user_too_far");
		}
		
		return super.checkSpecificConditions(context);
	}

	@Override
	public AbilityInputState cl_abilityInputState(Power<?> context) {
		AbilityInputState state = super.cl_abilityInputState(context);
		// FIXME fix Ctrl+F on an item in the inventory
//		if (PowerHud.isInContainerScreen()) { // make it work in a container screen too
//			state.setFlag(AbilityInputState.IS_ACTIVE, true);
//			state.setFlag(AbilityInputState.ONLY_IN_CONTAINER, true);
//		}
		return state;
	}

	@Override
	public void writeExtraInput(FriendlyByteBuf serverboundBuf, LivingEntity user, boolean isClientPlayer) {
		if (isClientPlayer) {
			ContainerSlotInput hoveredItem = ContainerSlotInput.cl_HoveredSlot();
			NetworkUtil.writeOptionally(hoveredItem, serverboundBuf, ContainerSlotInput.STREAM_CODEC);
		}
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandEntity stand = StandUtil.getSummonedStand(user);
			if (stand != null) {
				var slotInInventory = NetworkUtil.readOptional(extraClientInput, ContainerSlotInput.STREAM_CODEC);
				if (slotInInventory.isPresent()) {
//					// the ability was used on a slot in the player inventory
//					if (user instanceof Player player) {
//						ContainerSlotInput slotData = slotInInventory.get();
//						ItemStack inventoryItem = ContainerSlotInput.getItem(slotData, player);
//						
//						int inventoryItemCount = inventoryItem.getCount();;
//						if (!inventoryItem.isEmpty()) {
//							// try giving it to the stand
//							stand.addItem(inventoryItem);
//						}
//						if (inventoryItemCount == inventoryItem.getCount()) {
//							// swap the stand main hand item with the hovered slot
//							Slot inventorySlot = player.inventoryMenu.slots.get(slotData.slotNum());
//							InteractionHand standHandToSwap;
//							if (inventoryItem.isEmpty()
//									&& stand.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
//									&& !stand.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
//								// take the item from the stand's off-hand
//								standHandToSwap = InteractionHand.OFF_HAND;
//							}
//							else {
//								standHandToSwap = InteractionHand.MAIN_HAND;
//							}
//							
//							inventorySlot.set(stand.getItemInHand(standHandToSwap));
//							stand.setItemInHand(standHandToSwap, inventoryItem);
//						}
//					}
				}
				else {
					ItemStack lUserItem = user.getOffhandItem();
					ItemStack rUserItem = user.getMainHandItem();
					int lUserItemCount = lUserItem.getCount();
					int rUserItemCount = rUserItem.getCount();
					
					 // if the player is holding any items, give them to the stand
					if (!lUserItem.isEmpty() || !rUserItem.isEmpty()) {
						stand.addItem(rUserItem);
						stand.addItem(lUserItem);
						boolean gaveSomethingToStand = lUserItem.getCount() < lUserItemCount || rUserItem.getCount() < rUserItemCount;
						if (!gaveSomethingToStand) {
							// swap the player's non-empty items with stand's
							if (!lUserItem.isEmpty()) swapItemsInHand(stand, user, InteractionHand.OFF_HAND);
							if (!rUserItem.isEmpty()) swapItemsInHand(stand, user, InteractionHand.MAIN_HAND);
						}
					}
					// or, if the player's hands are empty, *take* both items from the stand
					else {
						swapItemsInHand(stand, user, InteractionHand.OFF_HAND);
						swapItemsInHand(stand, user, InteractionHand.MAIN_HAND);
					}

					if (!stand.getOffhandItem().isEmpty()) {
						LivingComponentGrab standGrab = stand.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
						if (standGrab != null) {
							standGrab.setGrabTarget(null);
						}
					}
				}
			}
		}
	}
	
	public static void swapItemsInHand(LivingEntity stand, LivingEntity user, InteractionHand hand) {
		ItemStack userItem = user.getItemInHand(hand);
		ItemStack standItem = stand.getItemInHand(hand);
		stand.setItemInHand(hand, userItem);
		user.setItemInHand(hand, standItem);
	}
}
