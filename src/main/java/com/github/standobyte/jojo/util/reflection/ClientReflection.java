package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

public final class ClientReflection {

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
