package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracker;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracking;
import com.github.standobyte.jojo.mechanics.itemtracking.OriginalItemPosComponent;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.mc.ItemUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StandEntityGrabAbility extends StandEntityAbility {

	public StandEntityGrabAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandEntityGrab::new);
		usageGroup = AbilityUsageGroup.COMBAT;
		setDefaultPhaseLength(ActionPhase.WINDUP, 9);
		noFinisherBarDecay = true;
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		return super.isAbilityAvailable(context) && StandUtil.getStandGrabTarget(context) == null;
	}
	

	public static class StandEntityGrab extends EntityActionInstance {

		public StandEntityGrab(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(0, 2, StandOffsetFromUser.Rotations.HEAD_XY, false);
			keepStandAimedAtTarget();
			aimAs = AimingEntity.STAND;
			tossStandHeldItems(EquipmentSlot.OFFHAND);
		}

		@Override
		public void actionPerformStart() {
			Level level = level();
			if (performer instanceof StandEntity standEntity) {
				LivingEntity user = standEntity.getUser();
				ActionTarget target = HitResultUtil.clipEntityLook(standEntity, 
						entity -> !entity.is(user) && StandEntityPunchAbility.canStandHit(standEntity, entity) && !(
								entity instanceof LivingEntity living && (
										LivingComponentGrab.getEntityGrabbedBy(living) != null
										|| LivingComponentGrab.getEntityGrabbing(living) != null)), 
						0);
				if (!level.isClientSide()) {
					switch (target.getType()) {
						case ENTITY -> {
							Entity targetEntity = target.getMainEntity();
							// TODO grab entities like lit TNT
							if (targetEntity instanceof LivingEntity targetLiving) {
								LivingComponentGrab standGrab = performer.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
								tossStandHeldItems(EquipmentSlot.OFFHAND);
								standGrab.setGrabTarget(targetLiving);
							}
						}
						case BLOCK -> {
							BlockPos blockPos = target.getBlockPos();
							BlockState blockState = level.getBlockState(blockPos);
							double standStrength = standEntity.getAttackDamage();
							boolean canGrab = StandStatFormulas.canGrabBlock(standStrength, blockState, level, blockPos);
							if (canGrab) {
								ServerLevel serverLevel = (ServerLevel) level;
								// create the stack for the block
								List<ItemStack> items = ItemUtil.turnBlockToItem(blockState, serverLevel, blockPos, standEntity);
								ItemStack blockItem = null;
								if (items.size() == 1) {
									blockItem = items.get(0);
									if (blockItem.getCount() != 1) blockItem = null;
								}
								if (blockItem == null) {
									user.sendSystemMessage(ConditionCheck.message("grab_block.weird_drops"));
								}
								else {
									addOriginalPos(blockItem, blockPos, serverLevel);

									// TODO track the item in the ITS
									ItemTracker itemTracker = ItemTracking.getItemTracking(level).startTracking(blockItem, serverLevel);
									if (itemTracker != null) {
										itemTracker.context = "grabbed_block";
									}
									
									// TODO remember the removed block for the terrain restoration ability (ALONG with the ITS tracker)
									level.removeBlock(blockPos, false);
									CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, blockPos, blockState, 
											Optional.ofNullable(level.getBlockEntity(blockPos)), Collections.singletonList(blockItem), true);
									
									tossStandHeldItems(EquipmentSlot.OFFHAND);
									standEntity.setItemInHand(InteractionHand.OFF_HAND, blockItem);
								}
							}
							else {
								float hardness = blockState.getDestroySpeed(level, blockPos);
								if (hardness > 0) {
									user.sendSystemMessage(ConditionCheck.message("grab_block.too_hard"));
								}
							}
						}
						default -> {}
					}
					StandPower standPower = StandPower.get(getPowerUser());
					standPower.consumeStamina(10);
				}
				
				if (target.getType() == TargetType.ENTITY) {
					standRotationTarget = target;
				}
				else {
					aimAs = AimingEntity.CAMERA_ENTITY;
				}
			}
		}

	}

	public static void addOriginalPos(ItemStack item, BlockPos blockPos, ServerLevel level) {
		item.set(ModItemDataComponents.ORIGINAL_POS, new OriginalItemPosComponent(blockPos));
		item.set(DataComponents.ITEM_NAME, Component.translatable("translation.test.args", 
				item.getHoverName(), 
				ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockPos.getX(), blockPos.getY(), blockPos.getZ()))
						.withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN)));
	}

}
