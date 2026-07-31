package com.github.standobyte.jojo.mechanics.jojopose.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.AnimVariantsList;
import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.mechanics.voiceline.ClientVoiceLineDefinition;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.functions.CodecUtil;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Zone;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ProfilerFiller;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
	
	
	public Map<ResourceLocation, JojoPoseAnimSet2> anims = new HashMap<>();
	
	@Nullable
	public JojoPoseAnimSet2 getAnimSet(ResourceLocation geckoAnimFilePath) {
		return anims.get(geckoAnimFilePath);
	}
	
	// shit code - don't call the method below too often, or make the query process better
	
	public Stream<JojoPose> getPosesForCharacter(Holder<StoryCharacter> playerCharacter, @Nullable Holder<StoryPart> playerStoryPart) {
		if (playerCharacter == null) {
			return Stream.empty();
		}
		
		ResourceLocation storyPartId = playerStoryPart != null ? playerStoryPart.unwrapKey().map(ResourceKey::location).orElse(null) : null;
		return anims.entrySet().stream()
				.filter(animSetEntry -> {
					JojoPoseAnimSet2 animSet = animSetEntry.getValue();

					@Nullable ResourceLocation characterFilter = animSet.character;
					@Nullable List<ResourceLocation> storyPartsFilter = animSet.storyPart;

					return (characterFilter == null || playerCharacter.is(characterFilter)) &&
							(storyPartsFilter == null || playerStoryPart != null && storyPartsFilter.contains(storyPartId));
				})
				.flatMap(animSetEntry -> {
					JojoPoseAnimSet2 animSet = animSetEntry.getValue();
					return animSet.anims.values().stream();
				});
	}
	
	

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
								resourcePathFull, true, false);
						if (!anim.isEmpty()) {
							animSetEntry.animSet = anim;
						}
					}
					case DATA_FILE_NAME -> {
						PoseAnimSetPrep animSetEntry = animSetEntries.computeIfAbsent(entryId, __ -> new PoseAnimSetPrep());
						for (var resource : resourceEntry.getValue()) {
							try (var reader = resource.openAsReader()) {
								JsonObject json = JSONUtil.parse(reader);
								Optional<JojoPoseAnimSetDataPrep> poseData = JSONUtil.fromJson(json, JojoPoseAnimSetDataPrep.CODEC);
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
		JojoPoseAnimSetDataPrep poseData;
	}
	
	static record JojoPoseAnimSetDataPrep(
			Optional<ResourceLocation> character, 
			Optional<List<ResourceLocation>> storyPart, 
			Optional<String> authors, 
			Optional<Map<String, JojoPoseAnimDataPrep>> animSpecificData) {

		public static final Codec<JojoPoseAnimSetDataPrep> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						ResourceLocation.CODEC.optionalFieldOf("character").forGetter(JojoPoseAnimSetDataPrep::character),
						CodecUtil.listOrSingleCodec(ResourceLocation.CODEC).optionalFieldOf("story_part").forGetter(JojoPoseAnimSetDataPrep::storyPart),
						Codec.STRING.optionalFieldOf("authors").forGetter(JojoPoseAnimSetDataPrep::authors),
						Codec.unboundedMap(Codec.STRING, JojoPoseAnimDataPrep.CODEC).optionalFieldOf("anim_specific").forGetter(JojoPoseAnimSetDataPrep::animSpecificData))
				.apply(builder, JojoPoseAnimSetDataPrep::new));
		
		@Nullable
		public JojoPoseAnimDataPrep getAnimSpecificData(String animName) {
			return animSpecificData.isPresent() ? animSpecificData.get().get(animName) : null;
		}
	}
	
	static record JojoPoseAnimDataPrep(
			Optional<JojoPose.WithStandSummonPose> standSummonPose,
			Optional<List<ClientVoiceLineDefinition>> voiceLine,
			Optional<String> authors) {
		public static final JojoPoseAnimDataPrep EMPTY_DATA = new JojoPoseAnimDataPrep(Optional.empty(), Optional.empty(), Optional.empty());
		
		public static final Codec<JojoPoseAnimDataPrep> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						JojoPose.WithStandSummonPose.CODEC.optionalFieldOf("stand_summon_pose").forGetter(JojoPoseAnimDataPrep::standSummonPose),
						CodecUtil.listOrSingleCodec(ClientVoiceLineDefinition.CODEC).optionalFieldOf("voice_line").forGetter(JojoPoseAnimDataPrep::voiceLine),
						Codec.STRING.optionalFieldOf("authors").forGetter(JojoPoseAnimDataPrep::authors))
				.apply(builder, JojoPoseAnimDataPrep::new));
		
	}
	
	
	@Override
	protected void apply(Map<ResourceLocation, PoseAnimSetPrep> prep, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.anims.clear();
		prep.forEach((animSetId, loaded) -> {
			if (loaded.animSet != null) {
				Map<String, AnimVariantsList> anims = loaded.animSet.build().namedAnimations;
				@Nullable ResourceLocation character = loaded.poseData != null ? loaded.poseData.character.orElse(null) : null;
				@Nullable List<ResourceLocation> storyPart = loaded.poseData != null ? loaded.poseData.storyPart.orElse(null) : null;
				JojoPoseAnimSet2 animSet = new JojoPoseAnimSet2(character, storyPart);
				this.anims.put(animSetId, animSet);
				for (var animEntry : anims.entrySet()) {
					String animName = animEntry.getKey();
					RotpAnimDefinition anim = animEntry.getValue().getSingle();
					JojoPoseAnimDataPrep poseData = loaded.poseData != null ? loaded.poseData.getAnimSpecificData(animName) : null;
					if (poseData == null) poseData = JojoPoseAnimDataPrep.EMPTY_DATA;
					try {
						Optional<String> authors = poseData.authors.or(() ->
								Optional.ofNullable(loaded.poseData).flatMap(animSetData -> animSetData.authors));
						
						JojoPose pose = new JojoPose(animSetId, animName, 
								anim, 
								poseData.standSummonPose.orElse(null),
								poseData.voiceLine.orElse(null),
								authors);
						animSet.anims.put(animName, pose);
					}
					catch (Exception e) {
						JojoMod.LOGGER.error("Failed to load a JoJo pose {}#{}", animSetId, animName, e);
					}
				}
			}
		});
		JojoMod.getLogger().info("Loaded {} JoJo pose files", this.anims.size());
	}

}
