package com.github.standobyte.jojo.client.input;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.CommonHooks;

public class ClientSideClickCopypasta {

	public static InteractionResult startUseItem() {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameMode.isDestroying()) {
			mc.rightClickDelay = 4;
			if (!mc.player.isHandsBusy()) {
				if (mc.hitResult == null) {
					JojoMod.getLogger().warn("Null returned as 'hitResult', mc shouldn't happen!");
				}

				for (InteractionHand hand : InteractionHand.values()) {
					InputEvent.InteractionKeyMappingTriggered inputEvent = ClientHooks.onClickInput(1, mc.options.keyUse, hand);
					if (inputEvent.isCanceled()) {
						if (inputEvent.shouldSwingHand()) {
							mc.player.swing(hand);
						}
						return InteractionResult.SUCCESS; // I guess?
					}
					ItemStack item = mc.player.getItemInHand(hand);
					if (!item.isItemEnabled(mc.level.enabledFeatures())) {
						return InteractionResult.PASS;
					}

					if (mc.hitResult != null) {
						InteractionResult interactionResult;
						switch (mc.hitResult.getType()) {
							case ENTITY -> {
								EntityHitResult entityHitResult = (EntityHitResult)mc.hitResult;
								Entity entity = entityHitResult.getEntity();
								if (!mc.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
									return InteractionResult.FAIL;
								}
								
								interactionResult = mc.gameMode.interactAt(mc.player, entity, entityHitResult, hand);
								if (!interactionResult.consumesAction()) {
									interactionResult = mc.gameMode.interact(mc.player, entity, hand);
								}
								
								if (interactionResult.consumesAction()) {
									if (interactionResult.shouldSwing() && inputEvent.shouldSwingHand()) {
										mc.player.swing(hand);
									}
									
									return interactionResult;
								}
							}
							case BLOCK -> {
								BlockHitResult blockHitResult = (BlockHitResult) mc.hitResult;
								int prevItemCount = item.getCount();
								interactionResult = mc.gameMode.useItemOn(mc.player, hand, blockHitResult);
								if (interactionResult.consumesAction()) {
									if (interactionResult.shouldSwing() && inputEvent.shouldSwingHand()) {
										mc.player.swing(hand);
										if (!item.isEmpty() && (item.getCount() != prevItemCount || mc.gameMode.hasInfiniteItems())) {
											mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
										}
									}
									
									return interactionResult;
								}
								
								if (interactionResult == InteractionResult.FAIL) {
									return interactionResult;
								}
							}
							default -> {}
						}
					}

					if (item.isEmpty() && (mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS)) {
						CommonHooks.onEmptyClick(mc.player, hand);
					}

					if (!item.isEmpty()) {
						InteractionResult result = mc.gameMode.useItem(mc.player, hand);
						if (result.consumesAction()) {
							if (result.shouldSwing()) {
								mc.player.swing(hand);
							}

							mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
							return result;
						}
					}
				}
			}
		}
		
		return InteractionResult.PASS;
	}
}
