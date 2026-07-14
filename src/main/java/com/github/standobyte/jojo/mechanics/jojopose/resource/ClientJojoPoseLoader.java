package com.github.standobyte.jojo.mechanics.jojopose.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.AnimVariantsList;
import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.mechanics.jojopose.resource.JojoPoseAnimSet.JojoPoseAnimData;
import com.github.standobyte.jojo.mechanics.jojopose.resource.JojoPoseAnimSet.JojoPoseAnimSetData;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Zone;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ProfilerFiller;
import com.google.gson.JsonObject;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class ClientJojoPoseLoader extends SimplePreparableReloadListener<Map<ResourceLocation, ClientJojoPoseLoader.PoseAnimSetPrep>> {
	private static ClientJojoPoseLoader instance;
	
	@ApiStatus.Internal
	public static void init(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new ClientJojoPoseLoader();
		}
//		event.addListener(JojoMod.resLoc("jojopose"), instance);
		event.registerReloadListener(instance);
	}
	
	public static ClientJojoPoseLoader getInstance() {
		return instance;
	}
	
	
	public Map<ResourceLocation, JojoPoseAnimSet> anims = new HashMap<>();
	
	@Nullable
	public JojoPoseAnimSet getAnimSet(ResourceLocation geckoAnimFilePath) {
		return anims.get(geckoAnimFilePath);
	}
	
	// shit code - don't call the two methods below too often, or make the query process better
	
	public Stream<Map.Entry<ResourceLocation, JojoPoseAnimSet>> getForCharacter(Holder<StoryCharacter> playerCharacter, @Nullable Holder<StoryPart> playerStoryPart) {
		if (playerCharacter == null) {
			return Stream.empty();
		}
		
		ResourceLocation storyPartId = playerStoryPart != null ? playerStoryPart.unwrapKey().map(ResourceKey::location).orElse(null) : null;
		return anims.entrySet().stream().filter(animSetEntry -> {
			JojoPoseAnimSet animSet = animSetEntry.getValue();
			JojoPoseAnimSetData data = animSet.data();
			
			@Nullable ResourceLocation characterFilter = data.character();
			@Nullable List<ResourceLocation> storyPartsFilter = data.storyPart();
			
			return (characterFilter == null || playerCharacter.is(characterFilter)) &&
					(storyPartsFilter == null || playerStoryPart != null && storyPartsFilter.contains(storyPartId));
		});
	}
	
	public Stream<JojoPoseAnim> getPosesForCharacter(Holder<StoryCharacter> playerCharacter, @Nullable Holder<StoryPart> playerStoryPart) {
		return getForCharacter(playerCharacter, playerStoryPart).flatMap(animSetEntry -> {
			ResourceLocation animSetId = animSetEntry.getKey();
			JojoPoseAnimSet animSet = animSetEntry.getValue();
			JojoPoseAnimSetData dataMap = animSet.data();
			return animSet.anims().namedAnimations.entrySet().stream().flatMap(animEntry -> {
				AnimVariantsList anims = animEntry.getValue();
				String animBaseName = animEntry.getKey();
				JojoPoseAnimData data = dataMap.getAnimSpecificData(animBaseName);
				
				return IntStream.range(0, anims.anims.size()).mapToObj(index -> new JojoPoseAnim(
						animSetId, animEntry.getKey(), index, anims.anims.get(index), data));
			});
		});
	}
	
	public static record JojoPoseAnim(ResourceLocation animSet, String animName, int animIndex, 
			RotpAnimDefinition anim, @Nullable JojoPoseAnimData data) {}
	
	

	private static final String TOP_DIR = "jojo_pose";
	private static final String FILE_EXT = ".json";
	private static final String ANIM_FILE_NAME = "animation.json";
	private static final String DATA_FILE_NAME = "data.json";
	
	@Override
	protected Map<ResourceLocation, PoseAnimSetPrep> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, PoseAnimSetPrep> animSetEntries = new HashMap<>();
		
		try (Zone zone = _ProfilerFiller.zone(profiler, JojoMod.MOD_ID + "_jojo_poses")) {
			Map<ResourceLocation, List<Resource>> animResources = resourceManager.listResourceStacks(TOP_DIR, path -> path.getPath().endsWith(FILE_EXT));
			for (var resourceEntry : animResources.entrySet()) {
				ResourceLocation resourcePathFull = resourceEntry.getKey();
				String[] path = resourcePathFull.getPath().split("/");
				String fileName = path[path.length - 1];
				ResourceLocation entryId = resourcePathFull.withPath(p -> p.substring(TOP_DIR.length() + 1, p.length() - fileName.length() - 1));
				switch (fileName) {
					case ANIM_FILE_NAME -> {
						PoseAnimSetPrep animSetEntry = animSetEntries.computeIfAbsent(entryId, __ -> new PoseAnimSetPrep());
						AnimationSet.Builder anim = AnimationLoader.loadAnimations(resourceEntry.getValue(), 
								resourcePathFull, true);
						if (!anim.isEmpty()) {
							animSetEntry.animSet = anim;
						}
					}
					case DATA_FILE_NAME -> {
						PoseAnimSetPrep animSetEntry = animSetEntries.computeIfAbsent(entryId, __ -> new PoseAnimSetPrep());
						for (var resource : resourceEntry.getValue()) {
							try (var reader = resource.openAsReader()) {
								JsonObject json = JSONUtil.parse(reader);
								Optional<JojoPoseAnimSetData> poseData = JSONUtil.fromJson(json, JojoPoseAnimSetData.CODEC);
								poseData.ifPresent(data -> animSetEntry.poseData = data);
							}
							catch (Exception e) {
								JojoMod.getLogger().error("Failed to read JoJo pose data from {}", resourcePathFull, e);
							}
						}
					}
				}
			}
		}
		
		return animSetEntries;
	}
	
	static class PoseAnimSetPrep {
		AnimationSet.Builder animSet;
		JojoPoseAnimSetData poseData;
	}
	
	
	@Override
	protected void apply(Map<ResourceLocation, PoseAnimSetPrep> prep, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.anims.clear();
		prep.forEach((key, loaded) -> {
			if (loaded.animSet != null) {
				this.anims.put(key, new JojoPoseAnimSet(
						loaded.animSet.build(), 
						Optional.ofNullable(loaded.poseData).orElse(JojoPoseAnimSet.EMPTY_DATA)));
			}
		});
		JojoMod.getLogger().info("Loaded {} JoJo pose files", this.anims.size());
	}

}
