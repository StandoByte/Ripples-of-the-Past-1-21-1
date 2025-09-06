package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.lwjgl.openal.AL10;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmLoopPartitioning.BgmPart;
import com.github.standobyte.jojo.client.sound.util.EventlessSound;
import com.github.standobyte.jojo.client.sound.util.SoundCache;
import com.github.standobyte.jojo.client.sound.util.SoundUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.JSONUtil;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.logging.LogUtils;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
// FIXME !!!!!!!!!!!!!!!!!! use less reflection ffs
public class BgmLoopPlayer {
	protected static final Logger LOGGER = LogUtils.getLogger();
    
    // FIXME !!!!!!!!!!!!!!!!!! also keep soundEngine reference
    // FIXME !!!!!!!!!!!!!!!!!! can this cause a memory leak?
	protected static SoundBufferLibrary vanillaSoundBuffers;
	protected static PartitionedSoundBuffers partitionedSoundBuffers;
	
	protected final LivingEntity bossEntity;
	protected final SoundEvent soundEvent;
	protected SoundSource category;
	protected float volume;
	protected float pitch;

	boolean setLooped = false;
	boolean finished = false;

	protected Sound sound;
	protected SoundInstance loopSoundInstance;
	protected OptionalInt _soundSourceID = OptionalInt.empty();
	protected OptionalInt _loopSoundBuffer = OptionalInt.empty();
	@Nullable protected SoundBuffer outroAudioStream;

	public BgmLoopPlayer(SoundEvent soundEvent, LivingEntity entity) {
		this(soundEvent, entity, SoundSource.RECORDS, 0.4f, 1);
	}

	public BgmLoopPlayer(SoundEvent soundEvent, LivingEntity entity, 
			SoundSource category, float volume, float pitch) {
		this.soundEvent = soundEvent;
		this.bossEntity = entity;
		this.category = category;
		this.volume = volume;
		this.pitch = pitch;
	}

	static BgmLoopPlayer tickLoopPlayer;
	
	// FIXME !!!!!!!!!!!!!!!!!! a function to preload sound events


	// TODO (bgm) allow playing a sound event without bgm meta
	public void start() {
		Minecraft mc = Minecraft.getInstance();
		SoundManager soundManager = mc.getSoundManager();
		SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);
		if (!ClientReflection.isLoaded(soundEngine)) {
			LOGGER.error("Failed playing looping BGM - sound engine is not loaded");
			finished = true;
			return;
		}

		ResourceManager resourceManager = mc.getResourceManager();
		if (vanillaSoundBuffers == null) vanillaSoundBuffers = ClientReflection.getSoundBuffers(soundEngine);
		if (partitionedSoundBuffers == null) {
			partitionedSoundBuffers = new PartitionedSoundBuffers();
			SoundCache.partitionedSoundBuffers = partitionedSoundBuffers;
		}
		

		// get all the separate sounds from this sound event
		List<Weighted<Sound>> sounds = SoundUtil.getSoundFiles(soundEvent, soundManager);
		if (sounds.isEmpty()) {
			LOGGER.error("Failed playing looping BGM - empty sound event {}", soundEvent.getLocation());
			finished = true;
			return;
		}
		
		// read and cache the .bgmmeta files for all the sounds, and get only the sound files that have those
		sounds = sounds.stream().filter(_sound -> {
			Sound sound = _sound.getSound(null);
			ResourceLocation key = sound.getLocation();
			List<Weighted<BgmLoopPartitioning>> data = SoundCache.computeIfKeyAbsent(SoundCache.bgmLoopMeta, key, soundId -> {
				ResourceLocation path = BgmLoopPartitioning.LISTER.idToFile(soundId);
				try (var fileReader = resourceManager.openAsReader(path)) {
					JsonElement json = JsonParser.parseReader(fileReader);
					return BgmLoopPartitioning.parseList(json);
				} catch (Exception e) {
					LOGGER.error("Failed to read {}", path, e);
					return null;
				}
			});
			return data != null && !data.isEmpty();
		}).toList();
		if (sounds.isEmpty()) {
			LOGGER.error("Failed playing looping BGM - no sounds in {} have a BGM meta file", soundEvent.getLocation());
			finished = true;
			return;
		}
		
		// randomly pick the track
		sound = SoundUtil.pick(sounds);
		ResourceLocation soundLocation = sound.getLocation();
		List<Weighted<BgmLoopPartitioning>> loopsList = SoundCache.bgmLoopMeta.get(soundLocation);
		BgmLoopPartitioning loopData = SoundUtil.pick(loopsList, bgm -> bgm.matchesSoundEvent(soundEvent), null);
		
		if (loopData == null) {
			LOGGER.error("Failed playing looping BGM - you probably messed up the sound events field (tried playing {} but didn't find a BGM meta matching the sound event {})", sound.getLocation(), soundEvent.getLocation());
			finished = true;
			return;
		}
		
		play((channelHandle, soundInstance) -> {
			// play the intro part buffer and queue the main loop buffer immediately after
			partitionedSoundBuffers.getPartitionedBuffers(sound.getPath(), vanillaSoundBuffers, loopData).thenAccept(splitAudioStreams -> {
				SoundBuffer introAudioStream = splitAudioStreams.get(BgmPart.INTRO);
				SoundBuffer mainAudioStream = splitAudioStreams.get(BgmPart.MAIN);
				this.outroAudioStream = splitAudioStreams.get(BgmPart.OUTRO);
				OptionalInt introSoundBuffer = ClientReflection.getAlBuffer(introAudioStream);
				OptionalInt mainLoopBuffer = ClientReflection.getAlBuffer(mainAudioStream);

				channelHandle.execute(channel -> {
					_loopSoundBuffer = introSoundBuffer;
					int soundSourceId = ClientReflection.getSourceId(channel);
					_soundSourceID = OptionalInt.of(soundSourceId);
					this.loopSoundInstance = soundInstance;
					
					// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! i think i can reuse the same channel actually
					if (tickLoopPlayer != null) {
						tickLoopPlayer.forceStop();
					}
					tickLoopPlayer = this;
					
					AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, 0);
					AL10.alSourceQueueBuffers(soundSourceId, introSoundBuffer.getAsInt());
					AL10.alSourceQueueBuffers(soundSourceId, mainLoopBuffer.getAsInt());
					AL10.alSourcePlay(soundSourceId);
					NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
				});
			});
		});
	}
	
	protected void play(BiConsumer<ChannelAccess.ChannelHandle, SoundInstance> playSound) {
		Minecraft mc = Minecraft.getInstance();
		SoundManager soundManager = mc.getSoundManager();
		SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);
		// SoundEngine copypasta
		CompletableFuture<ChannelAccess.ChannelHandle> completablefuture = ClientReflection.getChannelAccess(soundEngine).createHandle(Library.Pool.STATIC);
		ChannelAccess.ChannelHandle channelaccess$channelhandle = completablefuture.join();
		if (channelaccess$channelhandle == null) {
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				LOGGER.warn("Failed to create new sound handle");
			}
		}
		else {
			float categoryVolume = category != null && category != SoundSource.MASTER ? mc.options.getSoundSourceVolume(category) : 1.0F;
			float volume = Mth.clamp(this.volume * categoryVolume, 0.0F, 1.0F);
			float pitch = Mth.clamp(this.pitch, 0.5F, 2.0F);
			
			// we need to create a sound instance object so that the vanilla can handle stuff like changing volume while the music player correctly
			SoundInstance soundInstance = new EventlessSound(sound, category, null, 
					volume, pitch, false, 0, 
					SoundInstance.Attenuation.NONE, 0, 0, 0, false) {
				@Override public boolean canStartSilent() { return true; }
			};
			
			ClientReflection.getSoundDeleteTime(soundEngine).put(soundInstance, ClientReflection.getTickCount(soundEngine) + 20);
			ClientReflection.getInstanceToChannel(soundEngine).put(soundInstance, channelaccess$channelhandle);
			ClientReflection.getInstanceBySource(soundEngine).put(category, soundInstance);
			
			channelaccess$channelhandle.execute(channel -> {
				channel.setPitch(pitch);
				channel.setVolume(volume);
				channel.disableAttenuation();

				channel.setSelfPosition(Vec3.ZERO);
				channel.setRelative(false);
			});
			
			playSound.accept(channelaccess$channelhandle, soundInstance);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void tickBossMusic(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.isPaused() && tickLoopPlayer != null && !tickLoopPlayer.finished) {
			tickLoopPlayer.tick();
		}
		if (tickLoopPlayer != null && tickLoopPlayer.finished) {
			tickLoopPlayer = null;
		}
	}

	public void tick() {
		if (bossEntity != null) {
			if (bossEntity.isDeadOrDying()) {
				finishWithOutro();
				return;
			}
			else if (bossEntity.isRemoved()) {
				forceStop();
				return;
			}
		}

		// check if the intro part has stopped - if it did, it's now the main loop playing (we've queued it previously), so we set looping for that to true
		if (!setLooped) {
			_soundSourceID.ifPresent(soundSourceId -> {
				_loopSoundBuffer.ifPresent(loopSoundBuffer -> {
					int curBuffer = AL10.alGetSourcei(soundSourceId, AL10.AL_BUFFERS_PROCESSED);
					boolean introIsOver = curBuffer == 1;
					if (introIsOver) {
						AL10.alSourceUnqueueBuffers(soundSourceId, new int[] { loopSoundBuffer });
						AL10.alSourcei(soundSourceId, AL10.AL_LOOPING, AL10.AL_TRUE);
						setLooped = true;
					}
				});
			});
		}
	}

	public void finishWithOutro() {
		if (!finished && outroAudioStream != null) {
			Minecraft mc = Minecraft.getInstance();
			SoundManager soundManager = mc.getSoundManager();
			SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);
			
			play((channelHandle, soundInstance) -> {
				channelHandle.execute(channel -> {
					// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! i think i can reuse the same channel actually
					int soundSourceId = ClientReflection.getSourceId(channel);
					OptionalInt outroSoundBuffer = ClientReflection.getAlBuffer(outroAudioStream);
					AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, outroSoundBuffer.getAsInt());
					AL10.alSourcePlay(soundSourceId);
					NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
				});
			});
		}
		
		forceStop();
	}

	// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!! properly close this
	public void forceStop() {
		SoundManager soundManager = Minecraft.getInstance().getSoundManager();
		if (loopSoundInstance != null) {
			soundManager.stop(loopSoundInstance);
			loopSoundInstance = null;
		}
		finished = true;
	}

	public boolean hasFinished() {
		return finished;
	}

}
