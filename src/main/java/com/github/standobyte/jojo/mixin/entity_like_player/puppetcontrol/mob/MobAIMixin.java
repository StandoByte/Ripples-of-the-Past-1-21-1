package com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.EntityComponentController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.mob.HardcodedMobControlCommands;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobAIMixin extends LivingEntity implements HardcodedMobControlCommands.KeepRMBState {
	public boolean jojo_ripples$controllerHoldingRMB = false;
	@Override public boolean jojo_ripples$isHoldingRMB() { return jojo_ripples$controllerHoldingRMB; }
	@Override public void jojo_ripples$setIsHoldingRMB(boolean isHoldingRMB) { this.jojo_ripples$controllerHoldingRMB = isHoldingRMB; }

	protected MobAIMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}
	
	@Redirect(method = "serverAiStep", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V"))
	public void jojo_ripples$cancelCustomAiOnClient(Mob thisAsMob) {
		if (!this.level().isClientSide()) {
			this.customServerAiStep();
		}
	}

	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$manualMobControl(CallbackInfo ci) {
		if (!level().isClientSide()) {
			EntityComponentController controller = EntityComponentController.getCurrentController(this);
			if (controller != null && controller.suppressControlledEntity()) {
				HardcodedMobControlCommands.serverTickControlledMob((Mob) (LivingEntity) this, jojo_ripples$controllerHoldingRMB);
				ci.cancel();
			}
		}
	}
	
	@Shadow protected abstract void customServerAiStep();
}
