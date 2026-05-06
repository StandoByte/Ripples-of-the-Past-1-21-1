package com.github.standobyte.jojo.client.standskin.text;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

public class StandSkinComponent {

	public static MutableComponent translatable(ResourceLocation standId, Optional<ResourceLocation> standSkin, String key) {
		return MutableComponent.create(new StandSkinTranslatableContents(key, null, TranslatableContents.NO_ARGS, standId, standSkin));
	}

	public static MutableComponent translatable(ResourceLocation standId, Optional<ResourceLocation> standSkin, String key, Object... args) {
		return MutableComponent.create(new StandSkinTranslatableContents(key, null, args, standId, standSkin));
	}

	public static MutableComponent translatableWithFallback(ResourceLocation standId, Optional<ResourceLocation> standSkin, String key, @Nullable String fallback) {
		return MutableComponent.create(new StandSkinTranslatableContents(key, fallback, TranslatableContents.NO_ARGS, standId, standSkin));
	}

	public static MutableComponent translatableWithFallback(ResourceLocation standId, Optional<ResourceLocation> standSkin, String key, @Nullable String fallback, Object... args) {
		return MutableComponent.create(new StandSkinTranslatableContents(key, fallback, args, standId, standSkin));
	}
	


	public static MutableComponent translatable(StandInstance standInstance, String key) {
		if (standInstance != null) {
			return StandSkinComponent.translatable(standInstance.getStandId(), standInstance.getSelectedSkin(), key);
		}
		return Component.translatable(key);
	}

	public static MutableComponent translatable(StandInstance standInstance, String key, Object... args) {
		if (standInstance != null) {
			return StandSkinComponent.translatable(standInstance.getStandId(), standInstance.getSelectedSkin(), key, args);
		}
		return Component.translatable(key, args);
	}

	public static MutableComponent translatableWithFallback(StandInstance standInstance, String key, @Nullable String fallback) {
		if (standInstance != null) {
			return StandSkinComponent.translatable(standInstance.getStandId(), standInstance.getSelectedSkin(), key, fallback);
		}
		return Component.translatableWithFallback(key, fallback);
	}

	public static MutableComponent translatableWithFallback(StandInstance standInstance, String key, @Nullable String fallback, Object... args) {
		if (standInstance != null) {
			return StandSkinComponent.translatable(standInstance.getStandId(), standInstance.getSelectedSkin(), key, fallback, args);
		}
		return Component.translatableWithFallback(key, fallback, args);
	}
	


	public static MutableComponent translatable(Power<?> standPower, String key) {
		StandPower cast = PowerClass.STAND.cast(standPower);
		StandInstance stand = cast != null ? cast.getStandInstance().orElse(null) : null;
		return StandSkinComponent.translatable(stand, key);
	}

	public static MutableComponent translatable(Power<?> standPower, String key, Object... args) {
		StandPower cast = PowerClass.STAND.cast(standPower);
		StandInstance stand = cast != null ? cast.getStandInstance().orElse(null) : null;
		return StandSkinComponent.translatable(stand, key, args);
	}

	public static MutableComponent translatableWithFallback(Power<?> standPower, String key, @Nullable String fallback) {
		StandPower cast = PowerClass.STAND.cast(standPower);
		StandInstance stand = cast != null ? cast.getStandInstance().orElse(null) : null;
		return StandSkinComponent.translatableWithFallback(stand, key, fallback);
	}

	public static MutableComponent translatableWithFallback(Power<?> standPower, String key, @Nullable String fallback, Object... args) {
		StandPower cast = PowerClass.STAND.cast(standPower);
		StandInstance stand = cast != null ? cast.getStandInstance().orElse(null) : null;
		return StandSkinComponent.translatableWithFallback(stand, key, fallback, args);
	}
}
