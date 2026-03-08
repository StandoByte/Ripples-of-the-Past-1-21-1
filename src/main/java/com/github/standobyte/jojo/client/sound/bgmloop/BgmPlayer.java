package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.lwjgl.openal.AL10;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning.BgmPart;
import com.github.standobyte.jojo.client.sound.util.EventlessSound;
import com.github.standobyte.jojo.client.sound.util.SoundUtil;
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
	
	protected Consumer<BgmPlayer> onTick;
	protected SoundSource category = SoundSource.RECORDS;
	protected float volume = 0.4f;
	protected float pitch = 1;

	boolean _setLooped = false;
	boolean isPlaying = false;
//	boolean finished = false;

	public final Weighted<BgmTrackInfo> track;
	protected Sound sound;
	protected SoundInstance loopSoundInstance;
	protected OptionalInt _soundSourceID = OptionalInt.empty();
	protected OptionalInt _loopSoundBuffer = OptionalInt.empty();
	@Nullable protected SoundBuffer outroAudioStream;
	
	public static BgmPlayer track(ResourceLocation trackId) {
		BgmTrackLoader loader = BgmTrackLoader.getInstance();
		Weighted<BgmTrackInfo> track = loader.tracks.get(trackId);
		if (track == null) {
			LOGGER.error("BGM track {} not found", trackId);
		}
		return new BgmPlayer(track);
	}
	
	public static BgmPlayer standBGM(ResourceLocation standId) {
		BgmTrackLoader loader = BgmTrackLoader.getInstance();
		Weighted<BgmTrackInfo> track = loader.standOstTracks.get(standId);
		if (track == null) {
			LOGGER.error("BGM theme of Stand {} not found", standId);
		}
		return new BgmPlayer(track);
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
					bgm.forceStop();
				}
			}
		});
	}


	// FIXME !!!!! (bgm) a function to preload sounds
	public static void start(BgmPlayer bgm) {
		BgmTrackLoader.getInstance().play(bgm);
	}
	
	@Nullable
	public static BgmPlayer getCurTrackPlaying() {
		BgmTrackLoader loader = BgmTrackLoader.getInstance();
		return loader != null ? loader.bgmPlaying : null;
	}
	
	public void startPlaying(SoundBufferLibrary vanillaSoundBuffers, PartitionedSoundBuffers partitionedSoundBuffers, SoundEngine soundEngine, Runnable onPlay) {
		BgmPlayer bgm = this;
		
		BgmTrackInfo track = bgm.track.getSound(SoundUtil.random);
		@Nullable BgmLoopPartitioning loopData = track.loop();
		bgm.sound = track.sound();
		
		if (loopData != null) {
			bgm.play((channelHandle, soundInstance) -> {
				// play the intro part buffer and queue the main loop buffer immediately after
				partitionedSoundBuffers.getPartitionedBuffers(bgm.sound.getPath(), vanillaSoundBuffers, loopData).thenAccept(splitAudioStreams -> {
					SoundBuffer introAudioStream = splitAudioStreams.get(BgmPart.INTRO);
					SoundBuffer mainAudioStream = splitAudioStreams.get(BgmPart.MAIN);
					bgm.outroAudioStream = splitAudioStreams.get(BgmPart.OUTRO);
					OptionalInt introSoundBuffer = ClientReflection.getAlBuffer(introAudioStream);
					OptionalInt mainLoopBuffer = ClientReflection.getAlBuffer(mainAudioStream);
					
					channelHandle.execute(channel -> {
						bgm._loopSoundBuffer = introSoundBuffer;
						int soundSourceId = ClientReflection.getSourceId(channel);
						bgm._soundSourceID = OptionalInt.of(soundSourceId);
						bgm.loopSoundInstance = soundInstance;
						
						// FIXME !!!!!!!!!!! (bgm) i think i can reuse the same channel actually
						onPlay.run();
						
						AL10.alSourcei(soundSourceId, AL10.AL_BUFFER, 0);
						AL10.alSourceQueueBuffers(soundSourceId, introSoundBuffer.getAsInt());
						AL10.alSourceQueueBuffers(soundSourceId, mainLoopBuffer.getAsInt());
						AL10.alSourcePlay(soundSourceId);
						NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(soundEngine, soundInstance, channel));
					});
				});
			});
		}
		else {
			// FIXME !!!!!!!!!!!!!!!! (bgm) play the sound without looping
		}
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
	

	public void tick() {
		// check if the music should still be playing
		if (onTick != null) {
			onTick.accept(this);
		}

		// check if the intro part has stopped - if it did, it's now the main loop playing (we've queued it previously), so we set looping for that to true
		if (!hasFinished() && !_setLooped) {
			_soundSourceID.ifPresent(soundSourceId -> {
				_loopSoundBuffer.ifPresent(loopSoundBuffer -> {
					int curBuffer = AL10.alGetSourcei(soundSourceId, AL10.AL_BUFFERS_PROCESSED);
					boolean introIsOver = curBuffer == 1;
					if (introIsOver) {
						AL10.alSourceUnqueueBuffers(soundSourceId, new int[] { loopSoundBuffer });
						AL10.alSourcei(soundSourceId, AL10.AL_LOOPING, AL10.AL_TRUE);
						_setLooped = true;
					}
				});
			});
		}
	}

	public void finishWithOutro() {
		if (isPlaying && outroAudioStream != null) {
			Minecraft mc = Minecraft.getInstance();
			SoundManager soundManager = mc.getSoundManager();
			SoundEngine soundEngine = ClientReflection.getSoundEngine(soundManager);
			
			play((channelHandle, soundInstance) -> {
				channelHandle.execute(channel -> {
					// FIXME !!!!!!!!!!! (bgm) i think i can reuse the same channel actually
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

	// FIXME !!!!!!!! (bgm) properly close this
	public void forceStop() {
		if (isPlaying) {
			SoundManager soundManager = Minecraft.getInstance().getSoundManager();
			if (loopSoundInstance != null) {
				soundManager.stop(loopSoundInstance);
				loopSoundInstance = null;
			}
			isPlaying = false;
		}
	}

	public boolean hasFinished() {
		return !isPlaying;
	}

}
