package com.github.standobyte.jojo.client;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.github.standobyte.jojo.client.shader.ModShaders;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmEngine;
import com.github.standobyte.jojo.client.sound.util.SoundCache;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.client.layer.ClothesModelLoader;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ModClientResources {

	@SubscribeEvent
	public static void registerResourceLoaders(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
//		event.addListener(JojoMod.resLoc("resource_check"), new ResourcePathChecker.ResourceReloadNotifier());
		event.registerReloadListener(new ResourcePathChecker.ResourceReloadNotifier());
		RotpGeckoModelLoader.init(event);
		StandSkinsLoader.init(event);
		AnimationLoader.init(event);
		ClothesModelLoader.init(event);
		ModShaders.init(event);
		BgmEngine.init(event);
	}
	
	@SubscribeEvent
	public static void onResourceReload(SoundEngineLoadEvent event) {
		SoundCache.getInstance().onResourceReload(event);
	}
	
	public static Set<AutoCloseable> closeables = new HashSet<>();
}
