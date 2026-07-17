package com.github.standobyte.jojo.client.sound.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.sound.CustomPathSound;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;

public class SoundUtil {
	
	/**
     * If the specified key is not already associated with a value, 
     * attempts to compute its value using the given mapping function 
     * and enters it into this map.
     * 
     * Unlike the default {@link Map#computeIfAbsent(Object, Function)}, 
     * this will put the returned value into the map even if it's null,
     * meaning we don't have to run the same logic multiple times
     * if the sound we're looking for is missing.
	 */
	@Nullable
	public static <K, V> V computeIfKeyAbsent(Map<K, V> cache, K key, 
			Function<K, V> mappingFunctionMayReturnNull) {
		Objects.requireNonNull(mappingFunctionMayReturnNull);
		if (cache.containsKey(key)) return cache.get(key);
		V value = mappingFunctionMayReturnNull.apply(key);
		cache.put(key, value);
		return value;
	}

	protected static List<Weighted<Sound>> decomposeSounds(WeighedSoundEvents sounds) {
		if (sounds == null) {
			return Collections.emptyList();
		}
		List<Weighted<Sound>> soundList = sounds.list;
		List<Weighted<Sound>> decomposeList = null;
		for (int i = 0; i < soundList.size(); i++) {
			Weighted<Sound> sound = soundList.get(i);
			if (sound instanceof Sound || sound instanceof EventlessSoundAccessor) {
				if (decomposeList != null) {
					decomposeList.add(sound);
				}
			}
			else {
				if (decomposeList == null) {
					decomposeList = new ArrayList<>();
					for (int j = 0; j < i; j++) decomposeList.add(soundList.get(j));
				}
				if (sound instanceof WeighedSoundEvents weighted) {
					decomposeList.addAll(decomposeSounds(weighted));
				}
				else if (sound instanceof SoundEventDelegate delegate) {
					decomposeList.addAll(decomposeSounds(SoundEventDelegate.getSoundEvent(delegate.soundLocation)));
				}
				else if ("net.minecraft.client.sounds.SoundManager$Preparations$1".equals(sound.getClass().getName())) {
					if (DELEGATE_CLOSURE_SOUND_ID == null) {
						try {
							for (Field field : sound.getClass().getDeclaredFields()) {
								if (field.getType() == ResourceLocation.class) {
									DELEGATE_CLOSURE_SOUND_ID = field;
									field.setAccessible(true);
								}
							}
						}
						catch (Exception e) {
							JojoMod.getLogger().error("", e);
						}
						if (DELEGATE_CLOSURE_SOUND_ID == null) {
							JojoMod.getLogger().error("Couldn't retrieve delegate sound event id field");
						}
					}
					if (DELEGATE_CLOSURE_SOUND_ID != null) {
						try {
							ResourceLocation id = (ResourceLocation) DELEGATE_CLOSURE_SOUND_ID.get(sound);
							decomposeList.addAll(decomposeSounds(SoundEventDelegate.getSoundEvent(id)));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							JojoMod.getLogger().error("Couldn't get sounds from a delegate sound event", e);
						}
					}
				}
			}
		}
		return decomposeList != null ? decomposeList : soundList;
	}
	protected static Field DELEGATE_CLOSURE_SOUND_ID;

	public static RandomSource random = RandomSource.create();
	public static Sound pick(List<Weighted<Sound>> sounds) {
		return pick(sounds, SoundManager.EMPTY_SOUND);
	}
	
	public static <T> T pick(List<Weighted<T>> sounds, T empty) {
		if (sounds.isEmpty()) {
			return empty;
		}
		if (sounds.size() == 1) {
			Weighted<T> entry = sounds.get(0);
			if (entry.getWeight() > 0) {
				return entry.getSound(random);
			}
		}
		
		int i = 0;
		for (Weighted<T> weighted : sounds) {
			i += weighted.getWeight();
		}

		if (i != 0) {
			int j = random.nextInt(i);
			for (Weighted<T> weighted : sounds) {
				j -= weighted.getWeight();
				if (j < 0) {
					return weighted.getSound(random);
				}
			}
		}
		
		return empty;
	}
	
	/**
	 * If the sounds list elements have randomness on their own, this won't work correctly 
	 * (that's not a use case I need anyway, at least for now, so that's deliberate)
	 */
	public static <T> T pick(List<Weighted<T>> sounds, Predicate<T> filter, T empty) {
		if (sounds.isEmpty()) {
			return empty;
		}
		
		int i = 0;
		List<Weighted<T>> filtered = new ArrayList<>(sounds.size());
		for (Weighted<T> weighted : sounds) {
			T value = weighted.getSound(random);
			if (filter.test(value)) {
				filtered.add(weighted);
				i += weighted.getWeight();
			}
		}

		if (i != 0) {
			int j = random.nextInt(i);
			for (Weighted<T> weighted : filtered) {
				j -= weighted.getWeight();
				if (j < 0) {
					return weighted.getSound(random);
				}
			}
		}
		
		return empty;
	}
	
	
	
	static final Map<ResourceLocation, Sound> SOUNDS_CACHE = new HashMap<>();
	public static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);
	public static SoundInstance justPutTheSoundInTheBag(ResourceLocation soundLocation, @Nullable Component subtitle,
			SoundSource soundCategory, float volume, float pitch, SoundInstance.Attenuation attenuation,
			double x, double y, double z) {
		Sound sound = SOUNDS_CACHE.computeIfAbsent(soundLocation, path -> new CustomPathSound(path, 
				DEFAULT_FLOAT, DEFAULT_FLOAT, 
				1, Sound.Type.FILE,
				false, false, 16, 
				Sound.SOUND_LISTER.idToFile(path)));
		
		SoundInstance soundInstance = new EventlessSound(
				sound, soundCategory, subtitle,
				volume, pitch, false, 0,
				attenuation, x, y, z, false);
		
		return soundInstance;
	}
	
}
