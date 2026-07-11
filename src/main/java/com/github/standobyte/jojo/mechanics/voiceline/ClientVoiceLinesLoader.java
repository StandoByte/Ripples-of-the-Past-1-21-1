package com.github.standobyte.jojo.mechanics.voiceline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.functions.UtilFunctions;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Zone;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ProfilerFiller;
import com.google.gson.JsonObject;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

// just one more resource loader bro
public class ClientVoiceLinesLoader extends SimplePreparableReloadListener<Map<ResourceLocation, ClientVoiceLinesLoader.CharacterEntryPrep>> {
	private static ClientVoiceLinesLoader instance;
	
	@ApiStatus.Internal
	public static void init(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new ClientVoiceLinesLoader();
		}
//		event.addListener(JojoMod.resLoc("voiceline"), instance);
		event.registerReloadListener(instance);
	}
	
	public static ClientVoiceLinesLoader getInstance() {
		return instance;
	}
	
	
	public Map<ResourceLocation, List<ClientVoiceLineWithFilters>> voiceLines = new HashMap<>();
	
	public Stream<ClientVoiceLineDefinition> getVoiceLine(Holder<SoundEvent> soundEvent, 
			Holder<StoryCharacter> playerCharacter, 
			@Nullable Holder<StoryPart> playerStoryPart,
			@Nullable ResourceLocation playerStandType) {
		if (soundEvent == null || playerCharacter == null) {
			return Stream.empty();
		}
		
		ResourceLocation soundEventId = UtilFunctions.getId(soundEvent);
		if (soundEventId == null) {
			return Stream.empty();
		}
		
		List<ClientVoiceLineWithFilters> sounds = voiceLines.get(soundEventId);
		if (sounds == null) {
			return Stream.empty();
		}
		
		ResourceLocation playerStoryPartId = UtilFunctions.getId(playerStoryPart);
		// FIXME this kind of filtering sucks for things like stand summon callouts, where A LOT of characters & most stand types will have them
		return sounds.stream().filter(voiceLine -> {
			@Nullable List<ResourceLocation> storyPartsFilter = voiceLine.storyPartsFilter();
			@Nullable List<ResourceLocation> standTypesFilter = voiceLine.standTypesFilter();
			
			return playerCharacter.is(voiceLine.storyCharacter()) && 
					(storyPartsFilter == null || playerStoryPart != null && storyPartsFilter.contains(playerStoryPartId)) &&
					(standTypesFilter == null || playerStandType != null && standTypesFilter.contains(playerStandType));
		}).map(ClientVoiceLineWithFilters::sounds);
	}
	

	private static final String TOP_DIR = "voicelines";
	private static final String FILE_EXT = ".json";
	private static final String SOUNDS_FILE_NAME = "sounds.json";
	private static final String DATA_FILE_NAME = "data.json";
	
	@Override
	protected Map<ResourceLocation, CharacterEntryPrep> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, CharacterEntryPrep> voiceLineEntries = new HashMap<>();

		try (Zone zone = _ProfilerFiller.zone(profiler, JojoMod.MOD_ID + "_voice_lines")) {
			Map<ResourceLocation, List<Resource>> jsonResources = resourceManager.listResourceStacks(TOP_DIR, path -> path.getPath().endsWith(FILE_EXT));
			for (var resourceEntry : jsonResources.entrySet()) {
				ResourceLocation resourcePathFull = resourceEntry.getKey();
				String namespace = resourcePathFull.getNamespace();
				String[] path = resourcePathFull.getPath().split("/");
				String fileName = path[path.length - 1];
				ResourceLocation entryId = resourcePathFull.withPath(p -> p.substring(TOP_DIR.length() + 1, p.length() - fileName.length() - 1));
				switch (fileName) {
					case SOUNDS_FILE_NAME -> {
						CharacterEntryPrep entry = voiceLineEntries.computeIfAbsent(entryId, CharacterEntryPrep::new /*character id defaults to the directory path*/);
						for (var resource : resourceEntry.getValue()) {
							try (var reader = resource.openAsReader()) {
								JsonObject json = JSONUtil.parse(reader);
								for (var soundEventEntry : json.entrySet()) {
									ResourceLocation soundEventId = ResourceLocation.fromNamespaceAndPath(namespace, soundEventEntry.getKey());
									JSONUtil.parseArrayOrSingleElement(soundEventEntry.getValue(), _soundEventJson -> {
										JsonObject soundEventJson = _soundEventJson.getAsJsonObject();
										SoundEventPrep soundEvent = new SoundEventPrep();
										entry.sounds.put(soundEventId, soundEvent);
										
										if (soundEventJson.has("subtitle")) {
											soundEvent.subtitle = Component.translatable(soundEventJson.get("subtitle").getAsString());
										}
										if (soundEventJson.has("stand_type")) {
											soundEvent.standTypeFilter = JSONUtil.parseArrayOrSingleElement(
													soundEventJson.get("stand_type"), e -> ResourceLocation.parse(e.getAsString()));
										}
										soundEvent.sounds.addAll(JSONUtil.parseArrayOrSingleElement(
												soundEventJson.get("sounds"), e -> ResourceLocation.parse(e.getAsString())));
										
										return null;
									});
								}
							}
							catch (Exception e) {
								JojoMod.getLogger().error("Failed to read voice line sounds definition from {}", resourcePathFull, e);
							}
						}
					}
					case DATA_FILE_NAME -> {
						CharacterEntryPrep entry = voiceLineEntries.computeIfAbsent(entryId, CharacterEntryPrep::new);
						for (var resource : resourceEntry.getValue()) {
							try (var reader = resource.openAsReader()) {
								JsonObject json = JSONUtil.parse(reader);
								if (json.has("character")) {
									entry.characterId = ResourceLocation.parse(json.get("character").getAsString());
								}
								if (json.has("story_part")) {
									entry.storyPartFilter = JSONUtil.parseArrayOrSingleElement(
											json.get("story_part"), e -> ResourceLocation.parse(e.getAsString()));
								}
							}
							catch (Exception e) {
								JojoMod.getLogger().error("Failed to read voice line sounds definition from {}", resourcePathFull, e);
							}
						}
					}
				}
			}
		}
		
		return voiceLineEntries;
	}
	
	static class CharacterEntryPrep {
		@Nonnull ResourceLocation characterId;
		@Nullable List<ResourceLocation> storyPartFilter;
		
		final Map<ResourceLocation, SoundEventPrep> sounds = new HashMap<>();
		
		public CharacterEntryPrep(ResourceLocation characterId) { 
			this.characterId = characterId;
		}
	}
	
	static class SoundEventPrep {
		@Nullable List<ResourceLocation> standTypeFilter;
		@Nullable Component subtitle;
		
		final List<ResourceLocation> sounds = new ArrayList<>();
	}
	
	
	@Override
	protected void apply(Map<ResourceLocation, CharacterEntryPrep> prep, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.voiceLines.clear();
		prep.values().forEach(entry -> {
			entry.sounds.forEach((soundEvent, sound) -> {
				ClientVoiceLineDefinition voiceLineSound = new ClientVoiceLineDefinition(
						sound.sounds,
						sound.subtitle);
				ClientVoiceLineWithFilters filters = new ClientVoiceLineWithFilters(
						voiceLineSound,
						entry.characterId, 
						entry.storyPartFilter, 
						sound.standTypeFilter);
				this.voiceLines.computeIfAbsent(soundEvent, __ -> new ArrayList<>()).add(filters);
			});
		});
		JojoMod.getLogger().info("Loaded {} voice line entries", prep.size());
	}

}
