package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.SequencedMap;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundSource;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

public final class ClientReflection {

	private static final Field SOUND_MANAGER_SOUND_CACHE = ObfuscationReflectionHelper.findField(SoundManager.class, "soundCache");
	public static Map<ResourceLocation, Resource> getSoundCache(SoundManager soundManager) {
		return ReflectionUtil.getFieldValue(SOUND_MANAGER_SOUND_CACHE, soundManager);
	}

	private static final Field SOUND_MANAGER_SOUND_ENGINE = ObfuscationReflectionHelper.findField(SoundManager.class, "soundEngine");
	public static SoundEngine getSoundEngine(SoundManager soundManager) {
		return ReflectionUtil.getFieldValue(SOUND_MANAGER_SOUND_ENGINE, soundManager);
	}


	// this whole thing is an abomination

	private static final Field CHANNEL_SOURCE = ObfuscationReflectionHelper.findField(Channel.class, "source");
	public static int getSourceId(Channel source) {
		return ReflectionUtil.getIntFieldValue(CHANNEL_SOURCE, source);
	}

	private static final Method SOUND_BUFFER_GET_AL_BUFFER = ObfuscationReflectionHelper.findMethod(SoundBuffer.class, "getAlBuffer");
	public static OptionalInt getAlBuffer(SoundBuffer buffer) {
		return ReflectionUtil.invokeMethod(SOUND_BUFFER_GET_AL_BUFFER, buffer);
	}

	private static final Field SOUND_ENGINE_LOADED = ObfuscationReflectionHelper.findField(SoundEngine.class, "loaded");
	public static boolean isLoaded(SoundEngine soundEngine) {
		return ReflectionUtil.getBooleanFieldValue(SOUND_ENGINE_LOADED, soundEngine);
	}

	private static final Field SOUND_ENGINE_SOUND_BUFFERS = ObfuscationReflectionHelper.findField(SoundEngine.class, "soundBuffers");
	public static SoundBufferLibrary getSoundBuffers(SoundEngine soundEngine) {
		return ReflectionUtil.getFieldValue(SOUND_ENGINE_SOUND_BUFFERS, soundEngine);
	}

	private static final Field SOUND_ENGINE_CHANNEL_ACCESS = ObfuscationReflectionHelper.findField(SoundEngine.class, "channelAccess");
	public static ChannelAccess getChannelAccess(SoundEngine soundEngine) {
		return ReflectionUtil.getFieldValue(SOUND_ENGINE_CHANNEL_ACCESS, soundEngine);
	}

	private static final Field SOUND_ENGINE_INSTANCE_TO_CHANNEL = ObfuscationReflectionHelper.findField(SoundEngine.class, "instanceToChannel");
	public static Map<SoundInstance, ChannelAccess.ChannelHandle> getInstanceToChannel(SoundEngine soundEngine) {
		return ReflectionUtil.getFieldValue(SOUND_ENGINE_INSTANCE_TO_CHANNEL, soundEngine);
	}

	private static final Field SOUND_ENGINE_INSTANCE_BY_SOURCE = ObfuscationReflectionHelper.findField(SoundEngine.class, "instanceBySource");
	public static Multimap<SoundSource, SoundInstance> getInstanceBySource(SoundEngine soundEngine) {
		return ReflectionUtil.getFieldValue(SOUND_ENGINE_INSTANCE_BY_SOURCE, soundEngine);
	}

	private static final Field SOUND_ENGINE_SOUND_DELETE_TIME = ObfuscationReflectionHelper.findField(SoundEngine.class, "soundDeleteTime");
	public static Map<SoundInstance, Integer> getSoundDeleteTime(SoundEngine soundEngine) {
		return ReflectionUtil.getFieldValue(SOUND_ENGINE_SOUND_DELETE_TIME, soundEngine);
	}

	private static final Field SOUND_ENGINE_TICK_COUNT = ObfuscationReflectionHelper.findField(SoundEngine.class, "tickCount");
	public static int getTickCount(SoundEngine soundEngine) {
		return ReflectionUtil.getIntFieldValue(SOUND_ENGINE_TICK_COUNT, soundEngine);
	}

	private static final Field WEIGHED_SOUND_EVENTS_LIST = ObfuscationReflectionHelper.findField(WeighedSoundEvents.class, "list");
	public static List<Weighted<Sound>> getSoundsList(WeighedSoundEvents sounds) {
		return ReflectionUtil.getFieldValue(WEIGHED_SOUND_EVENTS_LIST, sounds);
	}

	private static final Field SOUND_BUFFER_DATA = ObfuscationReflectionHelper.findField(SoundBuffer.class, "data");
	@Nullable
	public static ByteBuffer getSoundData(SoundBuffer soundBuffer) {
		return ReflectionUtil.getFieldValue(SOUND_BUFFER_DATA, soundBuffer);
	}

	private static final Field SOUND_BUFFER_FORMAT = ObfuscationReflectionHelper.findField(SoundBuffer.class, "format");
	public static AudioFormat getAudioFormat(SoundBuffer soundBuffer) {
		return ReflectionUtil.getFieldValue(SOUND_BUFFER_FORMAT, soundBuffer);
	}


//	private static final Field GAME_RENDERER_RESOURCE_POOL = ObfuscationReflectionHelper.findField(GameRenderer.class, "resourcePool");
//	public static CrossFrameResourcePool getResourcePool(GameRenderer gameRenderer) {
//		return ReflectionUtil.getFieldValue(GAME_RENDERER_RESOURCE_POOL, gameRenderer);
//	}
	
	
	private static final Field RELOADABLE_RESOURCE_MANAGER_LISTENERS = ObfuscationReflectionHelper.findField(ReloadableResourceManager.class, "listeners");
	public static List<PreparableReloadListener> getListeners(ReloadableResourceManager resourceManager) {
		return ReflectionUtil.getFieldValue(RELOADABLE_RESOURCE_MANAGER_LISTENERS, resourceManager);
	}
	
	
	private static final Field OPTIONS_SCREEN_LAYOUT = ObfuscationReflectionHelper.findField(OptionsScreen.class, "layout");
	public static HeaderAndFooterLayout getLayout(OptionsScreen screen) {
		return ReflectionUtil.getFieldValue(OPTIONS_SCREEN_LAYOUT, screen);
	}
	

	private static final Field BUFFER_SOURCE_FIXED_BUFFERS = ObfuscationReflectionHelper.findField(MultiBufferSource.BufferSource.class, "fixedBuffers");
	public static SequencedMap<RenderType, ByteBufferBuilder> getFixedBuffers(MultiBufferSource.BufferSource bufferSource) {
		return ReflectionUtil.getFieldValue(BUFFER_SOURCE_FIXED_BUFFERS, bufferSource);
	}
	
	
	private static Map<String, KeyMapping> KEY_MAPPING_BY_NAME_CACHE;
	private static final Field KEY_MAPPING_ALL = ObfuscationReflectionHelper.findField(KeyMapping.class, "ALL");
	public static Map<String, KeyMapping> getKeyMappingMapByName() {
		if (KEY_MAPPING_BY_NAME_CACHE == null) {
			KEY_MAPPING_BY_NAME_CACHE = ReflectionUtil.getFieldValue(KEY_MAPPING_ALL, null);
		}
		return KEY_MAPPING_BY_NAME_CACHE;
	}
}
