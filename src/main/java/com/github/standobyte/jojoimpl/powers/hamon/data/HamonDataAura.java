package com.github.standobyte.jojoimpl.powers.hamon.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojoimpl.powers.hamon.HamonData;

import net.minecraft.Util;
import net.minecraft.world.entity.LivingEntity;

public class HamonDataAura {
	public final HamonData hamon;

	private HamonAuraColor auraColor = HamonAuraColor.ORANGE;
	private Ability lastUsedAbility = null;

	public HamonDataAura(HamonData hamon) {
		this.hamon = hamon;
	}

	public enum HamonAuraColor {
		ORANGE,
		BLUE,
		YELLOW,
		RED,
		SILVER
	}

//	private static final Map<HamonAuraColor, Supplier<? extends IParticleData>> PARTICLE_TYPE = Util.make(new HashMap<>(), map -> {
//		map.put(HamonAuraColor.ORANGE, ModParticles.HAMON_AURA);
//		map.put(HamonAuraColor.BLUE, ModParticles.HAMON_AURA_BLUE);
//		map.put(HamonAuraColor.YELLOW, ModParticles.HAMON_AURA_YELLOW);
//		map.put(HamonAuraColor.RED, ModParticles.HAMON_AURA_RED);
//		map.put(HamonAuraColor.SILVER, ModParticles.HAMON_AURA_SILVER);
//	});
//
//	private void tickChargeParticles(LivingEntity user) {
//		if (!user.level().isClientSide() || ClientProxy.getClientPlayer() == user) {
//			HamonAuraColor auraColor = getThisTickAuraColor(user);
//			if (auraColor != this.auraColor) {
//				this.auraColor = auraColor;
//				if (!user.level().isClientSide()) {
//					PacketManager.sendToClientsTracking(new TrHamonAuraColorPacket(user.getId(), auraColor), user);
//				}
//			}
//		}
//		if (user.level().isClientSide()) {
//			float energy = hamon.energy.energy;
//			Action<?> heldAction = power.getHeldAction();
//			if (heldAction instanceof HamonSunlightYellowOverdrive) {
//				if (!power.isUserCreative()) {
//					energy += ((HamonSunlightYellowOverdrive) heldAction).getSpentEnergy(power);
//				}
//				energy *= 2;
//			}
//			float particlesPerTick = energy / getMaxBreathStability() * getHamonDamageMultiplier();
//			boolean isUserTheCameraEntity = user == ClientUtil.getCameraEntity();
//			IParticleData particleType = PARTICLE_TYPE.get(auraColor).get();
//
//			GeneralUtil.doFractionTimes(() -> {
//				CustomParticlesHelper.createHamonAuraParticle(particleType, user, 
//						user.getX() + (random.nextDouble() - 0.5) * (user.getBbWidth() + 0.5F), 
//						user.getY() + random.nextDouble() * (user.getBbHeight() * 0.5F), 
//						user.getZ() + (random.nextDouble() - 0.5) * (user.getBbWidth() + 0.5F));
//			}, particlesPerTick);
//			if (isUserTheCameraEntity) {
//				CustomParticlesHelper.summonHamonAuraParticlesFirstPerson(particleType, user, particlesPerTick / 5);
//			}
//		}
//	}
//
//	private HamonAuraColor getThisTickAuraColor(PlayerPower power, LivingEntity user) {
//		if (power.getHeldAction() == ModHamonActions.HAMON_SUNLIGHT_YELLOW_OVERDRIVE.get()
//				|| power.getHeldAction() == ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get()) {
//			return HamonAuraColor.YELLOW;
//		}
//		if (ContinuousActionInstance.getCurrentAction(user)
//				.map(action -> action.getAction() == ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get())
//				.orElse(false)) {
//			return HamonAuraColor.YELLOW;
//		}
//		if (power.getHeldAction() == ModHamonActions.JONATHAN_SCARLET_OVERDRIVE.get()) {
//			return HamonAuraColor.RED;
//		}
//		if (power.getEnergy() == 0) {
//			lastUsedAbility = null;
//		}
//		else {
//			if (lastUsedAbility == ModHamonActions.HAMON_TURQUOISE_BLUE_OVERDRIVE.get()) {
//				return HamonAuraColor.BLUE;
//			}
//			if (lastUsedAbility == ModHamonActions.HAMON_SUNLIGHT_YELLOW_OVERDRIVE.get()) {
//				return HamonAuraColor.YELLOW;
//			}
//			if (lastUsedAbility == ModHamonActions.JONATHAN_SCARLET_OVERDRIVE.get()) {
//				return HamonAuraColor.RED;
//			}
//			if (lastUsedAbility == ModHamonActions.JONATHAN_METAL_SILVER_OVERDRIVE.get()
//					|| lastUsedAbility == ModHamonActions.JONATHAN_METAL_SILVER_OVERDRIVE_WEAPON.get()) {
//				return HamonAuraColor.SILVER;
//			}
//		}
//
//		if (isSkillLearned(ModHamonSkills.METAL_SILVER_OVERDRIVE.get()) && MCUtil.isItemWeapon(user.getMainHandItem())) {
//			return HamonAuraColor.SILVER;
//		}
//
//		if (isSkillLearned(ModHamonSkills.TURQUOISE_BLUE_OVERDRIVE.get()) && user.isUnderWater()) {
//			return HamonAuraColor.BLUE;
//		}
//
//		return HamonAuraColor.ORANGE;
//	}
//
//	public void setLastUsedAction(@Nullable Ability ability) {
//		this.lastUsedAbility = ability;
//	}
//
//	public void setAuraColor(HamonAuraColor color) {
//		this.auraColor = color;
//	}
}
