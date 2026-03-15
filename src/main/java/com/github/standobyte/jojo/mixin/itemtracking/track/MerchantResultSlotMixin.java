package com.github.standobyte.jojo.mixin.itemtracking.track;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {
	@Shadow @Final private MerchantContainer slots;
	@Shadow @Final private Merchant merchant;

	@Inject(method = "onTake", at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/world/item/trading/MerchantOffer;take("
					+ "Lnet/minecraft/world/item/ItemStack;"
					+ "Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 0))
	public void jojo_ripples$onTradePerform(Player player, ItemStack item, CallbackInfo ci) {
		Level level = player.level();
		if (!level.isClientSide()) {
			ItemStack playerOfferA = slots.getItem(0);
			ItemStack playerOfferB = slots.getItem(1);
			MerchantOffer offer = slots.getActiveOffer();
			if ((offer.satisfiedBy(playerOfferA, playerOfferB) || offer.satisfiedBy(playerOfferB, playerOfferA)) && merchant instanceof Entity merchantEntity) {
				ServerLevel world = (ServerLevel) level;
				ItemTracker trackerA = ItemTracking.getItemTracker(playerOfferA, level);
				if (trackerA != null) {
					jojo_ripples$trackTradeCost(playerOfferA, trackerA, merchantEntity, world);
				}
				ItemTracker trackerB = ItemTracking.getItemTracker(playerOfferB, level);
				if (trackerB != null) {
					jojo_ripples$trackTradeCost(playerOfferB, trackerB, merchantEntity, world);
				}
			}
		}
	}

	private static void jojo_ripples$trackTradeCost(ItemStack item, ItemTracker tracker, Entity merchantEntity, ServerLevel world) {
		ItemStack itemCopy = item.copy();
		tracker.setAtEntity(itemCopy, merchantEntity.getId(), world, KnownItemState.ENTITY_HAS_ITEM, null);
	}
}
