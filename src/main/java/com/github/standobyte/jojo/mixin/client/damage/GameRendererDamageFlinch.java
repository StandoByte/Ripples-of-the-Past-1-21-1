package com.github.standobyte.jojo.mixin.client.damage;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojo.customobjects.DamageSourceModified;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@Mixin(GameRenderer.class)
public class GameRendererDamageFlinch {
	@Shadow @Final Minecraft minecraft;

	@ModifyVariable(method = "bobHurt", at = @At(value = "STORE"), ordinal = 3)
	private float reduceCameraDamageTilt(float damageTilt) {
		if (damageTilt != 0) {
			DamageSource lastSrc = ((LivingEntity) this.minecraft.getCameraEntity()).getLastDamageSource();
			if (lastSrc != null) {
				damageTilt *= ((DamageSourceModified) lastSrc).jojo_ripples$getCameraTiltMultiplier();
			}
		}
		return damageTilt;
	}
}
