package com.github.standobyte.jojo.customobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public class StatusEffectModified extends MobEffect {
	public boolean isUncurable;
	public boolean disableCreeperLinger;

	public StatusEffectModified(MobEffectCategory category, int color) {
		super(category, color);
	}
	
	public StatusEffectModified(MobEffectCategory category, int color, ParticleOptions particle) {
		super(category, color, particle);
	}
	

	public void onAdded(LivingEntity entity, MobEffectInstance instance, @Nullable Entity source) {}
	
	public void onUpdated(LivingEntity entity, MobEffectInstance instance, @Nullable Entity source) {}

	public void onRemoved(LivingEntity entity, MobEffectInstance instance) {}
	

	@Deprecated
	@SuppressWarnings("unchecked")
	public <T extends StatusEffectModified> T setUncurable() {
		this.isUncurable = true;
		return (T) this;
	}

	@Override
	public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
		if (!isUncurable) {
			super.fillEffectCures(cures, effectInstance);
		}
	}


	@EventBusSubscriber(modid = JojoMod.MOD_ID)
	public static class EventHandler {
	
		@SubscribeEvent
		public static void disableCreeperLingeringClouds(ExplosionEvent.Detonate event) {
			Explosion explosion = event.getExplosion();
			if (explosion.getDirectSourceEntity() instanceof Creeper creeper) {
				Collection<Holder<MobEffect>> effects = new ArrayList<>(creeper.getActiveEffectsMap().keySet());
				effects.forEach(effect -> {
					if (effect.value() instanceof StatusEffectModified modEffect && modEffect.disableCreeperLinger) {
						creeper.removeEffect(effect);
					}
				});
			}
		}
	}

}
