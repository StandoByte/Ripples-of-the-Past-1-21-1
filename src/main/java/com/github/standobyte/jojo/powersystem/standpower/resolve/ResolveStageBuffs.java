package com.github.standobyte.jojo.powersystem.standpower.resolve;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.core.ModEntityAttributes;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.google.common.collect.ImmutableMap;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ResolveStageBuffs {
	
	public static boolean ignoreStaminaDebuff(LivingEntity standUser) {
		return standUser != null && ResolveModeEffect.getResolveEffectLvl(standUser) >= 0;
	}
	
	public static float finisherGainMultiplier(LivingEntity standUser) {
		if (standUser != null && ResolveModeEffect.getResolveEffectLvl(standUser) >= 0) {
			return 2;
		}
		return 1;
	}
	
	public static boolean keepResolveModeAtHalfPassively(StandPower standPower, ResolveCounter resolve) {
		return resolve.passedLastStageUnlock() && standPower.isSummoned();
	}

	
	
	public static boolean getsDamageResFromResolve(LivingEntity entity) {
    	PlayerPower playerPower = PlayerPower.get(entity);
    	return !(playerPower != null && playerPower.getPowerType() == ModPlayerPowers.VAMPIRISM.get());
	}
	
	public static float getDamageResistance(StandPower stand, ResolveCounter resolve, LivingEntity user) {
    	if (!stand.usesResolve() || !getsDamageResFromResolve(user)) return 0;
    	
    	int unlockedStage = resolve.getUnlockedStage();
    	if (unlockedStage == 2) {
    		return 0.4f * resolve.getResolveBarFill();
    	}
    	else if (unlockedStage >= 3) {
    		return 0.6f * resolve.getResolveBarFill();
    	}
    	
        return 0;
	}
	
	
	
	public static Map<Holder<Attribute>, ResourceLocation> EFFECT_MODIFIER_IDS = Util.make(new HashMap<>(), map -> {
		map.put(ModEntityAttributes.STAND_STRENGTH, JojoMod.resLoc("resolve_str"));
		map.put(ModEntityAttributes.STAND_SPEED, JojoMod.resLoc("resolve_spd"));
		map.put(ModEntityAttributes.STAND_DURABILITY, JojoMod.resLoc("resolve_dur"));
		map.put(ModEntityAttributes.STAND_PRECISION, JojoMod.resLoc("resolve_prc"));
	});
	public static AttributeModifier makeModifier(Holder<Attribute> attribute, double addValue) {
		return new AttributeModifier(EFFECT_MODIFIER_IDS.get(attribute), addValue, AttributeModifier.Operation.ADD_VALUE);
	}
	public static Map<Holder<Attribute>, AttributeModifier> EFFECTS_II = new ImmutableMap.Builder<Holder<Attribute>, AttributeModifier>()
			.put(ModEntityAttributes.STAND_DURABILITY, makeModifier(ModEntityAttributes.STAND_DURABILITY, 3))
			.build();
	public static Map<Holder<Attribute>, AttributeModifier> EFFECTS_III = new ImmutableMap.Builder<Holder<Attribute>, AttributeModifier>()
			.put(ModEntityAttributes.STAND_DURABILITY, makeModifier(ModEntityAttributes.STAND_DURABILITY, 6))
			.build();
	public static Map<Holder<Attribute>, AttributeModifier> EFFECTS_IV = new ImmutableMap.Builder<Holder<Attribute>, AttributeModifier>()
			.putAll(EFFECTS_III)
			.put(ModEntityAttributes.STAND_STRENGTH, makeModifier(ModEntityAttributes.STAND_STRENGTH, 3))
			.put(ModEntityAttributes.STAND_SPEED, makeModifier(ModEntityAttributes.STAND_SPEED, 3))
			.put(ModEntityAttributes.STAND_PRECISION, makeModifier(ModEntityAttributes.STAND_PRECISION, 3))
			.build();
	
	@Nullable
	public static Map<Holder<Attribute>, AttributeModifier> getModifiers(int resolveEffect) {
		if (resolveEffect < 1) return null;
		return switch (resolveEffect) {
			case 1 -> EFFECTS_II;
			case 2 -> EFFECTS_III;
			default -> EFFECTS_IV;
		};
	}
	
	public static boolean maxRangeIsEffectiveRange(LivingEntity standUser) {
		return standUser != null && ResolveModeEffect.getResolveEffectLvl(standUser) >= 3;
	}
	
}
