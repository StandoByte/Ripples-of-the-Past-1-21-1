package com.github.standobyte.jojo.customobjects;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.AttributeUtil;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public interface DamageSourceModified {
	void jojo_ripples$modifyKnockback(float add, float multiply);
	void jojo_ripples$verticalKnockback(float strength, float angleRatio);
	
	float jojo_ripples$knockbackMultiplier();
	float jojo_ripples$addKnockback();
	float jojo_ripples$verticalKnockbackStrength();
	float jojo_ripples$verticalKnockbackAngleRatio();
	
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void _onKnockbackEvent(LivingKnockBackEvent event) {
		LivingEntity target = event.getEntity();
		if (!target.damageContainers.isEmpty()) {
			DamageContainer curDamage = target.damageContainers.peek();
			DamageSource dmgSource = curDamage.getSource();
			if (dmgSource instanceof DamageSourceModified kbModifier) {
				event.setStrength((event.getStrength() + kbModifier.jojo_ripples$addKnockback()) * kbModifier.jojo_ripples$knockbackMultiplier());
			}
		}
	}
	
	public static void afterKnockbackApplied(LivingEntity target, @Nullable DamageSource dmgSource) {
		if (dmgSource instanceof DamageSourceModified kbModifier) {
			float vertical = kbModifier.jojo_ripples$verticalKnockbackStrength();
			if (vertical > 0) {
				float angle = (float) Math.PI / 2 * kbModifier.jojo_ripples$verticalKnockbackAngleRatio();
				if (angle > 0) {
					double kbRes = AttributeUtil.getValueOrDefault(target, Attributes.KNOCKBACK_RESISTANCE);
					if (kbRes < 1) {
						vertical *= (1 - kbRes);
						Vec3 movement = target.getDeltaMovement();

						float sin = Mth.sin(angle);
						float cos = Mth.cos(angle);

						Vec3 newMovement = new Vec3(movement.x * cos, movement.y + vertical * sin, movement.z * cos);

						target.setDeltaMovement(newMovement);
						target.hurtMarked = true;
					}
				}
			}
		}
	}
	
}
