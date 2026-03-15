package com.github.standobyte.jojo.mixin.modcompat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.util.Mth;

@Mixin(value = Mth.class, priority = 999)
public class MthInitBeforeLithium {
	@Shadow @Final public static float[] SIN;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void onClassInit(CallbackInfo ci) {
		MathUtil.initTanLUT(SIN);
	}
}
