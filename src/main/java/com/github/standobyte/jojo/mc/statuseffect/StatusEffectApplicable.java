package com.github.standobyte.jojo.mc.statuseffect;

import static net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result.APPLY;
import static net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result.DO_NOT_APPLY;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojoimpl.JojoDefinitions;
import com.github.standobyte.jojoimpl.JojoModLivingVariables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public interface StatusEffectApplicable {
	boolean isApplicable(LivingEntity entity);

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPotionApply(MobEffectEvent.Applicable event) {
		LivingEntity entity = event.getEntity();
		MobEffectInstance effect = event.getEffectInstance();
		if (JojoModLivingVariables.get(entity).isDyingBody) {
			if (effect.is(MobEffects.HUNGER) || effect.is(MobEffects.POISON) || effect.is(MobEffects.REGENERATION)) {
				event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
			}
		}
		if (entity instanceof Player player && JojoDefinitions.isPlayerJojoVampiric(player)) {
			if (effect.is(MobEffects.HUNGER)/* || effect.is(Effects.POISON) */) {
				event.setResult(DO_NOT_APPLY);
			}
			else if (effect.is(MobEffects.REGENERATION)) {
				event.setResult(APPLY);
			}
		}
		if (effect instanceof StatusEffectApplicable _effect && !_effect.isApplicable(entity)) {
			event.setResult(DO_NOT_APPLY);
		}
	}
}
