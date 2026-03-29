package com.github.standobyte.jojoimpl.powers.hamon;

import java.util.OptionalInt;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerData;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.jojoimpl.powers.hamon.data.HamonDataAura;
import com.github.standobyte.jojoimpl.powers.hamon.data.HamonDataEnergy;
import com.github.standobyte.jojoimpl.powers.hamon.data.HamonDataExercises;
import com.github.standobyte.jojoimpl.powers.hamon.data.HamonDataSkills;
import com.github.standobyte.jojoimpl.powers.hamon.data.HamonDataStats;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

// TODO atrribute for Hamon efficiency (decreased by freezing and bleeding, affects Hamon strength and Hamon energy gain speed)
public class HamonData extends PlayerPowerData {
	public HamonDataEnergy energy = new HamonDataEnergy(this);
	public HamonDataStats stats = new HamonDataStats(this);
	public HamonDataExercises exercises = new HamonDataExercises(this);
	public HamonDataSkills skills = new HamonDataSkills(this);
	public HamonDataAura aura = new HamonDataAura(this);
	//public HamonDataRequestsToTrain requestsToTrain = new HamonDataRequestsToTrain(this);
	
	
	//public HamonProjectileShieldEntity shieldEntity;
	//private boolean hamonProtection = false;
	//public OptionalInt regenImpliedDuration = OptionalInt.empty();

	//private boolean waterWalkingPrevTick = false;
	//private boolean waterWalkingThisTick = false;
	//private boolean trWaterWalking = false;
	//private boolean clLargeSpark = false;
	//private boolean clTickSpark = false;

	public HamonData() {
		super(HamonPowerType.HAMON.get());
	}
	
	@Override
	public void tick(Power<?> userPower) {
		super.tick(userPower);
		LivingEntity user = userPower.getUser();
		energy.tick(user);
	}

//	public void onClear(PlayerPower power) {
//		clearBreathingTrainingBuffs(power.getUser());
//	}
//
//	public void tick(PlayerPower power) {
//		updateHeight = false;
//		LivingEntity user = power.getUser();
//		if (user.isAlive()) {
//			/*if (hamonProtection) {
//                if (user.level.isClientSide) {
//                    tickHamonProtection();
//                }
//            }*/
//
//			HamonWallClimbing2.tickWallClimbing(power, this, user);
//			tickNewPlayerLearners(user);
//			if (!user.level().isClientSide()) {
//				tickAirSupply(user);
//
//				if (tcsa && (power.isUserCreative() || getCharacterTechnique() != null)) {
//					tcsa = false;
//				}
//
//				if (shieldEntity != null && !shieldEntity.isAlive()) {
//					shieldEntity = null;
//				}
//			}
//			tickChargeParticles();
//			tickBreathStability();
//		}
//		else {
//			setIsMeditating(user, false);
//
//			if (shieldEntity != null) {
//				shieldEntity.remove();
//				shieldEntity = null;
//			}
//			hamonProtection = false;
//		}
//
//		waterWalkingThisTick = false;
//	}
//
//
//
//	public static final float ALL_EXERCISES_EFFICIENCY_ADD_MULTIPLIER = 0.05F;
//	public float getActionEfficiency(float energyCost, boolean handSwingTimer, @Nullable AbstractHamonSkill hamonSkill) {
//		float efficiency = getHamonEnergyUsageEfficiency(energyCost, false) * getBloodstreamEfficiency();
//
//		if (efficiency > 0) {
//			float multiplier = 1;
//			if (exercisesCompleted >= MAX_EXERCISES_NEEDED) {
//				multiplier += ALL_EXERCISES_EFFICIENCY_ADD_MULTIPLIER;
//			}
//			if (hamonSkill != null) {
//				CharacterHamonTechnique technique = getCharacterTechnique();
//				if (technique != null) {
//					multiplier += technique.getAddSkillEfficiency(hamonSkill);
//				}
//			}
//			efficiency *= multiplier;
//
//			if (handSwingTimer && power.getUser() instanceof Player) {
//				float swingStrengthScale = ((Player) power.getUser()).getAttackStrengthScale(1);
//				efficiency *= (0.2F + swingStrengthScale * swingStrengthScale * 0.8F);
//			}
//
//			float stab = getBreathStability();
//			float maxStab = getMaxBreathStability();
//			if (stab < maxStab) {
//				efficiency *= 0.5F + 0.5F * stab / maxStab;
//			}
//		}
//
//		return efficiency;
//	}
//
//	@Nullable
//	public <T> T consumeHamonEnergyTo(Function<Float, T> actionWithHamonEfficiency, float energyCost, @Nullable AbstractHamonSkill usedSkill) {
//		float efficiency = getActionEfficiency(energyCost, false, usedSkill);
//		if (efficiency > 0) {
//			T result = actionWithHamonEfficiency.apply(efficiency);
//			getHamonEnergyUsageEfficiency(energyCost, true);
//			return result;
//		}
//		return null;
//	}
//
//
//	private boolean isUserWearingBreathMask() {
//		ItemStack headItem = power.getUser().getItemBySlot(EquipmentSlotType.HEAD);
//		return !headItem.isEmpty() && headItem.getItem() == ModItems.BREATH_CONTROL_MASK.get();
//	}
//
//	private void outOfBreath(boolean mask) {
//		power.getUser().setAirSupply(0);
//		serverPlayer.ifPresent(player -> {
//			PacketManager.sendToClient(new HamonUiEffectPacket(
//					mask ? HamonUiEffectPacket.Type.OUT_OF_BREATH_MASK : HamonUiEffectPacket.Type.OUT_OF_BREATH), player);
//		});
//	}
//
//	private float reduceEnergyConsumed(float amount, INonStandPower power, LivingEntity user) {
//		if (user.getItemBySlot(EquipmentSlotType.HEAD).getItem() == ModItems.SATIPOROJA_SCARF.get()) {
//			amount *= 0.6F;
//		}
//		return amount;
//	}
//
//	public float getBloodstreamEfficiency() {
//		float efficiency = 1;
//		LivingEntity user = power.getUser();
//
//		float bleeding = 0;
//		EffectInstance bleedingEffect = user.getEffect(ModStatusEffects.BLEEDING.get());
//		if (bleedingEffect != null) {
//			bleeding = Math.min((bleedingEffect.getAmplifier() + 1) * 0.2F, 0.8F);
//		}
//		efficiency *= (1F - bleeding);
//
//		float freeze = 0;
//		EffectInstance freezeEffect = user.getEffect(ModStatusEffects.FREEZE.get());
//		if (freezeEffect != null) {
//			freeze = Math.min((freezeEffect.getAmplifier() + 1) * 0.25F, 1);
//		}
//		freeze = Math.max(ModInteractionUtil.getEntityFreeze(user), freeze);
//		efficiency *= (1F - freeze);
//
//		return efficiency;
//	}
//
//
//
//	@Override
//	public boolean isActionUnlocked(Ability action, PlayerPower powerData) {
//		return action == ModHamonActions.HAMON_OVERDRIVE.get()
//				|| action == ModHamonActions.HAMON_BEAT.get()
//				|| action == ModHamonActions.HAMON_HEALING.get()
//				|| action == ModHamonActions.HAMON_BREATH.get()
//				|| action == ModHamonActions.CAESAR_BUBBLE_CUTTER_GLIDING.get()
//				|| hamonSkills.isUnlockedFromSkills(action);
//	}
//
//	@Override
//	public void onPowerGiven(NonStandPowerType<?> oldType, TypeSpecificData oldData) {
//		hamonSkills.addSkill(ModHamonSkills.OVERDRIVE.get());
//		hamonSkills.addSkill(ModHamonSkills.HEALING.get());
//		breathStability = getMaxBreathStability();
//		prevBreathStability = breathStability;
//		super.onPowerGiven(oldType, oldData);
//	}
//
//
//
//	private boolean isBeingSuffocated;
//	private void tickAirSupply(LivingEntity user) {
//		if (!isBeingSuffocated) {
//			int air = user.getAirSupply();
//			if (air < user.getMaxAirSupply() - 1 && air > 0) {
//				int airRegainChancePerc = (int) (getBreathingLevel() * getBreathStability() / getMaxBreathStability()) - 1;
//				if (user.tickCount % 100 < airRegainChancePerc) {
//					user.setAirSupply(air + 1);
//				}
//			}
//		}
//		isBeingSuffocated = false;
//
//		if (user.getAirSupply() <= -19) {
//			reduceBreathStability(0);
//		}
//	}
//
//	public void suffocateTick(float suffocationSpeed) {
//		reduceBreathStability(Math.max(getBreathStability() - getMaxBreathStability() * suffocationSpeed, 1));
//		this.isBeingSuffocated = true;
//	}
//
//	public void tcsa(boolean tcsa) {
//		this.tcsa = tcsa;
//	}
//
//
//
//	@Override
//	public void syncWithUserOnly(ServerPlayer user) {
//		giveBreathingTrainingBuffs(user);
//		updateExerciseAttributes(user);
//		hamonSkills.syncWithUser(user, this);
//		PacketManager.sendToClient(HamonExercisesPacket.allData(this), user);
//		PacketManager.sendToClient(new HamonSyncOnLoadPacket(ticksMaskWithNoHamonBreath), user);
//		ModCriteriaTriggers.HAMON_STATS.get().trigger(user, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
//	}
//
//	public void handleSyncPacket(HamonSyncOnLoadPacket packet) {
//		this.ticksMaskWithNoHamonBreath = packet.ticksMaskWithNoHamonBreath;
//	}
//
//	@Override
//	public void syncWithTrackingOrUser(LivingEntity user, ServerPlayer entity) {
//		PacketManager.sendToClient(new TrHamonStatsPacket(
//				user.getId(), false, getHamonStrengthPoints(), getHamonControlPoints(), getBreathingLevel()), entity);
//		PacketManager.sendToClient(new TrHamonBreathStabilityPacket(user.getId(), getBreathStability(), ticksNoBreathStabilityInc), entity);
//		PacketManager.sendToClient(new TrHamonEnergyTicksPacket(user.getId(), noEnergyDecayTicks), entity);
//		hamonSkills.syncWithTrackingOrUser(user, entity, this);
//		PacketManager.sendToClient(new TrHamonAuraColorPacket(user.getId(), auraColor), entity);
//		PacketManager.sendToClient(new TrHamonProtectionPacket(user.getId(), this), entity);
//		PacketManager.sendToClient(new TrHamonMeditationPacket(user.getId(), isMeditating()), entity);
//	}
//
//	public boolean toggleHamonProtection() {
//		setHamonProtection(!hamonProtection);
//		return hamonProtection;
//	}
//
//	public void setHamonProtection(boolean isEnabled) {
//		if (this.hamonProtection != isEnabled) {
//			this.hamonProtection = isEnabled;
//			LivingEntity user = power.getUser();
//			if (!user.level.isClientSide()) {
//				PacketManager.sendToClientsTrackingAndSelf(new TrHamonProtectionPacket(user.getId(), this), user);
//			}
//		}
//	}
//
//	public boolean isProtectionEnabled() {
//		return hamonProtection;
//	}
//
//	private void tickHamonProtection(LivingEntity user) {
//		if (hamonProtection) {
//			HamonSparksLoopSound.playSparkSound(user, user.getBoundingBox().getCenter(), 1.0F, 1);
//			CustomParticlesHelper.createHamonSparkParticles(user, 
//					user.getRandomX(0.5), user.getRandomY(), user.getRandomZ(0.5), 
//					(int) (MathUtil.fractionRandomInc(1) * 2));
//		}
//	}
//
//
//	public void setWaterWalkingThisTick() {
//		waterWalkingThisTick = true;
//	}
//
//	public void postTickWaterWalking(LivingEntity user) {
//		if (!user.level().isClientSide()) {
//			if (waterWalkingPrevTick ^ waterWalkingThisTick) {
//				PacketDistributor.sendToPlayersTracking(new TrHamonLiquidWalkingPacket(user.getId(), waterWalkingThisTick), user);
//			}
//		}
//		else {
//			if (!waterWalkingPrevTick && waterWalkingThisTick || clLargeSpark) {
//				HamonUtil.emitHamonSparkParticles(user.level, ClientUtil.getClientPlayer(), user.position(), 0.05F);
//				CustomParticlesHelper.createHamonSparkParticles(null, user.position(), 10);
//				clTickSpark = false;
//				clLargeSpark = false;
//			}
//		}
//		waterWalkingPrevTick = waterWalkingThisTick;
//
//		if (user.level.isClientSide() && (trWaterWalking || waterWalkingThisTick) && clTickSpark) {
//			HamonSparksLoopSound.playSparkSound(user, user.position(), 1.0F);
//			CustomParticlesHelper.createHamonSparkParticles(user, 
//					user.getRandomX(0.5), user.getY(Math.random() * 0.1), user.getRandomZ(0.5), 
//					1);
//		}
//		clTickSpark = true;
//	}
//
//	public void trSetWaterWalking(boolean waterWalking, LivingEntity user) {
//		this.trWaterWalking = waterWalking;
//		if (waterWalking) {
//			clLargeSpark = true;
//		}
//	}
//
//	public boolean isWaterWalking() {
//		return trWaterWalking || waterWalkingPrevTick;
//	}
//
//	public float waterWalkingTickCost() {
//		return waterWalkingPrevTick ? 1 : 50;
//	}






	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);

//		nbt.putInt("StrengthPoints", hamonStrengthPoints);
//		nbt.putInt("ControlPoints", hamonControlPoints);
//		nbt.putFloat("PointsIncFrac", pointsIncFrac);
//		nbt.putFloat("BreathingTechnique", breathingTrainingLevel);
//		nbt.put("Skills", hamonSkills.toNBT());
//		CompoundNBT exercises = new CompoundNBT();
//		for (Exercise exercise : Exercise.values()) {
//			exercises.putInt(exercise.toString(), Math.min(exerciseTicks.get(exercise), exercise.getMaxTicks(this)));
//		}
//		nbt.put("Exercises", exercises);
//		nbt.putFloat("TrainingBonus", breathingTrainingDayBonus);
//		nbt.putFloat("CanSkipDays", canSkipTrainingDays);
//		nbt.putFloat("BreathStability", breathStability);
//		nbt.putInt("EnergyTicks", noEnergyDecayTicks);
//		nbt.putInt("MaskNoBreathTicks", ticksMaskWithNoHamonBreath);
//		nbt.putInt("NoBreathIncTicks", ticksNoBreathStabilityInc);
		
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
//		hamonStrengthPoints = nbt.getInt("StrengthPoints");
//		hamonStrengthLevel = levelFromPoints(hamonStrengthPoints);
//		hamonControlPoints = nbt.getInt("ControlPoints");
//		hamonControlLevel = levelFromPoints(hamonControlPoints);
//		pointsIncFrac = nbt.getFloat("PointsIncFrac");
//		breathingTrainingLevel = nbt.getFloat("BreathingTechnique");
//		recalcHamonDamage();
//		hamonSkills.fromNbt(nbt.getCompound("Skills"));
//		CompoundNBT exercises = nbt.getCompound("Exercises");
//		int[] exercisesNbt = new int[Exercise.values().length];
//		for (Exercise exercise : Exercise.values()) {
//			exercisesNbt[exercise.ordinal()] = exercises.getInt(exercise.toString());
//		}
//		setExerciseTicks(exercisesNbt, false);
//		breathingTrainingDayBonus = nbt.getFloat("TrainingBonus");
//		canSkipTrainingDays = nbt.getInt("CanSkipDays");
//		breathStability = nbt.contains("BreathStability") ? nbt.getFloat("BreathStability") : getMaxBreathStability();
//		prevBreathStability = breathStability;
//		noEnergyDecayTicks = nbt.getInt("EnergyTicks");
//		ticksMaskWithNoHamonBreath = nbt.getInt("MaskNoBreathTicks");
//		ticksNoBreathStabilityInc = nbt.getInt("NoBreathIncTicks");
	}

	@Override
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {

	}

	@Override
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {

	}

}
