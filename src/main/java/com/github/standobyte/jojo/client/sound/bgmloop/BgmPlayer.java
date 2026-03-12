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
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
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

public class BgmPlayer {
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public boolean isPlaying = false;
	public Consumer<BgmPlayer> onTick;
	public SoundSource category = SoundSource.RECORDS;
	public float volume = 1.0f;
	public float pitch = 1;
	public final Weighted<BgmTrackInfo> track;
	public Sound sound;
	
	public AbstractSoundInstance soundInstance;
	public ChannelAccess.ChannelHandle channelHandle;
	public OptionalInt soundSourceId = OptionalInt.empty();
	
	public OptionalInt loopSoundBuffer = OptionalInt.empty();
	public boolean setLooped = false;
	@Nullable public SoundBuffer outroAudioStream;
	public boolean isAtOutro = false;
	protected int fadeOutTimer = -1;
	protected float fadeOutAmount = 0;
	
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
	public void start() {
		BgmTrackLoader.getInstance().play(this);
	}

	@ApiStatus.Internal
	public void _play(BiConsumer<ChannelAccess.ChannelHandle, SoundInstance> playSound) {
		Minecraft mc = Minecraft.getInstance();
		SoundManager soundManager = mc.getSoundManager();
		SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);
		// SoundEngine copypasta
		CompletableFuture<ChannelAccess.ChannelHandle> completablefuture = soundEngine.channelAccess.createHandle(Library.Pool.STATIC);
		ChannelAccess.ChannelHandle channelHandle = completablefuture.join();
		if (channelHandle == null) {
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				LOGGER.warn("Failed to create new sound handle");
			}
			return;
		}

		float volume = Mth.clamp(this.volume, 0.0F, 1.0F);
		float pitch = Mth.clamp(this.pitch, 0.5F, 2.0F);
		
		// we need to create a sound instance object so that the vanilla can handle stuff like changing volume while the music player correctly
		AbstractSoundInstance soundInstance = new EventlessSound(sound, category, null, 
				volume, pitch, false, 0, 
				SoundInstance.Attenuation.NONE, 0, 0, 0, false) {
			@Override public boolean canStartSilent() { return true; }
		};
		
		soundEngine.soundDeleteTime.put(soundInstance, soundEngine.tickCount + 20);
		soundEngine.instanceToChannel.put(soundInstance, channelHandle);
		soundEngine.instanceBySource.put(category, soundInstance);
		
		channelHandle.execute(channel -> {
			channel.setPitch(calculatePitch(soundInstance));
			channel.setVolume(calculateVolume(soundInstance));
			channel.disableAttenuation();

			channel.setSelfPosition(Vec3.ZERO);
			channel.setRelative(false);
		});
		
		playSound.accept(channelHandle, soundInstance);
		_setSoundInstance(soundInstance, channelHandle);
	}


	@ApiStatus.Internal
	public void _startBgm(SoundBufferLibrary vanillaSoundBuffers, PartitionedSoundBuffers partitionedSoundBuffers, 
			SoundEngine soundEngine) {
		BgmTrackInfo track = this.track.getSound(SoundUtil.random);
		@Nullable BgmLoopPartitioning loopData = track.loop();
		this.sound = track.sound();

		if (loopData != null) {
			this._play((channelHandle, soundInstance) -> {
				// play the intro part buffer and queue the main loop buffer immediately after
				partitionedSoundBuffers.getPartitionedBuffers(this.sound.getPath(), vanillaSoundBuffers, loopData).thenAccept(splitAudioStreams -> {
					SoundBuffer introAudioStream = splitAudioStreams.get(BgmPart.INTRO);
					SoundBuffer mainAudioStream = splitAudioStreams.get(BgmPart.MAIN);
					this.outroAudioStream = splitAudioStreams.get(BgmPart.OUTRO);
					OptionalInt introSoundBuffer = introAudioStream.getAlBuffer();
					OptionalInt mainLoopBuffer = mainAudioStream.getAlBuffer();
					
					channelHandle.execute(channel -> {
						BgmTrackLoader.getInstance().onStartedPlaying(this);
						int soundSourceId = channel.source;
						AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, 0);
						AL10.alSourceQueueBuffers(soundSourceId, introSoundBuffer.getAsInt());
						AL10.alSourceQueueBuffers(soundSourceId, mainLoopBuffer.getAsInt());
						AL10.alSourcePlay(soundSourceId);
						
						this.loopSoundBuffer = introSoundBuffer;
						this.soundSourceId = OptionalInt.of(soundSourceId);
						NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
					});
				});
			});
		}
		else {
			this._play((channelHandle, soundInstance) -> {
				// just play the audio without looping
				vanillaSoundBuffers.getCompleteBuffer(this.sound.getPath()).thenAccept(audioStream -> {
					OptionalInt soundBuffer = audioStream.getAlBuffer();
					
					channelHandle.execute(channel -> {
						BgmTrackLoader.getInstance().onStartedPlaying(this);
						int soundSourceId = channel.source;
						AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, 0);
						AL10.alSourceQueueBuffers(soundSourceId, soundBuffer.getAsInt());
						AL10.alSourcePlay(soundSourceId);

						this.loopSoundBuffer = OptionalInt.empty();
						this.soundSourceId = OptionalInt.of(soundSourceId);
						NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
					});
				});
			});
		}
	}

	public void finishWithOutro() {
		if (isPlaying && !isAtOutro) {
			if (outroAudioStream != null) {
				Minecraft mc = Minecraft.getInstance();
				SoundManager soundManager = mc.getSoundManager();
				SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);

				this._play((channelHandle, soundInstance) -> {
					channelHandle.execute(channel -> {
						stopSound();
						
						int soundSourceId = channel.source;
						OptionalInt outroSoundBuffer = outroAudioStream.getAlBuffer();
						AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, outroSoundBuffer.getAsInt());
						AL10.alSourcePlay(soundSourceId);

						this.loopSoundBuffer = OptionalInt.empty();
						this.soundSourceId = OptionalInt.of(soundSourceId);
						NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
					});
				});
			}
			else {
				setFadeOutTimer(40);
			}
			isAtOutro = true;
		}
	}

	@ApiStatus.Internal
	public void _setSoundInstance(AbstractSoundInstance soundInstance, ChannelAccess.ChannelHandle channelHandle) {
		if (this.soundInstance != null) {
			if (soundInstance != null) {
				soundInstance.volume = this.soundInstance.getVolume();
			}
			SoundManager soundManager = Minecraft.getInstance().getSoundManager();
			soundManager.stop(this.soundInstance);
		}
		this.soundInstance = soundInstance;
		this.channelHandle = channelHandle;
	}

	// FIXME !!!!! (bgm) weird error (one of the two seemingly at random)
	/*
	 * [Sound engine/ERROR] [mojang/OpenAlUtil]: Allocate new source: Invalid name parameter.
	 * [minecraft/SoundEngine]: Failed to create new sound handle
	 */
	/*
	 * [Sound engine/ERROR] [mojang/OpenAlUtil]: Stop: Invalid name parameter.
	 */
	public void stopSound() {
		_setSoundInstance(null, null);
	}


	@ApiStatus.Internal
	public void updateState() {
		soundSourceId.ifPresent(source -> {
			int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
			this.isPlaying = state == AL10.AL_PLAYING || state == AL10.AL_PAUSED;
		});
	}
	
	public void setFadeOutTimer(int ticks) {
		fadeOutTimer = ticks;
		fadeOutAmount = soundInstance.getVolume() / ticks;
	}

	@ApiStatus.Internal
	public void tick() {
		if (Minecraft.getInstance().isPaused()) return;
		
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
		
		// fade out
		if (fadeOutTimer > 0) {
			if (soundInstance != null) {
				this.volume -= fadeOutAmount;
			}
			fadeOutTimer--;
		}
		else if (fadeOutTimer == 0) {
			stopSound();
		}
		
		// a part of the TickableSoundInstance logic from SoundEngine (update volume and pitch)
		if (soundInstance != null && channelHandle != null) {
			float volume = this.volume * calculateVolume(soundInstance);
			float pitch = this.pitch * calculatePitch(soundInstance);
			Vec3 pos = new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
			channelHandle.execute(channel -> {
				channel.setVolume(volume);
				channel.setPitch(pitch);
				channel.setSelfPosition(pos);
			});
		}
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
