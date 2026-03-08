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

public class BgmTrackLoader extends SimplePreparableReloadListener<BgmTrackLoader.Preparations> {
	public Map<ResourceLocation, WeightsList<BgmTrackInfo>> tracks = new HashMap<>();

	public static final Logger LOGGER = LogUtils.getLogger();
	
	protected static BgmTrackLoader instance;

	@ApiStatus.Internal
	public static void init(RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new BgmTrackLoader();
		}
		event.registerReloadListener(instance);
		NeoForge.EVENT_BUS.register(instance);
	}
	
	public static BgmTrackLoader getInstance() {
		return instance;
	}
	
	// BGM manager
    
    // FIXME !!!!! (bgm) also keep soundEngine reference
    // FIXME !!!!! (bgm) can this cause a memory leak?
	protected SoundBufferLibrary vanillaSoundBuffers;
	protected PartitionedSoundBuffers partitionedSoundBuffers;
	public BgmPlayer bgmPlaying;
	
	public void play(BgmPlayer bgm) {
		Minecraft mc = Minecraft.getInstance();
		SoundManager soundManager = mc.getSoundManager();
		SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);
		if (!ClientReflection.isLoaded(soundEngine)) {
			LOGGER.error("Failed playing looping BGM - sound engine is not loaded");
			return;
		}
		
		if (bgm.track == null) {
			LOGGER.error("Failed playing BGM - track not found");
			return;
		}

		if (vanillaSoundBuffers == null) {
			vanillaSoundBuffers = ClientReflection.getSoundBuffers(soundEngine);
		}
		if (partitionedSoundBuffers == null) {
			partitionedSoundBuffers = new PartitionedSoundBuffers();
		}

		bgm.startPlaying(vanillaSoundBuffers, partitionedSoundBuffers, soundEngine, () -> {
			if (this.bgmPlaying != null) {
				this.bgmPlaying.forceStop();
			}
			this.bgmPlaying = bgm;
			bgm.isPlaying = true;
		});
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void tickBossMusic(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		BgmTrackLoader manager = BgmTrackLoader.getInstance();
		if (manager != null) {
			if (!mc.isPaused() && bgmPlaying != null && bgmPlaying.isPlaying) {
				bgmPlaying.tick();
			}
			if (bgmPlaying != null && !bgmPlaying.isPlaying) {
				bgmPlaying = null;
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
		Map<ResourceLocation, Resource> trackDefinitions = META_DATA_LISTER.listMatchingResources(resourceManager);

		for (var trackEntry : trackDefinitions.entrySet()) {
			Resource resource = trackEntry.getValue();
			ResourceLocation bgmDataId = META_DATA_LISTER.fileToId(trackEntry.getKey());
			try {
				preps.tracks.put(bgmDataId, parse(resource, resourceManager));
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
			List<BgmTrackInfo.Unbaked> tracksInfo = JSONUtil.parseArrayOrSingleElement(json, BgmTrackInfo.Unbaked::fromJson);
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
			if (bgmManager.partitionedSoundBuffers != null) bgmManager.partitionedSoundBuffers.clear();
		}
	}
	
	// FIXME !!!!!!!!!!!!!! (bgm) call this on Minecraft#close
	// FIXME !!!!!!!!!!!!!! (bgm) do i have to make a BgmLoopPlayer#close() too?
	public static void close() {
		BgmTrackLoader bgmManager = BgmTrackLoader.getInstance();
		if (bgmManager != null) {
			if (bgmManager.partitionedSoundBuffers != null) bgmManager.partitionedSoundBuffers.clear();
		}
	}
	
}
