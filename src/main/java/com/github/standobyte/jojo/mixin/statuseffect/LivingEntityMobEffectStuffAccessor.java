package com.github.standobyte.jojo.mixin.statuseffect;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityMobEffectStuffAccessor {
	@Invoker("onEffectUpdated") void invokeOnEffectUpdated(MobEffectInstance effectInstance, boolean forced, @Nullable Entity entity);
}
