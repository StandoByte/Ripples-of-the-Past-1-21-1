package com.github.standobyte.jojo.mixin.stand.theworld.timestop;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.level.TimeStopLevelTracker;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

	protected ClientLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension,
			RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration,
			Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed,
			int maxChainedNeighborUpdates) {
		super(levelData, dimension, 
				registryAccess, dimensionTypeRegistration, 
				profiler, isClientSide, isDebug, biomeZoomSeed,
				maxChainedNeighborUpdates);
	}

	@Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
	public void cancelDaylightCycle(CallbackInfo ci) {
		if (TimeStopLevelTracker.hasATimeStop(this)) {
			ci.cancel();
		}
	}
}
