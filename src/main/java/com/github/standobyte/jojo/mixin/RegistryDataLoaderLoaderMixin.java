package com.github.standobyte.jojo.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.MergingDatapackRegistry;

import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(targets = "net/minecraft/resources/RegistryDataLoader$Loader")
public class RegistryDataLoaderLoaderMixin<T> {
	@Shadow @Final private RegistryDataLoader.RegistryData<T> data;
	@Shadow @Final private WritableRegistry<T> registry;
	@Shadow @Final private Map<ResourceKey<?>, Exception> loadingErrors;

	@Inject(method = "Lnet/minecraft/resources/RegistryDataLoader$Loader;loadFromResources("
			+ "Lnet/minecraft/server/packs/resources/ResourceManager;"
			+ "Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;)V",
			at = @At("HEAD"), cancellable = true)
	public void onLoadFromResources(ResourceManager resouceManager, RegistryOps.RegistryInfoLookup registryInfoLookup, CallbackInfo ci) {
		if (MergingDatapackRegistry.onLoadFromResources(resouceManager, registryInfoLookup, data, registry, loadingErrors)) {
			ci.cancel();
		}
	}
}
