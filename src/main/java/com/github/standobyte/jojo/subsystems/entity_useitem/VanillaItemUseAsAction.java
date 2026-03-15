package com.github.standobyte.jojo.subsystems.entity_useitem;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier.ActionAnimIdHandsided;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * Lets the mod's action system know that the entity is in the middle of using an item via the vanilla system
 * (drawing a bow, charging a crossbow, eating, etc.).
 */
public class VanillaItemUseAsAction extends SpecialEntityActionType {

	public VanillaItemUseAsAction(ResourceLocation id) {
		super("", id);
		// Anim set overwrites the vanilla player poses, we don't want that
		this.animSet = null;
	}

	@Override
	public EntityActionInstance createActionObj() {
		return new ItemUsingInstance(this);
	}

	public ActionAnimIdHandsided bowAnim = new ActionAnimIdHandsided(new ActionAnimIdentifier("bow_shoot", false));
	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance _action) {
		ItemUsingInstance action = (ItemUsingInstance) _action;
		
		LivingEntity stand = action.getPerformer();
		// FIXME isn't true for the first few frames
		if (stand.isUsingItem()) {
			ItemStack usedItem = stand.getUseItem();
			action.usedItem = usedItem;
			action.vanillaAnim = usedItem.getUseAnimation();
			action.useHand = getItemInHand(stand, HumanoidArm.LEFT) == usedItem ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
		}
		
		if (action.vanillaAnim != null) {
			return switch (action.vanillaAnim) {
				case BOW -> bowAnim.get(action.useHand);
				default -> super.getEntityAnim(action);
			};
		}
		
		return super.getEntityAnim(action);
	}
	
	public static ItemStack getItemInHand(LivingEntity entity, HumanoidArm hand) {
		return entity.getItemBySlot(hand == entity.getMainArm() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
	}

	public static class ItemUsingInstance extends EntityActionInstance {
		protected boolean isPlayerEntity;
		protected ServerPlayer standUserPlayer;
		
		protected ItemStack usedItem;
		UseAnim vanillaAnim;
		HumanoidArm useHand;

		public ItemUsingInstance() {
			this(ModSpecialActions.RMB_USING_ITEM.get());
			this.setStartingPhase();
		}

		protected ItemUsingInstance(EntityActionType ability) {
			super(ability);
			phasesLength.put(ActionPhase.PERFORM, 72000f);
			phasesLength.put(ActionPhase.RECOVERY, 10f);
		}
		
		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			isPlayerEntity = performer instanceof Player;
			standUserPlayer = performer instanceof StandEntity stand && stand.getUser() instanceof ServerPlayer player ? player : null;
		}
		
		@Override
		public void actionTick() { // force stop the action if the entity has stopped using the item
			if (!level().isClientSide() && phase == ActionPhase.PERFORM && !performer.isUsingItem()) {
				setPhase(ActionPhase.RECOVERY, 0);
				syncPhaseChanges();
			}
		}
		
		@Override
		public void onButtonStopHold() { // lets stands shoot bows and throw tridents when the client releases RMB
			if (!level().isClientSide() && !isPlayerEntity) {
				ServerSideLivingClick.releaseUsingItem(performer, standUserPlayer);
				setPhase(ActionPhase.RECOVERY, 0);
				syncPhaseChanges();
			}
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		
	}
	
}
