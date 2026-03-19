package com.github.standobyte.jojo.powersystem.standpower.resolve;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.mc.statuseffect.RotpStatusEffect;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ResolveModeEffect extends RotpStatusEffect {

	public ResolveModeEffect(MobEffectCategory category, int color) {
		super(category, color);
		isUncurable = true;
		disableCreeperLinger = true;
	}

	@Override
	public void onAdded(LivingEntity entity, MobEffectInstance instance, @Nullable Entity source) {
		super.onAdded(entity, instance, source);
		StandPower standPower = StandPower.get(entity);
		if (standPower != null && standPower.usesResolve()) {
			standPower.resolveCounter.onResolveEffectStart(standPower, entity, instance);
			addStandAttributeModifiers(entity, instance.getAmplifier());
		}
	}

	@Override
	public void onUpdated(LivingEntity entity, MobEffectInstance instance, @Nullable Entity source) {
		super.onUpdated(entity, instance, source);
		StandPower standPower = StandPower.get(entity);
		if (standPower != null && standPower.usesResolve()) {
			standPower.resolveCounter.onResolveEffectStart(standPower, entity, instance);
			addStandAttributeModifiers(entity, instance.getAmplifier());
		}
	}

	@Override
	public void onRemoved(LivingEntity entity, MobEffectInstance instance) {
		super.onRemoved(entity, instance);
		StandPower standPower = StandPower.get(entity);
		if (standPower != null) {
			standPower.resolveCounter.onResolveEffectEnd(standPower, entity, instance);
			removeStandAttributeModifiers(entity);
		}
	}
	
	
	public void addStandAttributeModifiers(LivingEntity user, int effectLvl) {
		if (!user.level().isClientSide()) {
			var modifiers = ResolveStageBuffs.getModifiers(effectLvl);
			if (modifiers != null) {
				AttributeMap attributeMap = user.getAttributes();
				for (var modifierEntry : modifiers.entrySet()) {
					AttributeInstance attribute = attributeMap.getInstance(modifierEntry.getKey());
					if (attribute != null) {
						AttributeModifier modifier = modifierEntry.getValue();
		                attribute.removeModifier(modifier.id());
		                attribute.addPermanentModifier(modifier);
					}
				}
				//user.refreshDirtyAttributes();
			}
		}
	}
	
	public void removeStandAttributeModifiers(LivingEntity user) {
		if (!user.level().isClientSide()) {
			AttributeMap attributeMap = user.getAttributes();
			for (var modifierEntry : ResolveStageBuffs.EFFECT_MODIFIER_IDS.entrySet()) {
				AttributeInstance attribute = attributeMap.getInstance(modifierEntry.getKey());
				if (attribute != null) {
					attribute.removeModifier(modifierEntry.getValue());
				}
			}
			//user.refreshDirtyAttributes();
		}
	}
	

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
//		if (!entity.isInvisible()) {
//			entity.level().addParticle(ModParticles.RESOLVE.get(), 
//					entity.getRandomX(2.5D), entity.getY(entity.getRandom().nextDouble() * 1.5), entity.getRandomZ(2.5D), 0, 0, 0);
//		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 3 == 0;
	}
	
	
	public static void addAttributes(LivingEntity entity, Map<Holder<Attribute>, AttributeModifier> attributeModifiers) {
		AttributeMap entityAttributes = entity.getAttributes();
		for (var modifierEntry : attributeModifiers.entrySet()) {
			AttributeInstance attribute = entityAttributes.getInstance(modifierEntry.getKey());
			if (attribute != null) {
				attribute.removeModifier(modifierEntry.getValue().id());
				attribute.addPermanentModifier(modifierEntry.getValue());
			}
		}
	}



	public static final Set<Holder<? extends MobEffect>> RESOLVE_EFFECTS = new HashSet<>();
	@SubscribeEvent
	public static void afterRegister(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			RESOLVE_EFFECTS.add(ModStatusEffects.RESOLVE);
		});
	}
	
	@Nullable
	public static MobEffectInstance maxDurationResolveEffect(LivingEntity entity) {
		return entity.getActiveEffectsMap().entrySet().stream()
				.filter(effect -> RESOLVE_EFFECTS.contains(effect.getKey()))
				.max(Comparator.comparingInt(effect -> effect.getValue().getDuration()))
				.map(Map.Entry::getValue)
				.orElse(null);
	}
	
	// XXX (resolve notes) incapacitation - get up after N seconds during resolve
	// XXX (resolve notes) better combat AI during resolve
	// XXX (resolve notes) Resolve IV "automatic stand protection" (whatever it will end up being, if ever)
	public static int getResolveEffectLvl(LivingEntity entity) {
		return entity.getActiveEffectsMap().entrySet().stream()
				.filter(effect -> RESOLVE_EFFECTS.contains(effect.getKey()))
				.map(Map.Entry::getValue)
				.mapToInt(MobEffectInstance::getAmplifier)
				.max().orElse(-1);
	}

}
