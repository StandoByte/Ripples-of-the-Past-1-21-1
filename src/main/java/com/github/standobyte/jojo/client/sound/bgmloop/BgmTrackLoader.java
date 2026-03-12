package com.github.standobyte.jojo.client.sound.bgmloop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.ModClientResources;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning;
import com.github.standobyte.jojo.client.sound.bgmloop.DebugBgm.BgmTrackType;
import com.github.standobyte.jojo.util.JSONUtil;
import com.github.standobyte.jojo.util.java.WeightsList;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Strictness;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.common.NeoForge;

public class BgmTrackLoader extends SimplePreparableReloadListener<BgmTrackLoader.Preparations> implements AutoCloseable {
	public static final Logger LOGGER = LogUtils.getLogger();
	protected static BgmTrackLoader instance;

	@ApiStatus.Internal
	public static void init(RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new BgmTrackLoader();
			event.registerReloadListener(instance);
			NeoForge.EVENT_BUS.register(instance);
			ModClientResources.closeables.add(instance);
		}
	}
	
	protected BgmTrackLoader() {
		Minecraft mc = Minecraft.getInstance();
		SoundManager soundManager = mc.getSoundManager();
		this.soundEngine = ClientReflection.getSoundEngine(soundManager);
		this.vanillaSoundBuffers = soundEngine.soundBuffers;
		this.partitionedSoundBuffers = new PartitionedSoundBuffers();
	}
	
	public static BgmTrackLoader getInstance() {
		return instance;
	}
    
	
	public Map<ResourceLocation, WeightsList<BgmTrackInfo>> tracks = new HashMap<>();
	protected SoundEngine soundEngine;
	protected SoundBufferLibrary vanillaSoundBuffers;
	protected PartitionedSoundBuffers partitionedSoundBuffers;
	@Nullable public BgmPlayer bgmPlaying;
	
	// BGM manager
	
	public void play(BgmPlayer bgm) {
		if (!soundEngine.loaded) {
			LOGGER.error("Failed playing looping BGM - sound engine is not loaded");
			return;
		}
		
		if (bgm.track == null) {
			LOGGER.error("Failed playing BGM - track not found");
			return;
		}
		
		bgm._startBgm(vanillaSoundBuffers, partitionedSoundBuffers, soundEngine);
	}
	
	@ApiStatus.Internal
	public void onStartedPlaying(BgmPlayer bgm) {
		if (this.bgmPlaying != null) {
			this.bgmPlaying.stopSound();
		}
		this.bgmPlaying = bgm;
		bgm.isPlaying = true;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void tickBossMusic(ClientTickEvent.Pre event) {
		if (bgmPlaying != null) {
			bgmPlaying.updateState();
			if (!bgmPlaying.isPlaying) {
				bgmPlaying.stopSound();
				bgmPlaying = null;
			}
			else {
				bgmPlaying.tick();
			}
		}
	}
	
	// Resource loading

	public static final FileToIdConverter META_DATA_LISTER = new FileToIdConverter("bgm", ".json");
	public static final FileToIdConverter SOUND_LISTER = Sound.SOUND_LISTER;
	@Override
	protected BgmTrackLoader.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		BgmTrackLoader.Preparations preps = new BgmTrackLoader.Preparations();
		profiler.startTick();
		profiler.push("jojo_ripples:bgm");
		DebugBgm.clear(BgmTrackType.REGULAR);
		Map<ResourceLocation, Resource> trackDefinitions = META_DATA_LISTER.listMatchingResources(resourceManager);

		for (var trackEntry : trackDefinitions.entrySet()) {
			Resource resource = trackEntry.getValue();
			ResourceLocation bgmDataId = META_DATA_LISTER.fileToId(trackEntry.getKey());
			try {
				WeightsList<BgmTrackInfo> tracks = parse(resource, resourceManager);
				preps.tracks.put(bgmDataId, tracks);
				DebugBgm.onLoad(BgmTrackType.REGULAR, bgmDataId, tracks);
			} catch (RuntimeException | IOException e) {
				LOGGER.warn("Failed to load BGM definition {} in resourcepack: '{}'", bgmDataId, resource.sourcePackId(), e);
			}
		}

		profiler.pop();
		profiler.endTick();
		return preps;
	}
	
	public static WeightsList<BgmTrackInfo> parse(Resource bgmFile, ResourceManager resourceManager) throws IOException {
		try (Reader reader = bgmFile.openAsReader()) {
			WeightsList<BgmTrackInfo> tracks = new WeightsList<>(null);
			JsonElement json = JSONUtil.fromJson(JSONUtil.GSON, reader, JsonElement.class, Strictness.STRICT);
			List<BgmTrackInfo.Unbaked> tracksInfo = new ArrayList<>();
			BgmTrackInfo.Unbaked.fromJson(json, tracksInfo);
			for (BgmTrackInfo.Unbaked trackInfoParsed : tracksInfo) {
				ResourceLocation soundId = trackInfoParsed.audio;
				ResourceLocation soundPath = SOUND_LISTER.idToFile(soundId);
				if (!resourceManager.getResource(soundPath).isPresent()) {
					LOGGER.error("Soundtrack {} not found", soundId, new FileNotFoundException(soundPath.toString()));
					continue;
				}
				Sound sound = new Sound(soundId, DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, false, false, 16);
				BgmTrackInfo info = new BgmTrackInfo(BgmLoopPartitioning.createLoop(trackInfoParsed), sound);
				tracks.addValue(info, trackInfoParsed.weight);
			}
			return tracks;
		}
	}
	private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);

	@Override
	protected void apply(BgmTrackLoader.Preparations preps, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.tracks.clear();
		preps.tracks.forEach((bgmId, soundsList) -> {
			if (!soundsList.isEmpty()) {
				this.tracks.put(bgmId, soundsList);
			}
		});
	}

	public static class Preparations {
		public Map<ResourceLocation, WeightsList<BgmTrackInfo>> tracks = new HashMap<>();
	}


	public static void onResourceReload(SoundEngineLoadEvent event) {
		BgmTrackLoader bgmManager = BgmTrackLoader.getInstance();
		if (bgmManager != null) {
			bgmManager.clear();
		}
	}

	public void clear() {
		partitionedSoundBuffers.clear();
		if (bgmPlaying != null) {
			bgmPlaying.isPlaying = false;
			bgmPlaying = null;
		}
	}

	@Override
	public void close() throws Exception {
		clear();
	}

}
