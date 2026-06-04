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

import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning.BgmPart;
import com.github.standobyte.jojo.client.sound.bgmloop.DebugBgm.BgmTrackType;
import com.github.standobyte.jojo.client.sound.util.SoundUtil;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.objects_mc.WeightsList;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Strictness;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.common.NeoForge;

public class BgmEngine extends SimplePreparableReloadListener<BgmEngine.Preparations> implements AutoCloseable {
	public static final Logger LOGGER = LogUtils.getLogger();
	protected static BgmEngine instance;

	@ApiStatus.Internal
	public static void init(RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new BgmEngine();
			event.registerReloadListener(instance);
			NeoForge.EVENT_BUS.register(instance);
		}
	}
	
	protected BgmEngine() {
		this. mc = Minecraft.getInstance();
		this.vanillaSoundEngine = mc.getSoundManager().soundEngine;
		this.vanillaSoundBuffers = vanillaSoundEngine.soundBuffers;
		this.partitionedSoundBuffers = new PartitionedSoundBuffers();
	}
	
	public static BgmEngine getInstance() {
		return instance;
	}
	
	@Nullable
	public static BgmInstance getCurTrackPlaying() {
		return instance != null ? instance.bgmPlaying : null;
	}
    
	
	protected final Minecraft mc;
	public Map<ResourceLocation, WeightsList<BgmTrackInfo>> loadedTracks = new HashMap<>();
	protected SoundEngine vanillaSoundEngine;
	protected SoundBufferLibrary vanillaSoundBuffers;
	protected PartitionedSoundBuffers partitionedSoundBuffers;
	@Nullable public BgmInstance bgmPlaying;
	protected BgmChannel channel;
	
	// BGM manager
	
	public void play(BgmInstance bgm) {
		if (channel == null) {
			channel = BgmChannel.create();
		}
		
		if (!vanillaSoundEngine.loaded) {
			LOGGER.error("Failed playing BGM - sound engine is not loaded");
			return;
		}
		if (bgm.track == null) {
			LOGGER.error("Failed playing BGM - track not found");
			return;
		}
		if (bgm.started) {
			LOGGER.error("Failed playing BGM - already playing");
			return;
		}
		if (!bgm.canPlaySound()) {
			LOGGER.error("Failed playing BGM - condition not satisfied");
			return;
		}
		
		BgmTrackInfo track = bgm.track.getSound(SoundUtil.random);
		@Nullable BgmLoopPartitioning loopData = track.loop();
		bgm.sound = track.sound();

		float volume = Mth.clamp(bgm.volume, 0.0F, 1.0F);
		float pitch = Mth.clamp(bgm.pitch, 0.5F, 2.0F);
		
		bgm.volume = volume;
		bgm.pitch = pitch;
		
		float channelVolume = calculateVolume(bgm);
		if (channelVolume == 0) {
			LOGGER.warn("Failed playing BGM - {} volume was 0", bgm.sound.getLocation(), bgm.category);
			return;
		}
		
		vanillaSoundEngine.soundDeleteTime.put(bgm, vanillaSoundEngine.tickCount + 20);
//		vanillaSoundEngine.instanceToChannel.put(bgm, channelHandle);
		vanillaSoundEngine.instanceBySource.put(bgm.category, bgm);
		
		channel.setPitch(calculatePitch(bgm));
		channel.setVolume(channelVolume);

		
		
		if (loopData != null) {
			// play the intro part buffer and queue the main loop buffer immediately after
			partitionedSoundBuffers.getPartitionedBuffers(bgm.sound.getPath(), vanillaSoundBuffers, loopData).thenAccept(splitAudioStreams -> {
				SoundBuffer introAudioStream = splitAudioStreams.get(BgmPart.INTRO);
				@Nullable SoundBuffer mainLoopAudioStream = splitAudioStreams.get(BgmPart.MAIN_LOOP);
				@Nullable SoundBuffer outroAudioStream = splitAudioStreams.get(BgmPart.OUTRO);
				
				bgm.outroBuffer = outroAudioStream;
				if (mainLoopAudioStream != null) {
					channel.playAndQueueLoop(introAudioStream, mainLoopAudioStream);
				}
				else {
					if (outroAudioStream != null) {
						channel.playAndQueue(introAudioStream, outroAudioStream);
					}
					else {
						channel.play(introAudioStream);
					}
				}
				
				setBgmInstance(bgm);
			});
		}
		else {
			// just play the audio without looping
			vanillaSoundBuffers.getCompleteBuffer(bgm.sound.getPath()).thenAccept(audioStream -> {
				channel.play(audioStream);
				
				setBgmInstance(bgm);
			});
		}
	}
	
	public void stopBgm() {
		if (bgmPlaying != null) {
			bgmPlaying.stopped = true;
			channel.stop();
			setBgmInstance(null);
		}
	}
	
	@ApiStatus.Internal
	protected void setBgmInstance(BgmInstance bgm) {
		this.bgmPlaying = bgm;
		if (bgm != null) {
			bgm.started = true;
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void tickBGM(ClientTickEvent.Pre event) {
		if (mc.isPaused()) return;
		
		if (channel != null && bgmPlaying != null) {
			if (!bgmPlaying.canPlaySound()) {
				stopBgm();
				return;
			}
			
			bgmPlaying.tick();
			if (bgmPlaying.isStopped()) {
				stopBgm();
				return;
			}
			
			float volume = calculateVolume(bgmPlaying);
			if (volume <= 0) {
				stopBgm();
				return;
			}
			float pitch = calculatePitch(bgmPlaying);
			channel.setVolume(volume);
			channel.setPitch(pitch);
			
			channel.tick();
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void disableVanillaMusic(SelectMusicEvent event) {
		if (bgmPlaying != null) {
			event.setMusic(null);
		}
	}
	
	public void pause() {
		if (channel != null) {
			channel.pause();
		}
	}
	
	public void unpause() {
		if (channel != null) {
			channel.unpause();
		}
	}

	public void updateCategoryVolume(SoundSource category, float volume) {
		if (channel != null && bgmPlaying != null && bgmPlaying.category == category) {
			float catVolume = calculateVolume(bgmPlaying);
			if (catVolume <= 0.0F) {
				stopBgm();
			} else {
				channel.setVolume(catVolume);
			}
		}
	}
	
	// Resource loading

	public static final FileToIdConverter META_DATA_LISTER = new FileToIdConverter("bgm", ".json");
	public static final FileToIdConverter SOUND_LISTER = Sound.SOUND_LISTER;
	@Override
	protected BgmEngine.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		BgmEngine.Preparations preps = new BgmEngine.Preparations();
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
	protected void apply(BgmEngine.Preparations preps, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.loadedTracks.clear();
		preps.tracks.forEach((bgmId, soundsList) -> {
			if (!soundsList.isEmpty()) {
				this.loadedTracks.put(bgmId, soundsList);
			}
		});
	}

	public static class Preparations {
		public Map<ResourceLocation, WeightsList<BgmTrackInfo>> tracks = new HashMap<>();
	}


	public void clear() {
		stopBgm();
		if (channel != null) {
			channel.destroy();
			channel = null;
		}
		partitionedSoundBuffers.clear();
	}

	@Override
	public void close() throws Exception {
		clear();
	}



	// copypaste from SoundEngine - this shit doesn't deserve ATs
	public static float calculateVolume(SoundInstance sound) {
		return calculateVolume(sound.getVolume(), sound.getSource());
	}

	public static float calculateVolume(float volumeMultiplier, SoundSource source) {
		return Mth.clamp(volumeMultiplier * getVolume(source), 0.0F, 1.0F);
	}

	public static float getVolume(@Nullable SoundSource category) {
		return category != null && category != SoundSource.MASTER ? Minecraft.getInstance().options.getSoundSourceVolume(category) : 1.0F;
	}
	
	public static float calculatePitch(SoundInstance sound) {
		return Mth.clamp(sound.getPitch(), 0.5F, 2.0F);
	}

}
