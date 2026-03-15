package com.github.standobyte.jojoimpl.stands.crazydiamond;

import java.util.Collections;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.subsystems.itemtracking.OriginalItemPosComponent;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.BrokenBlocksChunkData;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.CDBlocksRestoredPacket;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.PrevBlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class CrazyDAnchorBlockAbility extends StandEntityAbility {

	public CrazyDAnchorBlockAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, AnchorBlockMove::new);
		setButtonHoldPhase(ActionPhase.PERFORM);
	}

	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		FoundAnchor anchor = getItemToUseAsAnchor(PowerClass.STAND.cast(context));
		return ConditionCheck.noMessage(anchor != null);
	}
	
	public static record FoundAnchor(LivingEntity entity, InteractionHand hand, 
			ItemStack item, OriginalItemPosComponent itemComponent) {}
	
	@Nullable
	public static FoundAnchor getItemToUseAsAnchor(StandPower standPower) {
		FoundAnchor anchor;
		
		StandEntity standEntity = standPower.getSummonedStandEntity();
		if (standEntity != null && standEntity.isManuallyControlled()) {
			anchor = getOriginalPosFromItemInHand(standEntity);
			if (anchor != null) return anchor;
		}
		
		LivingEntity user = standPower.getUser();
		if (user != null) {
			anchor = getOriginalPosFromItemInHand(user);
			if (anchor != null) return anchor;
		}
		
		if (standEntity != null) {
			anchor = getOriginalPosFromItemInHand(standEntity);
			if (anchor != null) return anchor;
		}
		
		return null;
	}
	
	@Nullable
	public static FoundAnchor getOriginalPosFromItemInHand(LivingEntity entity) {
		ItemStack item = entity.getMainHandItem();
		OriginalItemPosComponent component = item.get(ModItemDataComponents.ORIGINAL_POS);
		if (component != null) return new FoundAnchor(entity, InteractionHand.MAIN_HAND, item, component);
		item = entity.getOffhandItem();
		component = item.get(ModItemDataComponents.ORIGINAL_POS);
		if (component != null) return new FoundAnchor(entity, InteractionHand.OFF_HAND, item, component);
		return null;
	}
	
	
	public static class AnchorBlockMove extends EntityActionInstance {

		public AnchorBlockMove(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				setPhaseStart(ActionPhase.RECOVERY);
				syncPhaseChanges();
			}
		}
		
		@Override
		public void actionTick() {
			LivingEntity user = getPowerUser();
			Level level = user.level();
			if (!level.isClientSide()) {
				StandPower standPower = StandPower.get(user);
				if (standPower != null) {
					FoundAnchor anchor = getItemToUseAsAnchor(standPower);
					if (anchor != null) {
						Entity entity = entityToMove(anchor.entity);
						BlockPos blockPos = anchor.itemComponent.blockPos();
						Vec3 pos = Vec3.atCenterOf(blockPos);
		                boolean isCloseToAnchorPos = isCloseToAnchorPos(entity, pos);
		                if (!isCloseToAnchorPos) {
		                	moveWithAnchor(entity, pos);
		                }
		                else {
		                	ItemStack heldItem = anchor.item;
		                	
		                	BlockState blockStateToPlace = null;
		                	var brokenBlocks = BrokenBlocksChunkData.getExistingData(level, blockPos);
		                	if (brokenBlocks != null) {
		                		PrevBlockInfo block = brokenBlocks.getBrokenBlockAt(blockPos);
		                		if (block != null && block.drops.size() == 1 && ItemStack.matches(block.drops.get(0), heldItem)) {
		                			blockStateToPlace = block.state;
		                		}
		                	}
		                	
		                	BlockState curBlockState = level.getBlockState(blockPos);
		                	boolean willPlaceBlock = blockStateToPlace != null && FallingBlock.isFree(curBlockState);

		                	if (willPlaceBlock) {
		                		heldItem.shrink(1);
	                			level.setBlockAndUpdate(blockPos, blockStateToPlace);
//		                		performer.playSound(ModSoundEvents.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F);
		                	}
		                }
		                ServerLevel serverLevel = (ServerLevel) level;
						// TODO sounds
						// TODO particles on the item itself
						PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(blockPos), 
								new CDBlocksRestoredPacket(Collections.singletonList(blockPos), Collections.emptyList()));
					}
					else {
						setPhaseStart(ActionPhase.RECOVERY);
						syncPhaseChanges();
					}
				}
			}
		}
		
	}
	
	public static Entity entityToMove(Entity itemHolderEntity) {
		return itemHolderEntity.getRootVehicle();
	}
	
	public static boolean isCloseToAnchorPos(Entity entity, Vec3 pos) {
		return entity.distanceToSqr(pos) <= 16;
	}
	
	public static void moveWithAnchor(Entity entity, Vec3 pos) {
    	entity.setDeltaMovement(pos.subtract(entity.position()).normalize().scale(0.75));
        entity.fallDistance = 0;
        entity.hurtMarked = true;
	}
}
