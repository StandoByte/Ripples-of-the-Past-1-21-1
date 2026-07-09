package com.github.standobyte.jojoimpl.stands.crazydiamond;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.client.standskin.text.StandSkinComponent;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier.ActionAnimIdHandsided;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;
import com.github.standobyte.jojo.util.functions.HandUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CrazyDBlockBulletAbility extends StandEntityAbility {

	public CrazyDBlockBulletAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, BlockBulletShot::new);
		setDefaultPhaseLength(ActionPhase.WINDUP, 15);
	}

	@Override
	public ConditionCheck checkSpecificConditions(Power<?> power) {
		LivingEntity user = power.getUser();
		var block = getBlockToShoot(user, PowerClass.STAND.cast(power));
		if (block == null) {
			return ConditionCheck.createNegative("item_hard_material");
		}
		return super.checkSpecificConditions(power);
	}
	
	@Nullable
	public static FittingItem getBlockToShoot(LivingEntity user, StandPower power) {
		FittingItem block;
		if (power != null) {
			StandEntity stand = power.getSummonedStandEntity();
			if (stand != null) {
				block = getBlockToShoot(stand, stand.getMainArm().getOpposite());
				if (block != null) return block;
				block = getBlockToShoot(stand, stand.getMainArm());
				if (block != null) return block;
			}
		}

		block = getBlockToShoot(user, user.getMainArm().getOpposite());
		if (block != null) return block;
		block = getBlockToShoot(user, user.getMainArm());
		if (block != null) return block;
		
		return null;
	}

	@Nullable
	public static FittingItem getBlockToShoot(LivingEntity entity, HumanoidArm side) {
		InteractionHand hand = HandUtil.getHand(entity, side);
		ItemStack item = entity.getItemInHand(hand);
		Block block = getBulletBlock(item);
		if (block != null) {
			return new FittingItem(entity, side, item, block);
		}
		return null;
	}

	@Nullable
	public static Block getBulletBlock(ItemStack itemStack) {
		if (itemStack == null) return null;
		
		Item item = itemStack.getItem();
		if (item instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			BlockState blockState = block.defaultBlockState();
			// TODO check block hardness
//			if (!StandStatFormulas.isBlockBreakable(
//					power.isActive() ? ((StandEntity) power.getStandManifestation()).getAttackDamage()
//							: power.getType().getStats().getBasePower() + power.getType().getStats().getDevPower(power.getStatsDevelopment()), 
//							blockState.getDestroySpeed(user.level, user.blockPosition()), blockState.getHarvestLevel())) {
//				return ConditionCheck.createNegative("stand_cant_break_block");
//			}
			if (hardMaterial(blockState)) {
				return block;
			}
		}
		
		return null;
	}
	
	static record FittingItem(LivingEntity entity, HumanoidArm hand, ItemStack item, Block block) {}

	@Override
	public void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		super.initActionFromConfig(action, level, powerUser, performer);
		if (!level.isClientSide()) {
			BlockBulletShot _action = ((BlockBulletShot) action);
			if (disableHoming(powerUser)) {
				_action.isHomingDisabled = true;
			}
			
			var blockAndItem = getBlockToShoot(powerUser, StandPower.get(powerUser));
			if (blockAndItem != null) {
				_action.itemHeldByUser = blockAndItem.entity.is(powerUser);
				_action.side = blockAndItem.hand;
			}
		}
	}

	public ActionAnimIdHandsided animSided = new ActionAnimIdHandsided(this.anim);
	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		HumanoidArm side = ((BlockBulletShot) action).side;
		if (side != null) return animSided.get(side);

		return super.getEntityAnim(action);
	}

	public static class BlockBulletShot extends EntityActionInstance {
		protected boolean isHomingDisabled = false;
		protected boolean itemHeldByUser = true;
		protected HumanoidArm side = HumanoidArm.LEFT;

		public BlockBulletShot(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			boolean offHandIsRight = side == HumanoidArm.RIGHT;
			setStandOffset(new Vec3(offHandIsRight ? -0.1 : 0.1, -0.25, -0.4), StandOffsetFromUser.Rotations.BODY, false);
		}
		
		@ApiStatus.OverrideOnly
		public void toBuf(FriendlyByteBuf buf) {
			buf.writeEnum(side);
			buf.writeBoolean(itemHeldByUser);
		}

		@ApiStatus.OverrideOnly
		public void fromBuf(FriendlyByteBuf buf) {
			side = buf.readEnum(HumanoidArm.class);
			itemHeldByUser = buf.readBoolean();
		}

		@Override
		public void actionTick() {
			if (level().isClientSide() && phase == ActionPhase.WINDUP && ClientGlobals.canSeeStands) {
				LivingEntity entity = getEntityHoldingItem();
				if (entity != null) {
					InteractionHand hand = HandUtil.getHand(entity, side);
					CustomParticlesHelper.createCDRestorationParticle(entity, hand);
				}
			}
		}
		
		protected LivingEntity getEntityHoldingItem() {
			LivingEntity user = getPowerUser();
			if (user == null) return null;
			
			LivingEntity entity = null;
			if (itemHeldByUser) {
				entity = user;
			}
			else {
				StandPower standPower = StandPower.get(user);
				if (standPower != null) {
					StandEntity standEntity = standPower.getSummonedStandEntity();
					if (standEntity != null) {
						entity = standEntity;
					}
				}
			}
			
			return entity;
		}

		@Override
		public void actionPerformStart() {
			Level level = level();
			if (!level.isClientSide()) {
				LivingEntity user = getPowerUser();
				if (user == null) return;

				StandPower standPower = StandPower.get(user);
				LivingEntity holdingItem = getEntityHoldingItem();
				if (holdingItem == null) return;
				
				var blockAndItem = getBlockToShoot(holdingItem, side);
				if (blockAndItem == null) return;
				
				CrazyDBlockBulletEntity bullet = new CrazyDBlockBulletEntity(performer, level);
				bullet.setShootingPosOf(user);
				bullet.setBlock(blockAndItem.block());
				bullet.setStandSkinIdFrom(standPower);
				
				if (standPower != null && !isHomingDisabled) {
					UserStandEffects.getEffectLookedAt(standPower, ModStandAbilities.EFFECT_CD_BLOOD_DROPS.get(), PLAYER_TRACKING_RANGE, user).ifPresent(effect -> {
						bullet.setTarget(effect.getTarget());
					});
					
				}
				
				// FIXME projectile inaccuracy
//				standEntity.shootProjectile(bullet, 2.0F, 0.25F);
				bullet.shootFromRotation(performer, 2.0f, 0);
				addProjectileWithStandStats(bullet);
				
				ItemStack item = blockAndItem.item();
				ItemTracker itemTracker = ItemTracking.getItemTracker(item, level);
				if (itemTracker != null) {
					itemTracker.setAtEntity(item.copy(), bullet.getId(), level, KnownItemState.ENTITY_IS_ITEM, id -> bullet.isAlive());
				}
				
				if (!(blockAndItem.entity() instanceof Player player && player.getAbilities().instabuild)) {
					item.shrink(1);
				}
				standPower.consumeStamina(40);
				bullet.homingStaminaCost = 2;
			}
		}
		
		@Override
		public void onSetPhase(ActionPhase newPhase) {
			Level level = level();
			if (level.isClientSide() && ClientGlobals.canHearStands && performer instanceof StandEntity stand) {
				switch (newPhase) {
					case WINDUP -> {
						ClientsideSoundsHelper.playNonVanillaClassSound(new EntityStoppableSoundInstance(ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.CRAZY_DIAMOND_FIX_STARTED.get(), stand), 
								stand.getSoundSource(), 1, 1, stand, level.random.nextLong(), 
								() -> this.isOver() || this.phase != ActionPhase.WINDUP));
					}
					case PERFORM -> {
						level.playLocalSound(stand, ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.CRAZY_DIAMOND_BULLET_SHOT.get(), stand), 
								stand.getSoundSource(), 1, 1);
					}
					default -> {}
				}
			}
		}
	}

	public static final double PLAYER_TRACKING_RANGE = 64;

	
	public static boolean disableHoming(LivingEntity user) {
		return user.isShiftKeyDown();
	}

	public static boolean isHoming(LivingEntity user, StandPower userPower) {
		return user != null && !disableHoming(user)
				&& UserStandEffects.getEffectLookedAt(userPower, ModStandAbilities.EFFECT_CD_BLOOD_DROPS.get(), PLAYER_TRACKING_RANGE, user).isPresent();
	}


	protected String homingSpriteName;
	protected String homingTlKey;
	
	@Override
	protected void initVariationAssets() {
		this.homingSpriteName = this.spriteName + "_homing";
		this.homingTlKey = tlKey(abilityId, ".homing");
	}
	
	@Override
	public String getSpriteName(Power<?> context) {
		if (isHoming(context.getUser(), PowerClass.STAND.cast(context))) {
			return homingSpriteName;
		}
		return super.getSpriteName(context);
	}

	@Override
	public Component getName(Power<?> context) {
		if (isHoming(context.getUser(), PowerClass.STAND.cast(context))) {
			return StandSkinComponent.translatable(context, homingTlKey);
		}
		return super.getName(context);
	}


	// FIXME add wood and glass blocks to the tag
	public static boolean hardMaterial(BlockState blockState) {
//        material == Material.WOOD || 
//        material == Material.NETHER_WOOD || 
//        material == Material.GLASS || 			// beacon, conduit, stained glass pane, glass pane, glass, sea lantern
//        material == Material.BUILDABLE_GLASS ||	// redstone lamp
		
		return blockState.is(ModBlocks.CRAZY_D_CAN_MAKE_BULLET);
	}
    
    public static boolean isGlassBlock(BlockState blockState, Level level, @Nullable BlockPos blockPos) {
    	SoundType soundType = blockPos != null ? blockState.getSoundType() : blockState.getSoundType(level, blockPos, null);
    	return soundType == SoundType.GLASS;
    }

}
