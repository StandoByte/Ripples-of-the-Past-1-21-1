package com.github.standobyte.jojo.mixin.container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojoimpl.stands._helditems.StandHandsContainerMenu.HandSwapSyncFixCrutch;

import net.minecraft.world.entity.LivingEntity;

// see StandHandsContainerMenu#clicked F key handling for why this dogshit exists
@Mixin(LivingEntity.class)
public class LivingEntityHandSwapSyncFixCrutch implements HandSwapSyncFixCrutch {
	
	@Unique private boolean handSwapSyncFixCrutch = false;
	@Override public void jojo_ripples$enableHandSwapCrutch() { this.handSwapSyncFixCrutch = true; }
	@Override public void jojo_ripples$disableHandSwapCrutch() { this.handSwapSyncFixCrutch = false; }
	
	@Inject(method = "handleHandSwap", at = @At("HEAD"), cancellable = true)
	@Unique private void handSwapSyncFixCrutch(CallbackInfo ci) {
		if (handSwapSyncFixCrutch) ci.cancel();
	}

}
