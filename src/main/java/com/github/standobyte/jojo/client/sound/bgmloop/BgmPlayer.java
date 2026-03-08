package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.openal.AL10;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning.BgmPart;
import com.github.standobyte.jojo.client.sound.util.EventlessSound;
import com.github.standobyte.jojo.client.sound.util.SoundUtil;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.common.NeoForge;

// FIXME !!!!! (bgm) use less reflection ffs
public class BgmPlayer {
	protected static final Logger LOGGER = LogUtils.getLogger();
	
	boolean isPlaying = false;
	protected Consumer<BgmPlayer> onTick;
	protected SoundSource category = SoundSource.RECORDS;
	protected float volume = 0.4f;
	protected float pitch = 1;
	public final Weighted<BgmTrackInfo> track;
	protected Sound sound;
	
	protected SoundInstance soundInstance;
	protected OptionalInt soundSourceId = OptionalInt.empty();
	
	protected OptionalInt loopSoundBuffer = OptionalInt.empty();
	boolean setLooped = false;
	@Nullable protected SoundBuffer outroAudioStream;
	boolean isAtOutro = false;
	
	@Nullable
	public static BgmPlayer track(ResourceLocation trackId) {
		BgmTrackLoader loader = BgmTrackLoader.getInstance();
		Weighted<BgmTrackInfo> track = loader.tracks.get(trackId);
		if (track == null) {
			LOGGER.error("BGM track {} not found", trackId);
			return null;
		}
		return new BgmPlayer(track);
	}
	
	@Nullable
	public static BgmPlayer standResolve(StandSkin standSkin) {
		if (standSkin == null) return null;

		Weighted<BgmTrackInfo> resolveBGM = standSkin.getResolveBGM();
		if (resolveBGM == null) {
			LOGGER.error("Resolve BGM for Stand skin {} not found", standSkin.skinId);
			return null;
		}
		return new BgmPlayer(resolveBGM);
	}
	
	public BgmPlayer(Weighted<BgmTrackInfo> track) {
		this.track = track;
	}
	
	public void settings(SoundSource category, float volume, float pitch) {
		this.category = category;
		this.volume = volume;
		this.pitch = pitch;
	}

	
	/** Return true from the predicate returns true if you've stopped the soundtrack */
	public void setTickHandler(Consumer<BgmPlayer> onTick) {
		this.onTick = onTick;
	}
	
	public void bossEntity(LivingEntity entity) {
		setTickHandler(bgm -> {
			if (entity != null) {
				if (entity.isDeadOrDying()) {
					bgm.finishWithOutro();
				}
				else if (entity.isRemoved()) {
					bgm.stopSound();
				}
			}
		});
	}

	
	@Nullable
	public static BgmPlayer getCurTrackPlaying() {
		BgmTrackLoader loader = BgmTrackLoader.getInstance();
		return loader != null ? loader.bgmPlaying : null;
	}

	// TODO (bgm) a function to preload sounds
	public static void start(BgmPlayer bgm) {
		BgmTrackLoader.getInstance().play(bgm);
	}
	
	protected void play(BiConsumer<ChannelAccess.ChannelHandle, SoundInstance> playSound) {
		Minecraft mc = Minecraft.getInstance();
		SoundManager soundManager = mc.getSoundManager();
		SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);
		// SoundEngine copypasta
		CompletableFuture<ChannelAccess.ChannelHandle> completablefuture = ClientReflection.getChannelAccess(soundEngine).createHandle(Library.Pool.STATIC);
		ChannelAccess.ChannelHandle channelHandle = completablefuture.join();
		if (channelHandle == null) {
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				LOGGER.warn("Failed to create new sound handle");
			}
			return;
		}

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
		ClientReflection.getInstanceToChannel(soundEngine).put(soundInstance, channelHandle);
		ClientReflection.getInstanceBySource(soundEngine).put(category, soundInstance);
		
		channelHandle.execute(channel -> {
			channel.setPitch(pitch);
			channel.setVolume(volume);
			channel.disableAttenuation();

			channel.setSelfPosition(Vec3.ZERO);
			channel.setRelative(false);
		});
		
		playSound.accept(channelHandle, soundInstance);
	}


	@ApiStatus.Internal
	public void startBgm(SoundBufferLibrary vanillaSoundBuffers, PartitionedSoundBuffers partitionedSoundBuffers, 
			SoundEngine soundEngine) {
		BgmTrackInfo track = this.track.getSound(SoundUtil.random);
		@Nullable BgmLoopPartitioning loopData = track.loop();
		this.sound = track.sound();

		if (loopData != null) {
			this.play((channelHandle, soundInstance) -> {
				// play the intro part buffer and queue the main loop buffer immediately after
				partitionedSoundBuffers.getPartitionedBuffers(this.sound.getPath(), vanillaSoundBuffers, loopData).thenAccept(splitAudioStreams -> {
					SoundBuffer introAudioStream = splitAudioStreams.get(BgmPart.INTRO);
					SoundBuffer mainAudioStream = splitAudioStreams.get(BgmPart.MAIN);
					this.outroAudioStream = splitAudioStreams.get(BgmPart.OUTRO);
					OptionalInt introSoundBuffer = ClientReflection.getAlBuffer(introAudioStream);
					OptionalInt mainLoopBuffer = ClientReflection.getAlBuffer(mainAudioStream);
					
					channelHandle.execute(channel -> {
						BgmTrackLoader.getInstance().onStartedPlaying(this);
						int soundSourceId = ClientReflection.getSourceId(channel);
						AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, 0);
						AL10.alSourceQueueBuffers(soundSourceId, introSoundBuffer.getAsInt());
						AL10.alSourceQueueBuffers(soundSourceId, mainLoopBuffer.getAsInt());
						AL10.alSourcePlay(soundSourceId);
						
						this.loopSoundBuffer = introSoundBuffer;
						this.soundSourceId = OptionalInt.of(soundSourceId);
						this.soundInstance = soundInstance;
						NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
					});
				});
			});
		}
		else {
			this.play((channelHandle, soundInstance) -> {
				// just play the audio without looping
				vanillaSoundBuffers.getCompleteBuffer(this.sound.getPath()).thenAccept(audioStream -> {
					OptionalInt soundBuffer = ClientReflection.getAlBuffer(audioStream);
					
					channelHandle.execute(channel -> {
						BgmTrackLoader.getInstance().onStartedPlaying(this);
						int soundSourceId = ClientReflection.getSourceId(channel);
						AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, 0);
						AL10.alSourceQueueBuffers(soundSourceId, soundBuffer.getAsInt());
						AL10.alSourcePlay(soundSourceId);

						this.loopSoundBuffer = OptionalInt.empty();
						this.soundSourceId = OptionalInt.of(soundSourceId);
						this.soundInstance = soundInstance;
						NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
					});
				});
			});
		}
	}

	public void finishWithOutro() {
		if (isPlaying) {
			if (outroAudioStream == null) {
				// FIXME !!!!! (bgm) weird error (one of the two seemingly at random)
				/*
				 * [Sound engine/ERROR] [mojang/OpenAlUtil]: Allocate new source: Invalid name parameter.
				 * [minecraft/SoundEngine]: Failed to create new sound handle
				 */
				/*
				 * [Sound engine/ERROR] [mojang/OpenAlUtil]: Stop: Invalid name parameter.
				 */
				stopSound();
			}
			else if (!isAtOutro) {
				Minecraft mc = Minecraft.getInstance();
				SoundManager soundManager = mc.getSoundManager();
				SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);

				this.play((channelHandle, soundInstance) -> {
					channelHandle.execute(channel -> {
						stopSound();
						
						int soundSourceId = ClientReflection.getSourceId(channel);
						OptionalInt outroSoundBuffer = ClientReflection.getAlBuffer(outroAudioStream);
						AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, outroSoundBuffer.getAsInt());
						AL10.alSourcePlay(soundSourceId);

						this.loopSoundBuffer = OptionalInt.empty();
						this.soundSourceId = OptionalInt.of(soundSourceId);
						this.soundInstance = soundInstance;
						NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
					});
				});
				isAtOutro = true;
			}
		}
	}

	// FIXME !!!!!!!! (bgm) properly close this
	public void stopSound() {
		if (isPlaying) {
			SoundManager soundManager = Minecraft.getInstance().getSoundManager();
			if (soundInstance != null) {
				soundManager.stop(soundInstance);
				soundInstance = null;
			}
		}
	}

	
	public void updateState() {
		soundSourceId.ifPresent(source -> {
			int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
			this.isPlaying = state == AL10.AL_PLAYING || state == AL10.AL_PAUSED;
		});
	}

	public void tick() {
		// check if the music should still be playing
		if (onTick != null) {
			onTick.accept(this);
		}

		// check if the intro part has stopped - if it did, it's now the main loop playing (we've queued it previously), so we set looping for that to true
		if (isPlaying && !setLooped) {
			loopSoundBuffer.ifPresent(soundBuffer -> {
				soundSourceId.ifPresent(sourceName -> {
					int curBuffer = AL10.alGetSourcei(sourceName, AL10.AL_BUFFERS_PROCESSED);
					boolean introIsOver = curBuffer == 1;
					if (introIsOver) {
						AL10.alSourceUnqueueBuffers(sourceName, new int[] { soundBuffer });
						AL10.alSourcei(sourceName, AL10.AL_LOOPING, AL10.AL_TRUE);
						this.loopSoundBuffer = OptionalInt.empty();
						this.setLooped = true;
					}
				});
			});
		}
	}

}
