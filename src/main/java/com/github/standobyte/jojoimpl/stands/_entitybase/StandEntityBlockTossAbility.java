package com.github.standobyte.jojoimpl.stands._entitybase;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.customobjects.entity_projectile.ThrownBlockEntity;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier.ActionAnimIdHandsided;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.subsystems.entity_useitem.StandCallbackWhenShooting;
import com.github.standobyte.jojo.subsystems.target.AimingEntity;
import com.github.standobyte.jojo.util.functions.UtilFunctions;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class StandEntityBlockTossAbility extends StandEntityAbility {

	public StandEntityBlockTossAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandEntityBlockToss::new);
		setDefaultPhaseLength(ActionPhase.BUTTON_CHARGE, 16);
		setButtonHoldPhase(ActionPhase.WINDUP);
		setDefaultPhaseLength(ActionPhase.PERFORM, 6);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 12);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		if (super.isAbilityAvailable(context)) {
			StandEntity standEntity = StandUtil.getSummonedStand(context);
			if (standEntity != null) {
				for (InteractionHand hand : InteractionHand.values()) {
					ItemStack item = standEntity.getItemInHand(hand);
					if (isThrowable(item)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public AbilityInputState cl_abilityInputState(Power<?> context) {
		AbilityInputState state = super.cl_abilityInputState(context);
		state.setFlag(AbilityInputState.WITH_ITEM_HELD, true);
		state.setFlag(AbilityInputState.HIGH_PRIORITY, true);
		return state;
	}
	
	protected static final ItemStack DEFAULT_ITEM_ICON = new ItemStack(Items.COBBLESTONE);
	@Override
	public void renderAbilityIcon(Power<?> context, GuiGraphics guiGraphics, TextureAtlasSprite sprite, float x, float y, int color) {
		ItemStack item = DEFAULT_ITEM_ICON;
		
		if (context != null) {
			StandEntity standEntity = StandUtil.getSummonedStand(context);
			if (standEntity != null) {
				for (InteractionHand hand : InteractionHand.values()) {
					ItemStack itemToThrow = standEntity.getItemInHand(hand);
					if (isThrowable(itemToThrow)) {
						item = itemToThrow;
						break;
					}
				}
			}
		}

		guiGraphics.renderFakeItem(item, (int) x, (int) y);
	}
	
	public ActionAnimIdHandsided animSided = new ActionAnimIdHandsided(this.anim);
	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		HumanoidArm side = ((StandEntityBlockToss) action).side;
		if (side != null) return animSided.get(side);

		return super.getEntityAnim(action);
	}
	
	
	public static boolean isThrowable(ItemStack item) {
		if (item.isEmpty()) return false;
		Item itemType = item.getItem();
		return itemType instanceof BlockItem;
	}
	
	
	public static class StandEntityBlockToss extends EntityActionInstance {
		private HumanoidArm side = HumanoidArm.LEFT;

		public StandEntityBlockToss(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack itemToThrow = performer.getItemInHand(hand);
				if (isThrowable(itemToThrow)) {
					side = UtilFunctions.getHandSide(performer, hand);
					break;
				}
			}
		}
		
		@ApiStatus.OverrideOnly
		public void toBuf(FriendlyByteBuf buf) {
			buf.writeEnum(side);
		}

		@ApiStatus.OverrideOnly
		public void fromBuf(FriendlyByteBuf buf) {
			side = buf.readEnum(HumanoidArm.class);
		}
		
		@Override
		public void onButtonStopHold() {
			switch (getPhase()) {
				case BUTTON_CHARGE -> {
					phasesLength.put(ActionPhase.WINDUP, 0f);
					syncPhaseChanges();
				}
				case WINDUP -> {
					setPhaseStart(ActionPhase.PERFORM);
					syncPhaseChanges();
				}
				default -> {}
			}
		}
		
		@Override
		public void actionPerformStart() {
			if (performer instanceof StandEntity stand) {
				setStandOffset(0, Math.max(stand.offsetFromUser.getRelativeOffset().z, 0) + 2,
						StandOffsetFromUser.Rotations.HEAD_XY,
						false);
				
				Level level = performer.level();
				if (level.isClientSide() && ClientGlobals.canHearStands) {
					ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_PUNCH_HEAVY_CRY.get(), stand), 
							stand.getSoundSource(), 1, 1, stand, level));
				}
			}
			aimAs = AimingEntity.STAND;
		}
		
		@Override
		public void actionPerformEnd() {
			Level level = level();
			if (level instanceof ServerLevel serverLevel) {
				ItemStack itemStack = performer.getItemInHand(UtilFunctions.getHand(performer, side));
				if (isThrowable(itemStack)) {
					StandEntity stand = performer instanceof StandEntity s ? s : null;

					StandCallbackWhenShooting.shootProjectileWithStandStats(ThrownBlockEntity::fromItem, 
							serverLevel, itemStack, performer, stand, 0, 1.0f, 1.0f);
					
					itemStack.consume(1, performer);
					StandPower standPower = StandPower.get(getPowerUser());
					standPower.consumeStamina(20);
				}
			}
			aimAs = AimingEntity.CAMERA_ENTITY;
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return phase.ordinal() < ActionPhase.PERFORM.ordinal();
		}
		
	}
}
