package com.github.standobyte.jojo.mechanics.sunburn;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.util.functions.AttributeUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class SunBurn {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingTick(EntityTickEvent.Pre event) {
		Entity entity = event.getEntity();
		if (!entity.level().isClientSide() && entity instanceof LivingEntity living) {
			tickSunDamage(living);
		}
	}
	
	public static void tickSunDamage(LivingEntity entity) {
		if (entity.invulnerableTime <= 10) {
			double sunBurnDamage = AttributeUtil.getValueOrDefault(entity, ModEntityAttributes.SUN_BURN_DAMAGE, 0);
			if (sunBurnDamage > 0) {
				float ratio = getSunDamageRatio(entity);
				if (ratio > 0) {
					float damage = (float) (sunBurnDamage * ratio);
					if (entity.hurt(entity.damageSources().source(ModDamageTypes.VAMPIRE_SUN_BURN), damage)) {
						incSunBurn(entity, ratio);
					}
				}
			}
		}
	}
	
	public static void incSunBurn(LivingEntity entity, float tickUpAmount) {
		MobEffectInstance sunBurnEffect = entity.getEffect(ModStatusEffects.VAMPIRE_SUN_BURN);
		int duration;
		int amplifier;
		if (sunBurnEffect == null) {
			duration = (int) (60 * tickUpAmount);
			amplifier = (int) (tickUpAmount - 1);
		}
		else {
			duration = sunBurnEffect.getDuration() + Math.max((int) (30 * tickUpAmount), 2);
			amplifier = duration / 60;
		}
		
		duration = Math.max(duration, 11);
		SunBurnStatusEffect.giveEffectTo(entity, duration, amplifier);
	}
	
	public static final int DAY_LENGTH = 24000;
	
	public static final int TIME_SUNRISE_START = -540; // (23460)
	public static final int TIME_SUNRISE_END = 60;
	public static final int SUNRISE_DURATION = TIME_SUNRISE_END - TIME_SUNRISE_START; // 600
	
	public static final int TIME_SUNSET_START = 11940;
	public static final int TIME_SUNSET_END = 12540;
	public static final int SUNSET_DURATION = TIME_SUNSET_END - TIME_SUNSET_START; // 600
	
	private static float getSunDamageRatio(LivingEntity entity) {
		Level level = entity.level();
		if (isSunny(level)) {
			float brightness = entity.getLightLevelDependentMagicValue();
			BlockPos blockPos = BlockPos.containing(entity.getX(), (double)Math.round(entity.getY(1.0)), entity.getZ());
			if (entity.getVehicle() instanceof Boat) {
				blockPos = blockPos.above();
			}
			if (brightness > 0.5F && level.canSeeSky(blockPos)) {
				int time = (int) (level.getDayTime() % (long) DAY_LENGTH);
				
				// -540 ~ 23459
				if (time >= TIME_SUNRISE_START + DAY_LENGTH) { 
					time -= DAY_LENGTH;
				}
				
				// sunrise
				if (time <= TIME_SUNRISE_END) {
					return (float) (time + - TIME_SUNRISE_START) / 600F;
				}
				// day
				else if (time <= TIME_SUNSET_START) {
					return 1;
				}
				// sunset
				else if (time <= TIME_SUNSET_END) {
					return 1 - (float) (time - TIME_SUNSET_START) / SUNSET_DURATION;
				}
				// night
				else {
					return 0;
				}
			}
		}
		return 0;
	}
	
	public static boolean isSunny(Level level) {
		if (level.isClientSide()) {
			level.updateSkyBrightness();
		}
		return level.dimensionType().hasSkyLight()
				&& !level.dimensionType().hasCeiling()
				&& level.isDay()
				&& !level.isRaining()
				&& !level.isThundering();
	}
}
