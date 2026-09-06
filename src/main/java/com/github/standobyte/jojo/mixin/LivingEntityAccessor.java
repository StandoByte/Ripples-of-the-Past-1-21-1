package com.github.standobyte.jojo.mixin;

import java.util.Map;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor("attributes") public AttributeMap getAttributes();
	@Accessor("attributes") @Mutable public void setAttributes(AttributeMap attributes);
	
	@Accessor("combatTracker") public CombatTracker getCombatTracker();
	@Accessor("combatTracker") @Mutable public void setCombatTracker(CombatTracker combatTracker);
	
	@Accessor("activeEffects") public Map<Holder<MobEffect>, MobEffectInstance> getActiveEffects();
	@Accessor("activeEffects") @Mutable public void setActiveEffects(Map<Holder<MobEffect>, MobEffectInstance> activeEffects);
	
	@Invoker("onEffectUpdated") void invokeOnEffectUpdated(MobEffectInstance effectInstance, boolean forced, @Nullable Entity entity);
	
	@Invoker("getHurtSound") SoundEvent invokeGetHurtSound(DamageSource damageSource);
	@Invoker("getSoundVolume") float invokeGetSoundVolume();
	@Accessor("lastDamageSource") @Mutable public void setLastDamageSource(DamageSource lastDamageSource);
	@Accessor("lastDamageStamp") @Mutable public void setLastDamageStamp(long lastDamageStamp);
}
