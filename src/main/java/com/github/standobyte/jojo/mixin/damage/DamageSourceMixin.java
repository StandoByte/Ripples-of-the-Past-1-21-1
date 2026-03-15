package com.github.standobyte.jojo.mixin.damage;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.customobjects.DamageSourceModified;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceModified {
	private float jojo_ripples$addKnockback = 0;
	private float jojo_ripples$knockbackMultiplier = 1;
	private float jojo_ripples$verticalKnockbackStrength = 0;
	private float jojo_ripples$verticalKnockbackAngleRatio = 0;
	
	@Override
	public void jojo_ripples$modifyKnockback(float add, float multiply) {
		this.jojo_ripples$addKnockback = add;
		this.jojo_ripples$knockbackMultiplier = multiply;
	}
	
	@Override
	public void jojo_ripples$verticalKnockback(float strength, float angleRatio) {
		this.jojo_ripples$verticalKnockbackStrength = Mth.clamp(strength, 0, 1);
		this.jojo_ripples$verticalKnockbackAngleRatio = Mth.clamp(angleRatio, 0, 1);
	}
	
	@Override
	public float jojo_ripples$knockbackMultiplier() {
		return jojo_ripples$knockbackMultiplier;
	}
	
	@Override
	public float jojo_ripples$addKnockback() {
		return jojo_ripples$addKnockback;
	}
	
	@Override
	public float jojo_ripples$verticalKnockbackStrength() {
		return jojo_ripples$verticalKnockbackStrength;
	}
	
	@Override
	public float jojo_ripples$verticalKnockbackAngleRatio() {
		return jojo_ripples$verticalKnockbackAngleRatio;
	}
}
