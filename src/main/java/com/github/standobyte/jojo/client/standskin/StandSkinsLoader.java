package com.github.standobyte.jojo.client.standskin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ModClientResources;
import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel;
import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel.ModelFormat;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader.ModelFileFormatPath;
import com.github.standobyte.jojo.client.shader.core.ManualInitPostChain;
import com.github.standobyte.jojo.client.shader.core.ManualInitPostChain.PostChainDefinition;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmEngine;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo;
import com.github.standobyte.jojo.client.sound.bgmloop.DebugBgm;
import com.github.standobyte.jojo.client.sound.util.SoundEventDelegate;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.standskin.text.LanguagePrep;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.functions.JojoModUtil;
import com.github.standobyte.jojo.util.functions.StringUtil;
import com.github.standobyte.jojo.util.objects_mc.WeightsList;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Zone;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ProfilerFiller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.core.RegistryAccess;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class StandSkinsLoader implements PreparableReloadListener, AutoCloseable {
	private static StandSkinsLoader instance;
	public final AbilityIconSprites abilityIcons;
	
	@ApiStatus.Internal
	public static void init(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new StandSkinsLoader();
		}
//		ResourceLocation id = JojoMod.resLoc("standskins");
//		event.addListener(id, instance);
		ModClientResources.closeables.add(instance);
		event.registerReloadListener(instance);
	}
	
	protected StandSkinsLoader() {
		this.abilityIcons = new AbilityIconSprites(Minecraft.getInstance().getTextureManager());
		ModClientResources.closeables.add(abilityIcons);
	}
	
	public static StandSkinsLoader getInstance() {
		return instance;
	}
	
	@Nullable
	public static StandSkin getCurSkin() {
		StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
		if (standPower != null) {
			StandSkin skin = StandSkinsLoader.getInstance().getSkin(standPower);
			return skin;
		}
		return null;
	}
	
	@Override
	public void close() {
		discardOldSkins();
	}
	
	protected void discardOldSkins() {
		for (var skinsPerStand : skins.values()) {
			for (StandSkin skin : skinsPerStand.values()) {
				skin.discard();
			}
		}
	}

	
	private final Map<ResourceLocation, Map<ResourceLocation, StandSkin>> skins = new HashMap<>();
	private final Map<ResourceLocation, List<StandSkin>> skinsByStand = new HashMap<>();
	private final Map<ResourceLocation, StandSkin> defaultSkins = new HashMap<>();
	
	public StandSkin getDefaultSkin(ResourceLocation standId) {
		return defaultSkins.get(standId);
	}
	
	@Nullable
	public StandSkin getDefaultSkin(StandPower standPower) {
		return standPower != null ? getDefaultSkin(standPower.getPowerType().getId()) : null;
	}
	
	@Nullable
	public StandSkin getSkin(StandPower standPower) {
		if (standPower == null) return null;
		return standPower.getStandInstance().map(this::getSkin).orElse(null);
	}
	
	public StandSkin getSkin(StandInstance standInstance) {
		if (standInstance == null) return null;
		Optional<ResourceLocation> selectedSkin = standInstance.getSelectedSkin();
		ResourceLocation standId = standInstance.getStandId();
		return getSkinFromId(standId, selectedSkin);
	}
	
	public StandSkin getSkin(StandEntity standEntity) {
		return getSkinFromId(standEntity.getStandType(), standEntity.getStandSkin());
	}
	
	public StandSkin getSkinFromId(ResourceLocation standId, Optional<ResourceLocation> selectedSkin) {
		if (standId == null) return null;
		
		if (selectedSkin.isPresent()) {
			Map<ResourceLocation, StandSkin> standSkins = skins.get(standId);
			if (standSkins != null) {
				StandSkin skin = standSkins.getOrDefault(selectedSkin.get(), null);
				if (skin != null) {
					return skin;
				}
			}
		}
		
		return getDefaultSkin(standId);
	}
	
	public ResourceLocation getDefaultSkinId(StandType standType) {
		return standType.getId();
	}
	
	public List<StandSkin> getStandSkinsView(ResourceLocation standId) {
		List<StandSkin> skins = skinsByStand.get(standId);
		return skins != null ? skins : Collections.emptyList();
	}

	
	public static ResourceLocation remap(ResourceLocation assetPath, ResourceLocation skinId) {
		return remap.apply(assetPath, skinId);
	}
	protected static final BiFunction<ResourceLocation, ResourceLocation, ResourceLocation> remap = Util.memoize(
			(ResourceLocation assetPath, ResourceLocation skinId) -> ResourceLocation.fromNamespaceAndPath(
					skinId.getNamespace(), 
					"stand_skins/" + 
					skinId.getPath() + "/" + 
					"assets/" + 
					assetPath.getNamespace() + "/" + assetPath.getPath()));
	
	
	@Override
	public CompletableFuture<Void> reload(
			PreparableReloadListener.PreparationBarrier stage,
			ResourceManager resourceManager,
			ProfilerFiller preparationsProfiler,
			ProfilerFiller reloadProfiler,
			Executor backgroundExecutor,
			Executor gameExecutor) {
		return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager, preparationsProfiler), backgroundExecutor)
				.thenCompose(preps -> this.prepPost(preps, resourceManager, preparationsProfiler, backgroundExecutor))
				.thenCompose(stage::wait)
				.thenAcceptAsync(preps -> this.apply(preps, resourceManager, reloadProfiler), gameExecutor);
	}
	
	protected Preps prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		DebugBgm.clear(DebugBgm.BgmTrackType.STAND_SKINS);
		Preps preps = new Preps(this);

		try (Zone zone = _ProfilerFiller.zone(profiler, JojoMod.MOD_ID + "_stand_skins")) {
			Map<ResourceLocation, List<Resource>> allResourcesMap = resourceManager.listResourceStacks("stand_skins", path -> true);
			for (var resourceEntry : allResourcesMap.entrySet()) {
				/*
				 *				  	filePath[0]	 filePath[1]...	   		[2]	 	[3]			 [4]								      	[5]
				   assets/my_skins/stand_skins/cool_star_platinum_skin/assets/jojo_ripples/... (/textures/geo/animations/lang/sounds/...)/.../
							  ^						  ^							^													    ^
					   skin namespace				skin path				  in-mod namespace for assets used by the stand		    the actual asset file is most likely here
						(arbitrary*)				(arbitrary*)				  (must match the namespace from stand id)			     (or it is a subdirectory, and the assets further down the path)
						
					   the resourceEntry.getKey() will look like this:
				   my_skins:stand_skins/cool_star_platinum_skin/assets/jojo_ripples/...
					  
					   (*) - for the skin to be considered the "default" one, the skin's 'namespace:path' pair must be the same as in the stand's id
				   
				   
					   for the main file (with stand_type, color, etc.) the path is:
				   assets/my_skins/stand_skins/cool_star_platinum_skin/skin.json
				 */
				ResourceLocation filePath = resourceEntry.getKey();
				SkinResPath resPath = SkinResPath.fill(filePath);
				
				if (resPath.isMainSkinFile || resPath.isResource) {
					ResourceLocation skinId = ResourceLocation.fromNamespaceAndPath(filePath.getNamespace(), resPath.skinIdPath);
					StandSkinResourceBuilder skinBuilder = preps.skinsRead.computeIfAbsent(skinId, StandSkinResourceBuilder::new);
					if (resPath.isMainSkinFile) {
						for (var resource : resourceEntry.getValue()) {
							try (var reader = resource.openAsReader()) {
								JsonObject json = JSONUtil.parse(reader);
								loadSkinInfo(json, skinBuilder, JojoMod.getLogger());
							} catch (Exception e) {
								JojoMod.getLogger().error("Failed to read {} data for Stand skin {}", resPath.resourceType, skinId, e);
							}
						}
					}
					else {
						loadResource(resourceEntry.getValue(), resPath, skinBuilder, JojoMod.getLogger(), filePath, preps, resourceManager);
					}
				}
			}
		}
		
		return preps;
	}
	
	protected static class SkinResPath {
		public ResourceLocation filePath;
		public String[] pathByParts;
		public boolean isMainSkinFile;
		public boolean isResource;
		public String skinIdPath;
		public String resourceType;
		public String assetNamespace;
		public String assetType;
		public String assetPathWDirAndExtension;
		public String assetPathWExtension;
		static final SkinResPath instance = new SkinResPath();
		
		static SkinResPath fill(ResourceLocation filePath) {
			String[] split = filePath.getPath().split("/");
			instance.filePath = filePath;
			instance.pathByParts = split;
			instance.isMainSkinFile = split.length == 3 && "skin.json".equals(split[2]);
			instance.isResource = split.length >= 5 && "assets".equals(split[2]);

			instance.skinIdPath = split[1];
			instance.resourceType = split[2];
			if (instance.isResource) {
				instance.assetNamespace = split[3];
				instance.assetType = split[4];

				StringBuilder pathStr = new StringBuilder();
				StringBuilder pathDirStr = new StringBuilder(split[4] + "/");
				for (int i = 5; i < split.length; i++) {
					if (i > 5) {
						pathStr.append("/");
						pathDirStr.append("/");
					}
					pathStr.append(split[i]);
					pathDirStr.append(split[i]);
				}
				instance.assetPathWDirAndExtension = pathDirStr.toString();
				instance.assetPathWExtension = pathStr.toString();
			}
			else {
				instance.assetNamespace = null;
				instance.assetType = null;
				instance.assetPathWExtension = null;
			}
			
			return instance;
		}
		
		public String getResPathPart(int index) {
			return pathByParts[4 + index];
		}
		
		public String getFileName() {
			return pathByParts[pathByParts.length - 1];
		}
	}
	
	
	public static class Preps {
		protected final Map<ResourceLocation, StandSkinResourceBuilder> skinsRead = new HashMap<>();
		protected AbilityIconSprites.Preps abilitySprites;
		
		public Preps(StandSkinsLoader loader) {
			this.abilitySprites = new AbilityIconSprites.Preps(loader.abilityIcons);
		}
	}
	
	// XXX extensible stand skins?
	public static class StandSkinResourceBuilder {
		public final ResourceLocation skinId;
		public ResourceLocation standId;
		public StandSkinColor uiColor = null;
		public float[] scale = null;
		public Optional<ResourceLocation> storyPart = Optional.empty();
		public Map<ResourceLocation, LayerDefinition> models;
		public Map<ResourceLocation, AnimationSet.Builder> animations;
		public Map<ResourceLocation, WeighedSoundEvents> soundEvents;
		public Map<ResourceLocation, Pair<ResourceLocation, Resource>> soundFiles;
		public WeightsList<BgmTrackInfo> resolveBGM;
		public Map<ResourceLocation, PostChainDefinition> shaderChains;
		public Map<String, LanguagePrep> langFiles;
		public Language language;
		public String authors;
		
		public StandSkinResourceBuilder(ResourceLocation skinId) {
			this.skinId = skinId;
		}
		
		public boolean isValidSkin(Logger logger) {
			if (standId == null) {
				logger.error("Stand skin {} doesn't specify the Stand it belongs to! (Missing \"stand_type\")", skinId);
				return false;
			}
			return true;
		}
		
		public void createLang(List<String> langCodes, boolean defaultRightToLeft) {
			if (langFiles != null) {
				Map<String, String> translations = new HashMap<>();
				Map<String, Component> componentMap = new HashMap<>();
				for (String langCode : langCodes) {
					LanguagePrep langPrep = langFiles.get(langCode);
					if (langPrep != null) {
						translations.putAll(langPrep.storage);
						componentMap.putAll(langPrep.componentStorage);
					}
				}
				language = new ClientLanguage(Map.copyOf(translations), defaultRightToLeft, Map.copyOf(componentMap));
			}
		}
		
		public StandSkin makeSkin() {
			StandSkin skin = new StandSkin(skinId, standId, uiColor, storyPart);
			if (models != null) skin.withModels(models);
			if (animations != null) skin.withAnimations(animations);
			if (scale != null && scale.length >= 2) skin.withScale(scale[0], scale[1]);
			if (soundEvents != null) skin.withSoundEvents(soundEvents);
			if (soundFiles != null) skin.withSounds(soundFiles.entrySet().stream().collect(Collectors.toMap(
					Map.Entry::getKey, entry -> entry.getValue().getFirst())));
			if (resolveBGM != null && !resolveBGM.isEmpty()) skin.withResolveBGM(resolveBGM);
			if (shaderChains != null) skin.withShaders(shaderChains);
			if (language != null) skin.withLanguage(language);
			if (authors != null) skin.authors = JojoModUtil.makeAssetCredits(authors)
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
			return skin;
		}
	}
	
	
	private void loadSkinInfo(JsonObject skinInfoJson, StandSkinResourceBuilder builder, Logger logger) {
		ResourceLocation.CODEC.decode(JsonOps.INSTANCE, skinInfoJson.get("stand_type")).ifSuccess(res -> builder.standId = res.getFirst());
		
		if (skinInfoJson.has("color")) {
			builder.uiColor = StandSkinColor.fromJson(skinInfoJson.get("color"));
		}
		
		if (skinInfoJson.has("story_part")) {
			builder.storyPart = Optional.of(ResourceLocation.parse(skinInfoJson.get("story_part").getAsString()));
		}
		
		if (skinInfoJson.has("scale")) {
			JsonElement scaleJson = skinInfoJson.get("scale");
			if (scaleJson instanceof JsonArray jsonArray) {
				builder.scale = new float[] {
						jsonArray.get(0).getAsFloat(), 
						jsonArray.get(1).getAsFloat()
				};
			}
			else if (scaleJson instanceof JsonObject jsonObject) {
				builder.scale = new float[] {
						JSONUtil.getFloatOr("width", jsonObject, ClientUtil.DEFAULT_STAND_WIDTH), 
						JSONUtil.getFloatOr("height", jsonObject, ClientUtil.DEFAULT_STAND_HEIGHT)
				};
			}
		}
		
		if (skinInfoJson.has("authors")) {
			builder.authors = skinInfoJson.get("authors").getAsString();
		}
	}
	
	private static final String SOUND_EXTENSION = ".ogg";
	private void loadResource(List<Resource> resource, SkinResPath resPath, 
			StandSkinResourceBuilder builder, Logger logger, ResourceLocation fullFilePath, 
			Preps resourcePreps, ResourceManager resourceManager) {
		for (ModelFileFormatPath format : RotpGeckoModelLoader.STAND_SKIN_PATHS) {
			if (resPath.assetPathWDirAndExtension.startsWith(format.directory()) /* is in the correct directory */
					&& resPath.assetPathWDirAndExtension.endsWith(format.extension()) /* has the correct extension */ ) {
				readModel(resource, builder, resPath.assetNamespace, 
						resPath.assetPathWDirAndExtension.substring(format.directory().length() + 1) /* model path without the directory name */, 
						format.format(), format.extension());
				return;
			}
		}
		switch (resPath.assetType) {
			case "animations" -> {
				var json = readLastResource(resource, null, JSONUtil::parse, builder.skinId, resPath.assetNamespace, resPath.assetPathWExtension, ".animation.json");
				if (json != null) {
					if (builder.animations == null) builder.animations = new HashMap<>();
					ResourceLocation path = json.getFirst();
					AnimationLoader.addAnimsToAnimSet(json.getSecond(), builder.animations.computeIfAbsent(path, __ -> new AnimationSet.Builder(false, true)), path);
				}
			}
			case "textures" -> {
				switch (resPath.getResPathPart(1)) {
					case AbilityIconSprites.DIR_NAME -> {
						String spriteName = StringUtil.substrBack(resPath.getResPathPart(2), ".png".length());
						resourcePreps.abilitySprites.addSkinSprite(builder.skinId, spriteName, resPath.filePath);
					}
				}
			}
			case "lang" -> {
				loadLangFile(resource, builder, resPath.assetPathWExtension);
			}
			case "sounds" -> {
				if (resPath.assetPathWExtension.endsWith(SOUND_EXTENSION)) {
					if (builder.soundFiles == null) {
						builder.soundFiles = new HashMap<>();
					}
					ResourceLocation soundLocation = ResourceLocation.fromNamespaceAndPath(resPath.assetNamespace, 
							StringUtil.substrBack(resPath.assetPathWExtension, SOUND_EXTENSION.length()));
					Resource soundResource = getLastResource(resource);
					builder.soundFiles.put(soundLocation, Pair.of(fullFilePath, soundResource));
				}
			}
			case "sounds.json" -> {
				loadSoundsJson(resource, builder, resPath.assetNamespace);
			}
			// XXX (resolve BGM) different tracks on different resolve levels
			case "bgm" -> {
				if ("resolve.json".equals(resPath.getFileName())) {
					try {
						builder.resolveBGM = BgmEngine.parse(getLastResource(resource), resourceManager);
						DebugBgm.onLoad(DebugBgm.BgmTrackType.STAND_SKINS, builder.skinId, builder.resolveBGM);
					} catch (IOException e) {
						JojoMod.getLogger().warn("Failed to load BGM definition {} in Stand skin: '{}'", resPath.assetPathWExtension, builder.skinId, e);
					}
				}
			}
			case "shaders" -> {
				if (builder.shaderChains == null) {
					builder.shaderChains = new HashMap<>();
				}
				var read = readLastResource(resource, null, JSONUtil::parse, builder.skinId, 
						resPath.assetNamespace, resPath.assetPathWExtension, ".json");
				JsonObject json = read.getSecond();
				PostChainDefinition shader = ManualInitPostChain.parsePostChain(json);
				ResourceLocation shaderId = read.getFirst();
				builder.shaderChains.put(shaderId, shader);
			}
			default -> {}
		}
	}
	
	static Resource getLastResource(List<Resource> resourceStack) {
		return resourceStack.get(resourceStack.size() - 1);
	}
	
	
	@Nullable
	private <T> Pair<ResourceLocation, T> readLastResource(List<Resource> resource, 
			@Nullable Function<Resource, T> add, @Nullable Function<BufferedReader, T> read, 
			ResourceLocation skinId, String resNamespace, String resPathWithExt, String... fileExtensions) {
		Pair<ResourceLocation, List<T>> resourceRead = readResources(
				Collections.singletonList(getLastResource(resource)), 
				add, read, skinId, resNamespace, resPathWithExt, fileExtensions);
		return resourceRead != null ? resourceRead.mapSecond(list -> list.get(0)) : null;
	}
	
	@Nullable
	private <T> Pair<ResourceLocation, List<T>> readResources(List<Resource> resource, 
			@Nullable Function<Resource, T> addResource, @Nullable Function<BufferedReader, T> orReadResource, 
			ResourceLocation skinId, String resNamespace, String resPathWithExt, String... fileExtensions) {
		if (resource.isEmpty()) {
			throw new IllegalArgumentException();
		}
		if (fileExtensions.length == 0) {
			ResourceLocation path = ResourceLocation.fromNamespaceAndPath(resNamespace, resPathWithExt);
			return readResources(resource, addResource, orReadResource, skinId, path).map(res -> Pair.of(path, res)).orElse(null);
		}
		else {
			for (String ext : fileExtensions) {
				if (resPathWithExt.endsWith(ext)) {
					ResourceLocation path = ResourceLocation.fromNamespaceAndPath(resNamespace, StringUtil.substrBack(resPathWithExt, ext.length()));
					return readResources(resource, addResource, orReadResource, skinId, path).map(res -> Pair.of(path, res)).orElse(null);
				}
			}
		}
		return null;
	}
	
	private <T> Optional<List<T>> readResources(List<Resource> resource, 
			@Nullable Function<Resource, T> addResource, @Nullable Function<BufferedReader, T> orReadResource, 
			ResourceLocation skinId, ResourceLocation path) {
		List<T> jsons = new ArrayList<>();
		for (Resource file : resource) {
			if (addResource != null) {
				try {
					T element = addResource.apply(file);
					Objects.requireNonNull(element);
					jsons.add(element);
				} catch (Exception e) {
					JojoMod.getLogger().warn("Failed to add resource {} for Stand skin {}", path, skinId, e);
				}
			}
			else if (orReadResource != null) {
				try (var reader = file.openAsReader()) {
					T element = orReadResource.apply(reader);
					Objects.requireNonNull(element);
					jsons.add(element);
				} catch (Exception e) {
					JojoMod.getLogger().warn("Failed to read {} for Stand skin {}", path, skinId, e);
				}
			}
			
		}
		
		return jsons.isEmpty() ? Optional.empty() : Optional.of(jsons);
	}
	
	
	private void readModel(List<Resource> resource, StandSkinResourceBuilder builder, 
			String resNamespace, String resPathWithExt, ModelFormat modelFormat, String... fileExtensions) {
		var json = readLastResource(resource, null, JSONUtil::parse, builder.skinId, resNamespace, resPathWithExt, fileExtensions);
		if (json != null) {
			var modelDefinition = ParseModEntityModel.parse(json.getSecond(), modelFormat);
			if (builder.models == null) builder.models = new HashMap<>();
			builder.models.put(json.getFirst(), modelDefinition);
		}
	}
	

	private void loadLangFile(List<Resource> resource, StandSkinResourceBuilder standSkinBuilder, String fileName) {
		String langCode = StringUtil.trimEnding(fileName, ".json");
		
		if (standSkinBuilder.langFiles == null) {
			standSkinBuilder.langFiles = new HashMap<>();
		}
		standSkinBuilder.langFiles.put(langCode, LanguagePrep.fromJsonFile(resource, langCode, standSkinBuilder.skinId));
	}
	
	private static final String WHAT_THE_FUCK_IS_A_KILOMETER = "en_us";
	private List<String> langCodesToLoad = new ArrayList<>();
	private boolean langDefaultRightToLeft;
	private void updateRelevantLangCodes() {
		LanguageManager langManager = Minecraft.getInstance().getLanguageManager();
		String currentCode = langManager.getSelected();
		Map<String, LanguageInfo> languages = langManager.getLanguages();

		langCodesToLoad.clear();
		langCodesToLoad.add(WHAT_THE_FUCK_IS_A_KILOMETER);
		langDefaultRightToLeft = languages.get(WHAT_THE_FUCK_IS_A_KILOMETER).bidirectional();
		if (!currentCode.equals(WHAT_THE_FUCK_IS_A_KILOMETER)) {
			LanguageInfo languageInfo = languages.get(currentCode);
			if (languageInfo != null) {
				langCodesToLoad.add(currentCode);
				langDefaultRightToLeft = languageInfo.bidirectional();
			}
		}
	}

	
	private static final Gson LOAD_SOUND_EVENTS_GSON = new GsonBuilder()
			.registerTypeHierarchyAdapter(Component.class, new Component.SerializerAdapter(RegistryAccess.EMPTY))
			.registerTypeAdapter(SoundEventRegistration.class, new SoundEventRegistrationSerializer())
			.create();
	private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundEventRegistration>>() {};
	
	private void loadSoundsJson(List<Resource> soundsJsonsRes, StandSkinResourceBuilder standSkin, String namespace) {
		Map<String, SoundEventRegistration> soundsReg = new HashMap<>();
		var readSoundsJsons = readResources(soundsJsonsRes, 
				null, JSONUtil::parse, standSkin.skinId, 
				standSkin.skinId.withPath("sounds.json"));
		readSoundsJsons.ifPresent(jsons -> {
			for (var json : jsons) {
				soundsReg.putAll(LOAD_SOUND_EVENTS_GSON.fromJson(json, SOUND_EVENT_REGISTRATION_TYPE));
			}
		});
		if (soundsReg.isEmpty()) return;
		if (standSkin.soundEvents == null) {
			standSkin.soundEvents = new HashMap<>();
		}
		for (var soundEventReg : soundsReg.entrySet()) {
			String soundName = soundEventReg.getKey();
			ResourceLocation soundEventLocation = ResourceLocation.fromNamespaceAndPath(namespace, soundName);
			SoundEventRegistration registration = soundEventReg.getValue();
			WeighedSoundEvents soundEvent = standSkin.soundEvents.computeIfAbsent(
					soundEventLocation, loc -> new WeighedSoundEvents(loc, registration.getSubtitle()));
			if (!registration.isReplace()) {
				JojoMod.getLogger().warn("Stand skin sounds do not support \'\"replace\" = false\' option (sound event {} from Stand skin {})", soundName, standSkin.skinId);
			}
			for (final Sound sound : registration.getSounds()) {
				final ResourceLocation soundLocation = sound.getLocation();
				Weighted<Sound> weighted;
				switch (sound.getType()) {
					case FILE:
						weighted = sound;
						break;
					case SOUND_EVENT:
						weighted = new SoundEventDelegate(soundLocation);
						break;
					default:
						throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.getType());
				}

				soundEvent.addSound(weighted);
			}
		}
	}
	
	
	protected CompletableFuture<Preps> prepPost(Preps preps, ResourceManager resourceManager, 
			ProfilerFiller profiler, Executor backgroundExecutor) {
		return preps.abilitySprites.stitch(resourceManager, backgroundExecutor).thenApply(spritePreps -> preps);
	}
	
	
	protected void apply(Preps preps, ResourceManager resourceManager, ProfilerFiller profiler) {
		discardOldSkins();
		updateRelevantLangCodes();
		
		Minecraft mc = Minecraft.getInstance();
		SoundManager soundManager = mc.getSoundManager();
		Map<ResourceLocation, Resource> soundCache = soundManager.soundCache;
		SoundEngine soundEngine = soundManager.soundEngine;
		
		this.skins.clear();
		for (var skinBuilder : preps.skinsRead.values()) {
			if (skinBuilder.isValidSkin(JojoMod.getLogger())) {
				// doing this at this stage to make sure the LanguageManager has LanguageInfo on non-freedom languages during game startup
				skinBuilder.createLang(langCodesToLoad, langDefaultRightToLeft);
				// XXX move the makeSkin call to prepare?
				StandSkin skin = skinBuilder.makeSkin();
				this.skins.computeIfAbsent(skinBuilder.standId, __ -> new HashMap<>()).put(skinBuilder.skinId, skin);
				if (skin.skinId.equals(skinBuilder.standId)) {
					this.defaultSkins.put(skinBuilder.standId, skin);
				}
				
				if (skinBuilder.soundFiles != null) {
					skinBuilder.soundFiles.values().forEach(soundEntry -> {
						soundCache.put(soundEntry.getFirst(), soundEntry.getSecond());
					});
				}
				if (skinBuilder.soundEvents != null) {
					skinBuilder.soundEvents.values().forEach(soundEvent -> {
						soundEvent.preloadIfRequired(soundEngine);
					});
				}
			}
		}
		sortSkins();
		JojoMod.getLogger().info("Loaded {} Stand skins", skins.size());
		preps.abilitySprites.apply(resourceManager, profiler);
	}
	
	private void sortSkins() {
		this.skinsByStand.clear();
		for (var standSkinsEntry : skins.entrySet()) {
			ResourceLocation standId = standSkinsEntry.getKey();
			List<StandSkin> standSkins = standSkinsEntry.getValue().values().stream()
					.sorted(Comparator.<StandSkin>
						comparingInt(skin -> getSkinPriority(skin, standId))
						.thenComparing(skin -> skin.skinId))
					.toList();
			
			StandSkin defaultStandSkin = this.getDefaultSkin(standId);
			for (StandSkin skin : standSkins) {
				skin.defaultSkin = defaultStandSkin;
			}
			
			skinsByStand.put(standId, Collections.unmodifiableList(standSkins));
		}
	}
	
	private static int getSkinPriority(StandSkin skin, ResourceLocation standId) {
		if (standId.equals(skin.skinId)) return 1;
		if (standId.getNamespace().equals(skin.skinId.getNamespace())) return 2;
		return 3;
	}
	
}
