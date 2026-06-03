package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import org.lwjgl.openal.AL10;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning.BgmPart;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.audio.OpenAlUtil;
import com.mojang.blaze3d.audio.SoundBuffer;

public class BgmChannel {
	public final int source;
	private final AtomicBoolean initialized = new AtomicBoolean(true);

	private boolean startedPlaying = false;
	@Nullable private BgmPart[] bgmParts = null;
	@Nullable private BgmPart curBgmPart = null;

	private OptionalInt soundBufferToLoop = OptionalInt.empty();

	@Nullable
	public static BgmChannel create() {
		int[] aint = new int[1];
		AL10.alGenSources(aint);
		return OpenAlUtil.checkALError("Allocate new source") ? null : new BgmChannel(aint[0]);
	}

	private BgmChannel(int source) {
		this.source = source;

		AL10.alSourcei(this.source, AL10.AL_DISTANCE_MODEL, 0); // disable attenuation
		AL10.alSourcefv(this.source, AL10.AL_POSITION, new float[]{ 0, 0, 0 });
		AL10.alSourcei(this.source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
	}



	public void play(SoundBuffer track) {
		stop();
		if (checkBuffers(track.getAlBuffer())) {
			AL10.alSourcei(source, AL10.AL_BUFFER, 0);
			AL10.alSourceQueueBuffers(source, track.alBuffer);
			AL10.alSourcePlay(source);
			startedPlaying = true;
		}
	}

	public void playAndQueueLoop(SoundBuffer intro, SoundBuffer mainLoop) {
		stop();
		if (checkBuffers(intro.getAlBuffer(), mainLoop.getAlBuffer())) {
			AL10.alSourcei(source, AL10.AL_BUFFER, 0);
			AL10.alSourceQueueBuffers(source, intro.alBuffer);
			AL10.alSourceQueueBuffers(source, mainLoop.alBuffer);
			soundBufferToLoop = OptionalInt.of(mainLoop.alBuffer);
			bgmParts = new BgmPart[] { BgmPart.INTRO, BgmPart.MAIN_LOOP };
			curBgmPart = BgmPart.INTRO;
			AL10.alSourcePlay(source);
			startedPlaying = true;
		}
	}

	public void playAndQueue(SoundBuffer intro, SoundBuffer outro) {
		stop();
		if (checkBuffers(intro.getAlBuffer(), outro.getAlBuffer())) {
			AL10.alSourcei(source, AL10.AL_BUFFER, 0);
			AL10.alSourceQueueBuffers(source, intro.alBuffer);
			AL10.alSourceQueueBuffers(source, outro.alBuffer);
			bgmParts = new BgmPart[] { BgmPart.INTRO, BgmPart.OUTRO };
			curBgmPart = BgmPart.INTRO;
			AL10.alSourcePlay(source);
			startedPlaying = true;
		}
	}

	public void unqueueAndPlayOutro(SoundBuffer outro) {
		if (startedPlaying && curBgmPart != BgmPart.OUTRO && checkBuffers(outro.getAlBuffer())) {
			AL10.alSourceStop(source);
			AL10.alSourcei(source, AL10.AL_BUFFER, AL10.AL_NONE);
			AL10.alSourceQueueBuffers(source, outro.alBuffer);
			setLooping(false);
			AL10.alSourcePlay(source);

			soundBufferToLoop = OptionalInt.empty();
			bgmParts = null;
			curBgmPart = null;
		}
	}

	public void setLooping(boolean looping) {
		AL10.alSourcei(this.source, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
	}

	public void stop() {
		if (initialized.get()) {
			AL10.alSourceStop(this.source);
			OpenAlUtil.checkALError("Stop");
			AL10.alSourcei(source, AL10.AL_BUFFER, 0); // to unqueue the buffers

			startedPlaying = false;
			bgmParts = null;
			curBgmPart = null;
			soundBufferToLoop = OptionalInt.empty();
			setLooping(false);
		}
	}

	private boolean checkBuffers(OptionalInt... bufferIds) {
		for (OptionalInt buffer : bufferIds) {
			if (buffer.isEmpty()) {
				JojoMod.getLogger().error("Failed queueing audio buffer");
				return false;
			}
		}
		return true;
	}

	public void tick() {
		// check if the track is over
		if (startedPlaying) {
			int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
			if (state == AL10.AL_INITIAL || state == AL10.AL_STOPPED) {
				stop();
			}
		}
		
		// check which BGM part we're on
		if (playing() && bgmParts != null) {
			int curBuffer = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
			if (curBuffer < bgmParts.length) {
				BgmPart curPart = bgmParts[curBuffer];
				if (this.curBgmPart != curPart) {
					// loop the main loop part automatically
					if (curPart == BgmPart.MAIN_LOOP && soundBufferToLoop.isPresent()) {
						AL10.alSourceUnqueueBuffers(source, new int[] { soundBufferToLoop.getAsInt() });
						OpenAlUtil.checkALError("Unqueue buffer");
						setLooping(true);
					}

					this.curBgmPart = curPart;
				}
			}
		}
	}
	
	public String getStateName() {
		int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
		return switch (state) {
			case AL10.AL_INITIAL -> "INITIAL";
			case AL10.AL_PLAYING -> "PLAYING";
			case AL10.AL_PAUSED -> "PAUSED";
			case AL10.AL_STOPPED -> "STOPPED";
			default -> "";
		};
	}

	@Nullable
	public BgmPart getCurPlayingPart() {
		return curBgmPart;
	}

	public int getState() {
		return !this.initialized.get() ? AL10.AL_STOPPED : AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
	}

	public void pause() {
		if (getState() == AL10.AL_PLAYING) {
			AL10.alSourcePause(source);
		}
	}

	public void unpause() {
		if (getState() == AL10.AL_PAUSED) {
			AL10.alSourcePlay(source);
		}
	}

	public boolean playing() {
		return this.getState() == AL10.AL_PLAYING;
	}

	public boolean stopped() {
		return this.getState() == AL10.AL_STOPPED;
	}

	public void setVolume(float volume) {
		AL10.alSourcef(this.source, AL10.AL_GAIN, volume);
	}

	public void setPitch(float pitch) {
		AL10.alSourcef(this.source, AL10.AL_PITCH, pitch);
	}

	public void destroy() {
		if (initialized.compareAndSet(true, false)) {
			stop();
			AL10.alDeleteSources(new int[]{source});
			OpenAlUtil.checkALError("Cleanup");
		}
	}

}
