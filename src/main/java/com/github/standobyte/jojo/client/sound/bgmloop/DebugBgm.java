package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.objects_mc.WeightsList;

import net.minecraft.SharedConstants;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;

public class DebugBgm {
	public static Map<ResourceLocation, Pair<WeightsList<BgmTrackInfo>, MutableInt>> regularTracks = new HashMap<>();
	public static Map<ResourceLocation, Pair<WeightsList<BgmTrackInfo>, MutableInt>> standSkins = new HashMap<>();
	
	public enum BgmTrackType { 
		REGULAR(regularTracks), 
		STAND_SKINS(standSkins);
		
		private final Map<ResourceLocation, Pair<WeightsList<BgmTrackInfo>, MutableInt>> allTracks;
		private BgmTrackType(Map<ResourceLocation, Pair<WeightsList<BgmTrackInfo>, MutableInt>> allTracks) {
			this.allTracks = allTracks;
		}
	}
	
	public static void clear(BgmTrackType type) {
		if (!SharedConstants.IS_RUNNING_IN_IDE) return;
		
		type.allTracks.clear();
	}

	public static void onLoad(BgmTrackType type, ResourceLocation trackSoundEvent, WeightsList<BgmTrackInfo> tracks) {
		if (!SharedConstants.IS_RUNNING_IN_IDE) return;

		type.allTracks.compute(trackSoundEvent, (key, curValue) -> {
			WeightsList<BgmTrackInfo> list;
			Pair<WeightsList<BgmTrackInfo>, MutableInt> value;
			if (curValue == null) {
				list = new WeightsList<>(null);
				value = Pair.of(list, new MutableInt());
			}
			else {
				list = curValue.getLeft();
				value = curValue;
			}

			for (var entry : tracks.wrappedList) {
				list.addEntry(entry);
			}
			return value;
		});
	}

	@Nullable
	public static Weighted<BgmTrackInfo> cycleVariation(BgmTrackType type, ResourceLocation standSkinOrBgmPath) {
		if (!SharedConstants.IS_RUNNING_IN_IDE) return null;

		ResourceLocation track = standSkinOrBgmPath;
		Pair<WeightsList<BgmTrackInfo>, MutableInt> entry = type.allTracks.get(track);
		if (entry == null) {
			JojoMod.getLogger().debug("Track {} not found", track);
			return null;
		}

		WeightsList<BgmTrackInfo> tracks = entry.getLeft();
		if (tracks.isEmpty()) {
			JojoMod.getLogger().debug("Track {} has no variations", track);
			return null;
		}

		MutableInt counter = entry.getRight();
		int index = counter.intValue();
		Weighted<BgmTrackInfo> value = tracks.wrappedList.get(index);
		counter.setValue((index + 1) % tracks.wrappedList.size());
		JojoMod.getLogger().debug("Track {}: variation {} / {} (probability = {})", track, 
				index + 1, tracks.wrappedList.size(), 
				(float) value.getWeight() / tracks.getWeight());
		return value;
	}
}
