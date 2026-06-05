package com.github.standobyte.jojo.client.sound.bgmloop;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.github.standobyte.jojo.client.sound.util.EventlessSoundAccessor;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.logging.LogUtils;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.util.Lazy;

public class BgmInstance implements TickableSoundInstance {
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public final Weighted<BgmTrackInfo> track;
	public Sound sound;
	@Nullable public SoundBuffer outroBuffer;
	
	public final BgmType bgmType;
	public SoundSource category = SoundSource.RECORDS;
	public float volume = 1.0f;
	public float pitch = 1;
	public int FADE_OUT_DURATION = 40;
	
	protected boolean started = false;
	protected boolean finishing = false;
	protected boolean stopped = false;
	
	public BooleanSupplier keepPlaying;
	
	protected int fadeOutTimer = -1;
	protected float fadeOutAmount = 0;
	
	@Nullable
	public static BgmInstance track(ResourceLocation trackId) {
		BgmEngine loader = BgmEngine.getInstance();
		Weighted<BgmTrackInfo> track = loader.loadedTracks.get(trackId);
		if (track == null) {
			LOGGER.error("BGM track {} not found", trackId);
			return null;
		}
		return new BgmInstance(track, BgmType.STAND_RESOLVE);
	}
	
	@Nullable
	public static BgmInstance standResolve(StandSkin standSkin) {
		if (standSkin == null) return null;

		Weighted<BgmTrackInfo> resolveBGM = standSkin.getResolveBGM();
		if (resolveBGM == null) {
			return null;
		}
		return new BgmInstance(resolveBGM, BgmType.STAND_RESOLVE);
	}
	
	public BgmInstance(Weighted<BgmTrackInfo> track, BgmType bgmType) {
		this.track = track;
		this.bgmType = bgmType;
	}
	
	public enum BgmType {
		TRACK,
		STAND_RESOLVE
	}
	
	// TODO (bgm) a function to preload sounds
	public void start() {
		BgmEngine.getInstance().play(this);
	}

	
	protected Lazy<? extends WeighedSoundEvents> accessor = Lazy.of(
			() -> new EventlessSoundAccessor(sound.getLocation(), CommonComponents.EMPTY, sound));
	@Override public ResourceLocation getLocation() { return sound.getLocation(); }
	@Override public WeighedSoundEvents resolve(SoundManager manager) { return accessor.get(); }
	@Override public Sound getSound() { return sound; }
	@Override public SoundSource getSource() { return category; }
	@Override public boolean isLooping() { return false; }
	@Override public boolean isRelative() { return false; }
	@Override public int getDelay() { return 0; }
	@Override public float getVolume() { return volume; }
	@Override public float getPitch() { return pitch; }
	@Override public double getX() { return 0; }
	@Override public double getY() { return 0; }
	@Override public double getZ() { return 0; }
	@Override public Attenuation getAttenuation() { return Attenuation.NONE; }
	//@Override public boolean canStartSilent() { return true; }
	
	public void finishWithOutro() {
		if (started && !finishing) {
			if (outroBuffer != null) {
				BgmEngine.instance.channel.unqueueAndPlayOutro(outroBuffer);
			}
			else {
				setFadeOutTimer(FADE_OUT_DURATION);
			}
			finishing = true;
		}
	}
	
	public void setFadeOutTimer(int ticks) {
		this.fadeOutTimer = ticks;
		fadeOutAmount = getVolume() / ticks;
	}
	
	@Override
	public boolean isStopped() {
		return stopped;
	}
	
	public void setStopped() {
		stopped = true;
	}


	@Override
	public void tick() {
		if (!stopped && !finishing && keepPlaying != null && !keepPlaying.getAsBoolean()) {
			finishWithOutro();
			return;
		}
		
		// fade out
		if (fadeOutTimer > 0) {
			this.volume -= fadeOutAmount;
			fadeOutTimer--;
		}
		else if (fadeOutTimer == 0) {
			setStopped();
		}
	}
}
