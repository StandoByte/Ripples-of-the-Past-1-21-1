package com.github.standobyte.jojo.mixin.damage;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.github.standobyte.jojo.customobjects.DamageSourceModified;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceModified {
	private float jojo_ripples$addKnockback = 0;
	private float jojo_ripples$knockbackMultiplier = 1;
	private float jojo_ripples$verticalKnockbackStrength = 0;
	private float jojo_ripples$verticalKnockbackAngleRatio = 0;
	@Unique private boolean disableKBImpact = false;
	@Unique private Explosion causingExplosion = null;
	
	@Unique private boolean scalesFromStandDamageConfig = false;
	
	@Unique private boolean noHurtSound = false;
	@Unique private boolean noFlinchModelAnim = false;
	@Unique private float cameraTiltMultiplier = 1;
	
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
	public void jojo_ripples$setDisableKBImpact(boolean flag) {
		this.disableKBImpact = flag;
	}
	
	@Override
	public void jojo_ripples$trackCausingExplosion(Explosion explosion) {
		this.causingExplosion = explosion;
	}
	

	@Override
	public void jojo_ripples$setScalesFromStandDamageConfig(boolean flag) {
		this.scalesFromStandDamageConfig = flag;
	}


	@Override
	public void jojo_ripples$setNoHurtSound(boolean flag) {
		this.noHurtSound = flag;
	}

	@Override
	public void jojo_ripples$setNoFlinchModelAnim(boolean flag) {
		this.noFlinchModelAnim = flag;
	}

	@Override
	public void jojo_ripples$setCameraTiltMultiplier(float mult) {
		this.cameraTiltMultiplier = mult;
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
	
	@Override
	public boolean jojo_ripples$getDisabledKBImpact() {
		return disableKBImpact;
	}
	
	@Override
	@Nullable
	public Explosion jojo_ripples$getCausingExplosion() {
		return causingExplosion;
	}
	
	
	@Override
	public boolean jojo_ripples$getScalesFromStandDamageConfig() {
		return scalesFromStandDamageConfig;
	}

	
	@Override
	public boolean jojo_ripples$getNoHurtSound() {
		return noHurtSound;
	}

	@Override
	public boolean jojo_ripples$getNoFlinchModelAnim() {
		return noFlinchModelAnim;
	}

	@Override
	public float jojo_ripples$getCameraTiltMultiplier() {
		return cameraTiltMultiplier;
	}
}
