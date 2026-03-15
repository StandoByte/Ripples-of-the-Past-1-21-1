package com.github.standobyte.jojo.mixin.statuseffect;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.customobjects.StatusEffectModified;
import com.github.standobyte.jojo.init.ModStatusEffects;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class MobEffectLivingEntityMixin extends Entity {

	public MobEffectLivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	// ffs, why did they have to remove the LivingEntity parameter from MobEffect#addAttributeModifiers
	// at least they also added Entity parameter to give context about which entity is giving the effect FeelsOkayMan
	@Inject(method = "onEffectAdded", at = @At("TAIL"))
	public void jojo_ripples$onAddedStatusEffect(MobEffectInstance effect, @Nullable Entity source, CallbackInfo ci) {
		if (!this.level().isClientSide()) {
			LivingEntity asLiving = (LivingEntity) (Entity) this;
			if (effect.getEffect().value() instanceof StatusEffectModified statusEffect) {
				statusEffect.onAdded(asLiving, effect, source);
			}
			ModStatusEffects.trackAddEffect(effect, asLiving);
		}
	}

	@Inject(method = "onEffectUpdated", at = @At("TAIL"))
	public void jojo_ripples$onUpdatedStatusEffect(MobEffectInstance effect, boolean forced, @Nullable Entity source, CallbackInfo ci) {
		if (!this.level().isClientSide()) {
			LivingEntity asLiving = (LivingEntity) (Entity) this;
			if (forced && effect.getEffect().value() instanceof StatusEffectModified statusEffect) {
				statusEffect.onUpdated(asLiving, effect, source);
			}
			ModStatusEffects.trackAddEffect(effect, asLiving);
		}
	}

	@Inject(method = "onEffectRemoved", at = @At("TAIL"))
	public void jojo_ripples$onRemovedStatusEffect(MobEffectInstance effect, CallbackInfo ci) {
		if (!this.level().isClientSide()) {
			LivingEntity asLiving = (LivingEntity) (Entity) this;
			if (effect.getEffect().value() instanceof StatusEffectModified statusEffect) {
				statusEffect.onRemoved(asLiving, effect);
			}
			ModStatusEffects.trackRemoveEffect(effect.getEffect(), asLiving);
		}
	}
}
