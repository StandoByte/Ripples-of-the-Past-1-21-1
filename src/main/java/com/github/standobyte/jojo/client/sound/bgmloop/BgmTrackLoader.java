package com.github.standobyte.jojo.client.sound.bgmloop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning;
import com.github.standobyte.jojo.util.JSONUtil;
import com.github.standobyte.jojo.util.java.WeightsList;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Strictness;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class BgmTrackLoader extends SimplePreparableReloadListener<BgmTrackLoader.Preparations> {
	public Map<ResourceLocation, WeightsList<BgmTrackInfo>> tracks = new HashMap<>();
	public Map<ResourceLocation, WeightsList<BgmTrackInfo>> standOstTracks = new HashMap<>();

	public static final Logger LOGGER = LogUtils.getLogger();
	
	protected static BgmTrackLoader instance;

	@ApiStatus.Internal
	public static void init(RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new BgmTrackLoader();
		}
		event.registerReloadListener(instance);
	}
	
	public static BgmTrackLoader getInstance() {
		return instance;
	}

	public static final FileToIdConverter META_DATA_LISTER = new FileToIdConverter("sounds", ".bgmloop.json");
	public static final FileToIdConverter SOUND_LISTER = Sound.SOUND_LISTER;
	@Override
	protected BgmTrackLoader.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		BgmTrackLoader.Preparations preps = new BgmTrackLoader.Preparations();
		profiler.startTick();
		profiler.push("jojo_ripples:bgm");
		Map<ResourceLocation, Resource> trackDefinitions = META_DATA_LISTER.listMatchingResources(resourceManager);

		for (var trackEntry : trackDefinitions.entrySet()) {
			Resource resource = trackEntry.getValue();
			ResourceLocation id = META_DATA_LISTER.fileToId(trackEntry.getKey());
			try (Reader reader = resource.openAsReader()) {
				ResourceLocation soundPath = SOUND_LISTER.idToFile(id);
				if (!resourceManager.getResource(soundPath).isPresent()) {
					throw new FileNotFoundException(soundPath.toString());
				}
				Sound sound = new Sound(id, DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, false, false, 16);

				JsonElement json = JSONUtil.fromJson(JSONUtil.GSON, reader, JsonElement.class, Strictness.STRICT);
				List<BgmTrackInfo.Unbaked> tracksInfo = JSONUtil.parseArrayOrSingleElement(json, BgmTrackInfo.Unbaked::fromJson);
				for (BgmTrackInfo.Unbaked trackInfoParsed : tracksInfo) {
					BgmTrackInfo info = new BgmTrackInfo(BgmLoopPartitioning.createLoop(trackInfoParsed), sound);
					if (trackInfoParsed.trackIds != null) {
						for (ResourceLocation key : trackInfoParsed.trackIds) {
							preps.tracks.computeIfAbsent(key, __ -> new WeightsList<>(null)).addValue(info);
						}
					}
					if (trackInfoParsed.standTypeIds != null) {
						for (ResourceLocation key : trackInfoParsed.standTypeIds) {
							preps.standOstTracks.computeIfAbsent(key, __ -> new WeightsList<>(null)).addValue(info);
						}
					}
				}

			} catch (RuntimeException | IOException e) {
				LOGGER.warn("Failed to load BGM track {} in resourcepack: '{}'", id, resource.sourcePackId(), e);
			}
		}

		profiler.pop();
		profiler.endTick();
		return preps;
	}
	private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);

	@Override
	protected void apply(BgmTrackLoader.Preparations preps, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.tracks.clear();
		this.tracks.putAll(preps.tracks);
		this.standOstTracks.clear();
		this.standOstTracks.putAll(preps.standOstTracks);
	}

	public static class Preparations {
		public Map<ResourceLocation, WeightsList<BgmTrackInfo>> tracks = new HashMap<>();
		public Map<ResourceLocation, WeightsList<BgmTrackInfo>> standOstTracks = new HashMap<>();
	}
	
}
