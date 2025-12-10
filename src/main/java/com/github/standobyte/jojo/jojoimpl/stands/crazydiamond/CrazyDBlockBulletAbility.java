package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.ModUtilTags;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
		ItemStack itemToShoot = user.getOffhandItem();
		if (itemToShoot == null || itemToShoot.isEmpty() || !(itemToShoot.getItem() instanceof BlockItem)) {
			return ConditionCheck.createNegative("block_offhand");
		}
		Block block = ((BlockItem) itemToShoot.getItem()).getBlock();
		BlockState blockState = block.defaultBlockState();
		// TODO check block hardness
//		if (!StandStatFormulas.isBlockBreakable(
//				power.isActive() ? ((StandEntity) power.getStandManifestation()).getAttackDamage()
//						: power.getType().getStats().getBasePower() + power.getType().getStats().getDevPower(power.getStatsDevelopment()), 
//						blockState.getDestroySpeed(user.level, user.blockPosition()), blockState.getHarvestLevel())) {
//			return ConditionCheck.createNegative("stand_cant_break_block");
//		}
		if (!hardMaterial(blockState)) {
			return ConditionCheck.createNegative("item_hard_material");
		}
		return super.checkSpecificConditions(power);
	}

	@Override
	public void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		super.initActionFromConfig(action, level, powerUser, performer);
		if (!level.isClientSide() && disableHoming(powerUser)) {
			((BlockBulletShot) action).isHomingDisabled = true;
		}
	}

	public static class BlockBulletShot extends EntityActionInstance {
		protected boolean isHomingDisabled = false;

		public BlockBulletShot(EntityActionType ability) {
			super(ability);
		}

		// TODO mirror the animation for right-handed player
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			boolean offHandIsRight = getPowerUser().getMainArm() == HumanoidArm.LEFT;
			setStandOffset(new Vec3(offHandIsRight ? -0.1 : 0.1, -0.25, -0.4), StandOffsetFromUser.Rotations.BODY, false);
		}

		@Override
		public void actionTick() {
			if (level().isClientSide() && phase == ActionPhase.WINDUP && ClientGlobals.canSeeStands) {
				LivingEntity user = getPowerUser();
				if (user != null) {
					CustomParticlesHelper.createCDRestorationParticle(user, InteractionHand.OFF_HAND);
				}
			}
		}

		@Override
		public void actionPerformStart() {
			Level level = level();
			if (!level.isClientSide()) {
				LivingEntity user = getPowerUser();
				if (user == null) return;
				ItemStack item = user.getOffhandItem();
				Block block = !item.isEmpty() && item.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : null;
				if (block == null) return;
				
				CrazyDBlockBulletEntity bullet = new CrazyDBlockBulletEntity(performer, level);
				bullet.setShootingPosOf(user);
				bullet.setBlock(block);
				
				StandPower standPower = StandPower.get(user);
				if (standPower != null && !isHomingDisabled) {
					UserStandEffects.getEffectLookedAt(standPower, ModStandAbilities.EFFECT_CD_BLOOD_DROPS.get(), PLAYER_TRACKING_RANGE, user).ifPresent(effect -> {
						bullet.setTarget(effect.getTarget());
					});
					
				}
				
				// FIXME projectile inaccuracy
//				standEntity.shootProjectile(bullet, 2.0F, 0.25F);
				bullet.shootFromRotation(performer, 2.0f, 0);
				addProjectileWithStandStats(bullet);
				
				if (!(user instanceof Player player && player.getAbilities().instabuild)) {
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

	// FIXME add wood and glass blocks to the tag
	public static boolean hardMaterial(BlockState blockState) {
		return blockState.is(ModUtilTags.Blocks.CRAZY_D_CAN_MAKE_BULLET);
	}

	
	public static boolean disableHoming(LivingEntity user) {
		return user.isShiftKeyDown();
	}

	public static boolean isHoming(LivingEntity user, StandPower userPower) {
		return user != null && !disableHoming(user)
				&& UserStandEffects.getEffectLookedAt(userPower, ModStandAbilities.EFFECT_CD_BLOOD_DROPS.get(), PLAYER_TRACKING_RANGE, user).isPresent();
	}


	protected String homingSpriteName;
	protected Component homingAbilityName;
	
	@Override
	protected void initVariationAssets() {
		this.homingSpriteName = this.spriteName + "_homing";
		this.homingAbilityName = abilityName(abilityId, ".homing");
	}
	
	@Override
	public String getSpriteName(Power<?> context) {
		if (isHoming(context.getUser(), PowerClass.STAND.cast(context))) {
			return homingSpriteName;
		}
		return super.getSpriteName(context);
	}

	// TODO ability names in stand skins
	@Override
	public Component getName(Power<?> context) {
		if (isHoming(context.getUser(), PowerClass.STAND.cast(context))) {
			return homingAbilityName;
		}
		return name;
	}

}
