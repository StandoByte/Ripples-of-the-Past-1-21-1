package com.github.standobyte.jojo.mixin.client;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmEngine;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmInstance;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Shadow public boolean loaded;

	@Inject(method = "pause", at = @At("TAIL"))
	public void pauseSeparateChannels(CallbackInfo ci) {
		if (loaded) {
			BgmEngine.getInstance().pause();
		}
	}

	@Inject(method = "resume", at = @At("TAIL"))
	public void resumeSeparateChannels(CallbackInfo ci) {
		if (loaded) {
			BgmEngine.getInstance().unpause();
		}
	}
	
	@Inject(method = "updateCategoryVolume", at = @At("TAIL"))
	public void onUpdateCategoryVolume(SoundSource category, float volume, CallbackInfo ci) {
		if (loaded) {
			BgmEngine.getInstance().updateCategoryVolume(category, volume);
		}
	}
	
	@Inject(method = "stopAll", at = @At("TAIL"))
	public void stopSeparateChannels(CallbackInfo ci) {
		if (loaded) {
			BgmEngine.getInstance().stopBgm();
		}
	}
	
	@Inject(method = "stop(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/sounds/SoundSource;)V", at = @At("TAIL"))
	public void stopSeparateChannelsMatchingCategory(@Nullable ResourceLocation soundName, @Nullable SoundSource category, CallbackInfo ci) {
		if (loaded) {
			if (category != null) {
				BgmInstance bgm = BgmEngine.getCurTrackPlaying();
				if (bgm != null && bgm.category == category) {
					BgmEngine.getInstance().stopBgm();
				}
			}
		}
	}
}
