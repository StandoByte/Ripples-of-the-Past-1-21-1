package com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.input;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_grab.LivingComponentGrab;
import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class StandItemInput {

	public enum Action {
		DROP,
		DROP_FULL_STACK,
		SWAP_HANDS,
		SWAP_USER_AND_STAND
	}

	public static void handlePacket(Action action, StandEntity standEntity, LivingEntity user) {
		switch (action) {
			case DROP -> {
				if (!standEntity.getMainHandItem().isEmpty()) {
					standEntity.tossItem(InteractionHand.MAIN_HAND, false);
				}
				else {
					standEntity.tossItem(InteractionHand.OFF_HAND, false);
				}
			}
			case DROP_FULL_STACK -> {
				if (!standEntity.getMainHandItem().isEmpty()) {
					standEntity.tossItem(InteractionHand.MAIN_HAND, true);
				}
				else {
					standEntity.tossItem(InteractionHand.OFF_HAND, true);
				}
			}
			case SWAP_HANDS -> {
				ItemStack lItem = standEntity.getOffhandItem();
				ItemStack rItem = standEntity.getMainHandItem();
				standEntity.setItemInHand(InteractionHand.OFF_HAND, rItem);
				standEntity.setItemInHand(InteractionHand.MAIN_HAND, lItem);
			}
			case SWAP_USER_AND_STAND -> {
				ConditionCheck condition = distanceCondition(standEntity, user);
				if (!condition.isPositive()) {
					ConditionCheck.sendActionFailedMessage(null, condition, user);
					return;
				}
				ItemStack lUserItem = user.getOffhandItem();
				ItemStack rUserItem = user.getMainHandItem();
				int lUserItemCount = lUserItem.getCount();
				int rUserItemCount = rUserItem.getCount();

				// if the player is holding any items, give them to the stand
				if (!lUserItem.isEmpty() || !rUserItem.isEmpty()) {
					standEntity.addItem(rUserItem);
					standEntity.addItem(lUserItem);
					boolean gaveSomethingToStand = lUserItem.getCount() < lUserItemCount || rUserItem.getCount() < rUserItemCount;
					if (!gaveSomethingToStand) {
						// swap the player's non-empty items with stand's
						if (!lUserItem.isEmpty()) swapItemsInHand(standEntity, user, InteractionHand.OFF_HAND);
						if (!rUserItem.isEmpty()) swapItemsInHand(standEntity, user, InteractionHand.MAIN_HAND);
					}
				}
				// or, if the player's hands are empty, *take* both items from the stand
				else {
					ItemStack rStandItem = standEntity.getMainHandItem();
					// the stand only has an item in offhand, put it to the player's main hand
					if (rStandItem.isEmpty()) {
						ItemStack lStandItem = standEntity.getOffhandItem();
						standEntity.setItemInHand(InteractionHand.OFF_HAND, rUserItem /* which is empty btw */);
						user.setItemInHand(InteractionHand.MAIN_HAND, lStandItem);
					}
					else {
						swapItemsInHand(standEntity, user, InteractionHand.MAIN_HAND);
						swapItemsInHand(standEntity, user, InteractionHand.OFF_HAND);
					}
				}

				if (!standEntity.getOffhandItem().isEmpty()) {
					LivingComponentGrab standGrab = standEntity.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
					if (standGrab != null) {
						standGrab.setGrabTarget(null);
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
	
	public static ConditionCheck distanceCondition(StandEntity stand, LivingEntity user) {
		if (stand == null || user == null) return ConditionCheck.NEGATIVE;
		
		if (!stand.isFollowingUser() && MathUtil.getAABBDistance(stand.getBoundingBox(), user.getBoundingBox()) > 4.5) {
			return ConditionCheck.createNegative("stand_user_too_far");
		}
		
		return ConditionCheck.POSITIVE;
	}
	
}
