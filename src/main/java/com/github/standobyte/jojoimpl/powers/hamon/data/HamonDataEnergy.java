package com.github.standobyte.jojoimpl.powers.hamon.data;

import com.github.standobyte.jojo.util.objects_java.Lerp;
import com.github.standobyte.jojoimpl.powers.hamon.HamonData;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class HamonDataEnergy {
	public final HamonData hamon;

	public HamonDataEnergy(HamonData hamon) {
		this.hamon = hamon;
	}

	public Lerp.FloatValue energyAmount = new Lerp.FloatValue();
	
	public boolean isHamonBreathing;
	public int breathTicks;
	
	public Lerp.FloatValue _maxEnergy = new Lerp.FloatValue();
	public int maxAmountIncreaseNoDecayTime;

	public static final float ENERGY_TICK_DOWN_AMOUNT = 20;
	
	public float getEnergy() {
		return energyAmount.get();
	}
	
	public float getCurMaxEnergy() {
		return _maxEnergy.get();
	}
	
	public float getMaxEnergyPassive() {
		return 2500;
	}
	
	public float getMaxEnergyPossible() {
		return 5000;
	}
	
	
	public void startHamonBreath() {
		this.isHamonBreathing = true;
		this.maxAmountIncreaseNoDecayTime = 0;
	}
	
	public void stopHamonBreath() {
		this.isHamonBreathing = false;
		this.breathTicks = 0;
	}
	
	public void tick(LivingEntity user) {
		float FULL_BAR_GAIN_TIME = 200;
		int HAMON_BREATH_MAX_TICKS = 80;
		int KEEP_BREATH_BUFF_TIME_MULTIPLIER = 4;
		int BREATH_BUFF_WEAR_OFF_DURATION = 10;
		
		float curEnergy = energyAmount.get();
		int _air = user.getAirSupply();
		if (_air <= 0) {
			_maxEnergy.set(0, true);
			energyAmount.set(0, true);
			isHamonBreathing = false;
			breathTicks = 0;
			maxAmountIncreaseNoDecayTime = -BREATH_BUFF_WEAR_OFF_DURATION;
		}
		else {
			int _maxAir = user.getMaxAirSupply();
			boolean atFullAir = _air >= _maxAir;
			//float airRatio = !atFullAir ? (float) _air / (float) _maxAir : 1;
			
			float maxPassive = getMaxEnergyPassive();
			float maxPossible = getMaxEnergyPossible();
			
			float energyLimit = isHamonBreathing ? maxPossible : maxPassive;
			if (maxAmountIncreaseNoDecayTime > -BREATH_BUFF_WEAR_OFF_DURATION) {
				float buffRatio = Mth.clamp(1 + (float) maxAmountIncreaseNoDecayTime / BREATH_BUFF_WEAR_OFF_DURATION, 0, 1);
				float curMax = maxPassive + (maxPossible - maxPassive) * buffRatio;
				energyLimit = Mth.clamp(curEnergy, energyLimit, curMax);
			}
			
			if (atFullAir) {
				float energyGain = maxPossible / FULL_BAR_GAIN_TIME;
				if (isHamonBreathing && breathTicks >= 0 && curEnergy < maxPassive) {
					energyGain *= 2;
				}
				curEnergy = Mth.clamp(curEnergy + energyGain, 0, energyLimit);
			}
			
			float max = Mth.clamp(curEnergy, maxPassive, maxPossible);
			_maxEnergy.set(max, true);
			energyAmount.set(Math.min(curEnergy, max), true);
			
			if (isHamonBreathing) {
				breathTicks = Math.min(breathTicks + 1, HAMON_BREATH_MAX_TICKS);
				maxAmountIncreaseNoDecayTime = Math.min(maxAmountIncreaseNoDecayTime + KEEP_BREATH_BUFF_TIME_MULTIPLIER,
						HAMON_BREATH_MAX_TICKS * KEEP_BREATH_BUFF_TIME_MULTIPLIER);
			}
			else if (maxAmountIncreaseNoDecayTime > -BREATH_BUFF_WEAR_OFF_DURATION) {
				maxAmountIncreaseNoDecayTime--;
			}
		}
	}

	
//	public int noEnergyDecayTicks = 0;
//	public boolean playedEnergySound = false;
//	public float breathStability;
//	public float prevBreathStability;
//	public int ticksMaskWithNoHamonBreath;
//	public int ticksNoBreathStabilityInc;
//	public int prevAir = 300;
//	
//	public float tickEnergy(PlayerPower power) {
//		LivingEntity user = power.getUser();
//		if (JojoModUtil.isDyingBody(user)) {
//			return 0;
//		}
//		if (power.getHeldAction() == ModHamonActions.HAMON_BREATH.get() && user.getAirSupply() >= user.getMaxAirSupply()) {
//			return power.getEnergy() + tickHamonBreath(ModHamonActions.HAMON_BREATH.get());
//		}
//		else {
//			if (isUserWearingBreathMask() && !isMeditating()) {
//				ticksMaskWithNoHamonBreath++;
//			}
//			else {
//				ticksMaskWithNoHamonBreath = 0;
//			}
//
//			if (power.getEnergy() <= 0) {
//				setHamonProtection(false);
//			}
//
//			playedEnergySound = false;
//			if (noEnergyDecayTicks > 0) {
//				noEnergyDecayTicks--;
//				return power.getEnergy();
//			}
//			else if (JojoModConfig.getCommonConfigInstance(user.level.isClientSide()).hamonEnergyTicksDown.get()) {
//				return power.getEnergy() - ENERGY_TICK_DOWN_AMOUNT;
//			}
//			else {
//				return power.getEnergy();
//			}
//		}
//	}
//
//	public float tickHamonBreath(PlayerPower power, Ability hamonBreathAbility) {
//		LivingEntity user = power.getUser();
//		ticksMaskWithNoHamonBreath = 0;
//		if (user.level.isClientSide() && power.getEnergy() > 0 && !playedEnergySound) {
//			ClientTickingSoundsHelper.playHamonEnergyConcentrationSound(user, 1.0F, hamonBreathAbility);
//			playedEnergySound = true;
//			if (user == ClientUtil.getClientPlayer()) {
//				BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).resetRedHighlight();
//			}
//		}
//		updateNoEnergyDecayTicks();
//		return getMaxBreathStability() / fullEnergyTicks();
//	}
//
//	public float getMaxEnergy() {
//		return getBreathStability();
//	}
//
//	public float getBreathStability() {
//		return breathStability;
//	}
//
//	public float getPrevBreathStability() {
//		return prevBreathStability;
//	}
//
//	public void reduceBreathStability(float value) {
//		setBreathStability(value, 80);
//	}
//
//	public void setBreathStability(float value, int noIncTicks) {
//		value = Mth.clamp(value, 0, getMaxBreathStability());
//		boolean send = this.breathStability != value;
//		this.breathStability = value;
//		this.prevBreathStability = value;
//		this.ticksNoBreathStabilityInc = Math.max(noIncTicks, this.ticksNoBreathStabilityInc);
//		if (send) {
//			LivingEntity user = power.getUser();
//			if (!user.level.isClientSide()) {
//				PacketManager.sendToClientsTrackingAndSelf(new TrHamonBreathStabilityPacket(
//						user.getId(), getBreathStability(), ticksNoBreathStabilityInc), user);
//			}
//		}
//	}
//
//	public void tickBreathStability(PlayerPower power, HamonData hamon) {
//		LivingEntity user = power.getUser();
//		if (JojoModUtil.isDyingBody(user)) {
//			breathStability = 0;
//			prevBreathStability = 0;
//			return;
//		}
//
//		boolean canBreath = user.getAirSupply() >= user.getMaxAirSupply();
//		float inc;
//		float maxStability = getMaxBreathStability();
//		boolean maskNoBreath = false;
//
//		if (isUserWearingBreathMask()) {
//			float ticksCanBreatheWithMask = 400 + breathingTrainingLevel * 16;
//			float breathMaskHandicap = 0;
//			if (breathingTrainingLevel < MAX_BREATHING_LEVEL) {
//				breathMaskHandicap = Mth.clamp((ticksCanBreatheWithMask - ticksMaskWithNoHamonBreath) / (ticksCanBreatheWithMask / 2), -1, 1);
//			}
//			boolean canIndicateInHud = user.level.isClientSide() && ClientUtil.getClientPlayer() == user;
//			if (canIndicateInHud && breathMaskHandicap == 0) {
//				BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).triggerRedHighlight(4);
//			}
//			// normal recovery, slowed down when not using hamon breath for too long (10s)
//			if (breathMaskHandicap >= 0) {
//				inc = maxStability / fullBreathStabilityTicks() * breathMaskHandicap;
//			}
//			// go down when not using hamon breath for even longer (20s)
//			else {
//				inc = maxStability / 1200 * breathMaskHandicap;
//
//				// do not go down below 20%
//				float stabLowerCap = 0.2F;
//				boolean stabIsReallyLow = (breathStability + inc) / maxStability < stabLowerCap;
//				if (stabIsReallyLow) {
//					inc = Mth.clamp(inc, stabLowerCap * maxStability - breathStability, 0);
//				}
//
//				maskNoBreath = true;
//
//				if (canIndicateInHud) {
//					BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).triggerRedHighlight(999999);
//					if ((ticksMaskWithNoHamonBreath - ticksCanBreatheWithMask > 400 || stabIsReallyLow)
//							&& breathStability + inc > 0
//							&& ModHamonActions.HAMON_BREATH.get().checkConditions(user, power, ActionTarget.EMPTY).isPositive()) {
//						ClientUtil.setOverlayMessage(Component.translatable("hamon.breath_control_mask.restore_stab"));
//					}
//				}
//			}
//		}
//		else {
//			// normal recovery
//			inc = maxStability / fullBreathStabilityTicks();
//		}
//
//		// meditation speeding up the recovery (if there's no mask handicap)
//		if (inc >= 0 && isMeditating() && breathStabilityIncTicks > 0) {
//			inc *= Mth.sqrt((float) Math.min(breathStabilityIncTicks, 100));
//		}
//
//		if (!canBreath) {
//			inc = Math.min(inc, 0);
//		}
//
//		if (inc > 0 && ticksNoBreathStabilityInc > 0) {
//			ticksNoBreathStabilityInc--;
//			inc = 0;
//		}
//		breathStability = Mth.clamp(breathStability + inc, 0, getMaxBreathStability());
//		int air = user.getAirSupply();
//		if (!user.level.isClientSide()) {
//			if (breathStability == 0 && prevBreathStability > 0 || air == 0 && prevAir > 0) {
//				outOfBreath(maskNoBreath && air > 0);
//			}
//		}
//		else if (user == ClientUtil.getClientPlayer()) {
//			if (breathStability >= getMaxBreathStability()) {
//				BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).resetRedHighlight();
//			}
//		}
//		prevBreathStability = breathStability;
//		prevAir = air;
//	}
//
//	private float fullEnergyTicks() {
//		float ticks = 80F - (40F * breathingTrainingLevel / MAX_BREATHING_LEVEL);
//		if (meditationCompleted) {
//			ticks -= MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION;
//		}
//		return ticks;
//	}
//
//	private float fullBreathStabilityTicks() {
//		float ticks = 1200F - (600F * breathingTrainingLevel / MAX_BREATHING_LEVEL);
//		return ticks;
//	}
//
//	public float getMaxBreathStability() {
//		return getMaxBreathStabilityAt(getHamonControlLevel());
//	}
//
//	protected float getMaxBreathStabilityAt(int controlLvl) {
//		float max = NonStandPower.BASE_MAX_ENERGY * (1F + controlLvl * 0.1F);
//		if (swimmingCompleted) {
//			max *= SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER;
//		}
//		return max;
//	}
//
//	public void setNoEnergyDecayTicks(TrHamonEnergyTicksPacket packet) {
//		this.noEnergyDecayTicks = packet.getTicks();
//	}
//
//	private void updateNoEnergyDecayTicks() {
//		noEnergyDecayTicks = 50 + MathUtil.fractionRandomInc(150F * getBreathingLevel() / HamonData.MAX_BREATHING_LEVEL);
//	}
//
//	public float getEnergyRatio() {
//		return power.getEnergy() / getMaxBreathStability();
//	}
//
//
//
//	private static final float NO_ENERGY_EFFICIENCY = 0.5f;
//	private static final float ENERGY_STABILITY_USAGE_RATIO = 2.5F;
//	float getHamonEnergyUsageEfficiency(float energyNeeded, boolean doConsume) {
//		LivingEntity user = power.getUser();
//		doConsume &= !user.level.isClientSide() && !power.isUserCreative();
//		energyNeeded = reduceEnergyConsumed(energyNeeded, power, user);
//
//		if (power.getEnergy() >= energyNeeded || energyNeeded == 0) {
//			if (doConsume) {
//				power.setEnergy(power.getEnergy() - energyNeeded);
//			}
//			return 1;
//		}
//
//		else if (power.getEnergy() > 0) {
//			float energyRatio = power.getEnergy() / energyNeeded;
//			if (doConsume) {
//				power.setEnergy(0);
//			}
//			return NO_ENERGY_EFFICIENCY + (1 - NO_ENERGY_EFFICIENCY) * energyRatio;
//		}
//
//		else {
//			if (doConsume) {
//				serverPlayer.ifPresent(player -> {
//					PacketManager.sendToClient(new HamonUiEffectPacket(HamonUiEffectPacket.Type.NO_ENERGY), player);
//				});
//			}
//
//			float energyFromStability = getBreathStability();
//			if (!isUserWearingBreathMask()) {
//				energyFromStability *= ENERGY_STABILITY_USAGE_RATIO;
//			}
//
//			float energyRatio = Math.min(energyFromStability / energyNeeded, 1);
//			if (doConsume) {
//				if (energyFromStability < energyNeeded) {
//					reduceBreathStability(0);
//					outOfBreath(false);
//				}
//				else {
//					reduceBreathStability((energyFromStability - energyNeeded) / ENERGY_STABILITY_USAGE_RATIO);
//				}
//			}
//			return NO_ENERGY_EFFICIENCY * energyRatio;
//		}
//	}
}
