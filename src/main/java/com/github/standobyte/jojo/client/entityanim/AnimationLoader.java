package com.github.standobyte.jojo.client.entityanim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.gecko.ParseGeckoAnims;
import com.github.standobyte.jojo.client.entityanim.molang.KeyframesMolangEngine;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.functions.StringUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Zone;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ProfilerFiller;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class AnimationLoader extends SimplePreparableReloadListener<Map<ResourceLocation, AnimationSet.Builder>> {
	private static AnimationLoader instance;
	
	@ApiStatus.Internal
	public static void init(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new AnimationLoader();
		}
//		event.addListener(JojoMod.resLoc("entityanim"), instance);
		event.registerReloadListener(instance);
		KeyframesMolangEngine.init();
	}
	
	public static AnimationLoader getInstance() {
		return instance;
	}
	
	
	private Map<ResourceLocation, AnimationSet> anims = new HashMap<>();
	
	@Nullable
	public AnimationSet getAnimSet(ResourceLocation geckoAnimFilePath) {
		return anims.get(geckoAnimFilePath);
	}
	

	private static final String TOP_DIR = "animations";
	private static final String EXTENSION = ".animation.json";
	
	@Override
	protected Map<ResourceLocation, AnimationSet.Builder> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, AnimationSet.Builder> anims = new HashMap<>();

		try (Zone zone = _ProfilerFiller.zone(profiler, JojoMod.MOD_ID + "_animations")) {
			Map<ResourceLocation, List<Resource>> resources = resourceManager.listResourceStacks(TOP_DIR, path -> path.getPath().endsWith(EXTENSION));
			for (var resourceEntry : resources.entrySet()) {
				ResourceLocation resourcePathFull = resourceEntry.getKey();
				ResourceLocation animPath = resourcePathFull.withPath(
						StringUtil.trimEnding(resourceEntry.getKey().getPath(), EXTENSION).substring(TOP_DIR.length() + 1));
				AnimationSet.Builder anim = loadAnimations(resourceEntry.getValue(), resourcePathFull, false, true);
				if (!anim.isEmpty()) {
					anims.put(animPath, anim);
				}
			}
		}
		
		return anims;
	}
	
	public static AnimationSet.Builder loadAnimations(List<Resource> resources, 
			ResourceLocation resourcePath, boolean preserveOrder, boolean groupByName) {
		AnimationSet.Builder animationSet = new AnimationSet.Builder(preserveOrder, groupByName);
		for (var animFile : resources) {
			try (var reader = animFile.openAsReader()) {
				JsonObject json = JSONUtil.parse(reader);
				addAnimsToAnimSet(json, animationSet, resourcePath);
			}
			catch (Exception e) {
				JojoMod.getLogger().error("Failed to read animations from {}", resourcePath, e);
			}
		}
		return animationSet;
	}
	
	public static void addAnimsToAnimSet(JsonObject json, AnimationSet.Builder animSetBuilder, ResourceLocation resPath) {
		JsonObject modelAnimsJson = json.getAsJsonObject().getAsJsonObject("animations");
		for (Map.Entry<String, JsonElement> animJsonEntry : modelAnimsJson.entrySet()) {
			String animName = animJsonEntry.getKey();
			if (!animName.startsWith("unused#")) {
				try {
					JsonObject animJson = animJsonEntry.getValue().getAsJsonObject();
					RotpAnimDefinition anim = ParseGeckoAnims.parseAnim(animName, animJson);
					if (animName.startsWith("always")) {
						animSetBuilder.addAlwaysAnim(anim);
					}
					else {
						animSetBuilder.putNamedAnim(animName, anim);
					}
				}
				catch (Exception e) {
					JojoMod.getLogger().error("Failed to load animation {} from {}", animName, resPath, e);
					continue;
				}
			}
		}
	}
	
	
	@Override
	protected void apply(Map<ResourceLocation, AnimationSet.Builder> skinsRead, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.anims.clear();
		skinsRead.forEach((key, animBuilder) -> {
			this.anims.put(key, animBuilder.build());
		});
		JojoMod.getLogger().info("Loaded {} entity animation files", this.anims.size());
	}
	
}
