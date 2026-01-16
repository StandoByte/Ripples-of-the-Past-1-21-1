package com.github.standobyte.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CrazyDBloodCutterAbility extends StandEntityAbility {

	public CrazyDBloodCutterAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, CutterShot::new);
		setDefaultPhaseLength(ActionPhase.WINDUP, 5);
	}

	@Override
	public ConditionCheck checkSpecificConditions(Power<?> power) {
		LivingEntity user = power.getUser();
		if (user.getHealth() >= user.getMaxHealth()
				&& !(user instanceof Player player && player.getAbilities().invulnerable)) {
			return ConditionCheck.createNegative("full_health");
		}
		return super.checkSpecificConditions(power);
	}


	public static class CutterShot extends EntityActionInstance {

		public CutterShot(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(-0.1, -0.5, StandOffsetFromUser.Rotations.HEAD_XY, false);
		}

		@Override
		public void actionPerformStart() {
			Level level = level();
			if (!level.isClientSide()) {
				LivingEntity user = getPowerUser();
				CrazyDBloodCutterEntity cutter = new CrazyDBloodCutterEntity(user, level);
				cutter.setShootingPosOf(user);
				
				Vec3 pos = cutter.position();
				// TODO (blood cutter) offset it a bit to the right (doesn't work correctly when looking up/down but i wanna go sleep already)
				pos = pos.add(user.getLookAngle().yRot((float) -Math.PI / 2).scale(0.25));
				cutter.setPos(pos);
				
				// FIXME projectile inaccuracy
				// FIXME stutter when shooting a projectile
//				cutter.shootFromRotation(user, 1.5f, standEntity.getProjectileInaccuracy(1.0F));
				cutter.shootFromRotation(user, 1.5f, 0);
				addProjectileWithStandStats(cutter);

				StandPower standPower = StandPower.get(user);
				if (standPower != null) {
					int cooldown = 300;
					if (!standPower.isUserCreative() && standPower.getUser() != null) {
						cooldown = Mth.ceil((float) cooldown * user.getHealth() / user.getMaxHealth());
						// FIXME ability cooldowns
					}
					
					standPower.consumeStamina(25);
				}
			}
		}
		
		@Override
		public void onSetPhase(ActionPhase newPhase) {
			Level level = level();
			if (level.isClientSide() && ClientGlobals.canHearStands && performer instanceof StandEntity stand) {
				switch (newPhase) {
					case WINDUP -> {
						if (!stand.isArmsOnlyMode()) {
							ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
									ModSoundEvents.STAND_PUNCH_CRY.get(), stand), 
									stand.getSoundSource(), 1, 1, stand, level));
						}
					}
					case PERFORM -> {
						level.playLocalSound(stand, ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.CRAZY_DIAMOND_BLOOD_CUTTER_SHOT.get(), stand), 
								stand.getSoundSource(), 1, 1);
					}
					default -> {}
				}
			}
		}

	}
	
	public static void onBleedingAdded(LivingEntity entity) {
//		IStandPower.getStandPowerOptional(entity).ifPresent(power -> {
//			if (ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get().isUnlocked(power)) {
//				power.setCooldownTimer(ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get(), 0);
//			}
//		});
	}

}
