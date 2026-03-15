package com.github.standobyte.jojo.adventure.npc;

import java.util.Optional;

import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.mechanics.standdisc.StandDiscItem;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DummyStuff {

//	public static void onSyncedDataUpdated(LivingEntity dummyEntity, EntityDataAccessor<?> key) {
//		if (Entity.DATA_CUSTOM_NAME.equals(key) && !dummyEntity.level().isClientSide()) {
//			updateStandAction(dummyEntity);
//		}
//	}

	protected static void updateStandAction(LivingEntity dummyEntity) {
//		dummyEntity.action = null;
//		Component name = dummyEntity.getCustomName();
//		if (name != null) {
//			String nameStr = name.getString();
//			if (nameStr.contains(":")) {
//				dummyEntity.action = standActionFromId(ResourceLocation.parse(nameStr));
//			}
//			else {
//				StandPower stand = StandPower.get(dummyEntity);
//				if (stand != null && stand.hasPower()) {
//					ResourceLocation standId = stand.getType().getRegistryName();
//					dummyEntity.action = standActionFromId(ResourceLocation.fromNamespaceAndPath(standId.getNamespace(), standId.getPath() + "_" + nameStr));
//				}
//			}
//		}
	}

//	protected static StandAction standActionFromId(ResourceLocation id) {
//		if (JojoCustomRegistries.ACTIONS.getRegistry().containsKey(id)) {
//			Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(id);
//			if (action instanceof StandAction) {
//				return (StandAction) action;
//			}
//		}
//		return null;
//	}
//
//	public static void tick(LivingEntity dummyEntity) {
//		if (!dummyEntity.level().isClientSide() && dummyEntity.isAlive()) {
//			StandPower stand = StandPower.get(dummyEntity);
//			if (stand != null && stand.hasPower() && dummyEntity.action != null && dummyEntity.useAction) {
//				stand.clickAction(dummyEntity.action, false, ActionTarget.EMPTY, null);
//			}
//		}
//	}

	public static InteractionResult mobInteract(LivingEntity dummyEntity, Player player, InteractionHand hand) {
		Level level = dummyEntity.level();
		ItemStack item = player.getItemInHand(hand);
		if (item.getItem() == ModItems.STAND_DISC.get()) {
			if (!level.isClientSide()) {
				StandPower stand = PowerClass.STAND.attachGet(dummyEntity);
				if (stand != null) {
					if (stand.hasPower()) {
						Optional<StandInstance> previousDiscStand = stand.getStandInstance();
						previousDiscStand.ifPresent(prevStand -> {
							player.drop(StandDiscItem.withStand(prevStand), false);
						});
					}

					StandInstance discStand = StandDiscItem.getStandInstance(item);
					if (discStand != null) {
						stand.setStandInstance(Optional.of(discStand));
						stand.skipProgression();
						if (!player.getAbilities().instabuild) {
							item.shrink(1);
						}
						updateStandAction(dummyEntity);
						return InteractionResult.SUCCESS;
					}
				}
				return InteractionResult.FAIL;
			} else {
				return InteractionResult.CONSUME;
			}
		}
		else {
			if (!level.isClientSide()) {
				StandPower stand = StandPower.get(dummyEntity);
				if (stand != null && stand.hasPower()) {
					if (player.isShiftKeyDown()) {
//						dummyEntity.useAction = !dummyEntity.useAction;
//						if (!dummyEntity.useAction) {
//							stand.stopHeldAction(false);
//						}
					}
					else {
						stand.getPowerType().onUserSummonCommand(dummyEntity, stand);
					}
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide());
		}
	}
}
