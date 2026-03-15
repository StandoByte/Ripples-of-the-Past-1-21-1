package com.github.standobyte.jojo.mixin.grab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.subsystems.entity_grab.LivingComponentGrab;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	private final LivingComponentGrab jojo_ripples$grabHandler = this.getData(ModDataAttachmentTypes.LIVING_GRAB.get());

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$cancelPushWhenGrabbed(CallbackInfo ci) {
		if (jojo_ripples$grabHandler.isGrabbed()) {
			ci.cancel();
		}
	}

	@Inject(method = "doPush", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$cancelPushGrabbedEntity(Entity entity, CallbackInfo ci) {
		if (entity.is(jojo_ripples$grabHandler.getGrabbedEntity())) {
			ci.cancel();
		}
	}
	
}
