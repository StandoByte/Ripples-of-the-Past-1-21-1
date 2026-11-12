package com.github.standobyte.jojo.config.stand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.interfaces.CommonConfig;
import com.github.standobyte.jojo.config.interfaces.fileimpl.CommonFileConfig;
import com.github.standobyte.jojo.config.internal.ConfigOptionsMap;
import com.github.standobyte.jojo.config.internal.ObjectConfigHelper;
import com.github.standobyte.jojo.config.stand.datapack.DataDrivenStandsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandStats.StatWithValue;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.functions.StringUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;

public class StandCommonConfig implements CommonConfig<StandType> {
	public final ResourceLocation standId;
	public final Map<StatWithValue, ConfigOption<?>> statConfigs;
	public final Map<String, Map<String, ConfigOption<?>>> abilityConfigs;
	public final ObjectConfigHelper<StandType> configState;
	public final File standDir;
	
	protected Map<Path, JsonElement> lastSavedState = new HashMap<>();
	protected Map<Path, JsonElement> savingState = new HashMap<>();
	
	protected static final String ABILITY_CFG_SEPARATOR = "#";

	protected StandCommonConfig(Dist environment, StandType standType,
			Map<StatWithValue, ConfigOption<?>> statConfigs,
			Map<String, Map<String, ConfigOption<?>>> abilityConfigs) {
		this.statConfigs = statConfigs;
		this.abilityConfigs = abilityConfigs;
		Map<String, ConfigOption<?>> allOptionsByName = new HashMap<>();
		
		for (var statConfigEntry : statConfigs.entrySet()) {
			String fieldName = statConfigEntry.getKey().nameInJson;
			ConfigOption<?> configOption = statConfigEntry.getValue();
			allOptionsByName.put(fieldName, configOption);
		}
		
		for (var configurableAbility : abilityConfigs.entrySet()) {
			String abilityName = configurableAbility.getKey();
			for (var abilityConfigEntry : configurableAbility.getValue().entrySet()) {
				String fieldName = abilityConfigEntry.getKey();
				ConfigOption<?> configOption = abilityConfigEntry.getValue();
				fieldName = abilityName + ABILITY_CFG_SEPARATOR + fieldName;
				allOptionsByName.put(fieldName, configOption);
			}
		}

		ConfigOptionsMap configOptionsMap = ConfigOptionsMap.create(allOptionsByName);
		this.configState = new ObjectConfigHelper<>(standType, configOptionsMap);

		File mainDirectory = ModConfig.getMainDirectory(environment);
		File standsDirectory = new File(mainDirectory, ObjectConfigHelper.DIRECTORY + "stands/");
		this.standId = standType.getId();
		this.standDir = new File(standsDirectory, standId.getNamespace() + "/" + standId.getPath());
	}

	public static StandCommonConfig makeConfig(Dist environment, StandType standType) {
		StandStats stats = standType.getStandStats();
		
		Map<StatWithValue, ConfigOption<?>> statConfigs = new EnumMap<>(StatWithValue.class);
		for (StatWithValue stat : StatWithValue.values()) {
			statConfigs.put(stat, stats.getField(stat));
		}
		
		Map<String, Map<String, ConfigOption<?>>> abilityConfigs = new HashMap<>();
		Moveset moveset = standType.getMoveset().value;
		for (var abilityEntry : moveset.abilities.entrySet()) {
			String abilityName = abilityEntry.getKey();
			Ability ability = abilityEntry.getValue();
			Map<String, ConfigOption<?>> abilityConfig = GetAbilityConfigOptionFields.get(ability);
			if (!abilityConfig.isEmpty()) {
				abilityConfigs.put(abilityName, abilityConfig);
			}
		}
		
		return new StandCommonConfig(environment, standType, 
				statConfigs,
				abilityConfigs);
	}

	@Override
	public StandType getObj() {
		return configState.configObj;
	}


	@Override
	public boolean isDefault() {
		return configState.isDefault();
	}

	@Override
	public void reset() {
		configState.reset(null);
	}
	
	
	@Override
	public void onServerSideApply(MinecraftServer server) {
		for (ServerLevel level : server.getAllLevels()) {
			for (Entity entity : level.getAllEntities()) {
				if (entity instanceof LivingEntity living) {
					StandPower standPower = StandPower.get(living);
					if (standPower != null) {
						StandStats.updateStandStatAttributes(standPower, living, standId);
					}
				}
			}
		}
	}


	@Override
	public void setRemoteState() {
		configState.configOptions.values().forEach(ConfigOption::setRemoteState);
	}
	
	@Override
	public void unsetRemoteState() {
		configState.configOptions.values().forEach(ConfigOption::unsetRemoteState);
	}


	@Override
	public void loadFromFileSystem() {
		if (!standDir.exists()) return;
		
		Path standCfgPath = standDir.toPath();
		ResourceLocation standId = getObj().getId();
		try (Stream<Path> filePaths = Files.walk(standCfgPath)) {
			Map<String[], Path> jsonFiles = filePaths
					.filter(path -> Files.isRegularFile(path) 
							&& path.getFileName().toString().endsWith(DataDrivenStandsLoader.FILE_EXT))
					.collect(Collectors.toMap(
							absolutePath -> {
								Path relativePath = standCfgPath.relativize(absolutePath);
								Stream<Path> componentDirs = StreamSupport.stream(relativePath.spliterator(), false);
								String[] path = componentDirs.map(p -> p.toString()).toArray(String[]::new);
								StringUtil.trimFileExtension(path, DataDrivenStandsLoader.FILE_EXT);
								return path;
							}, 
							Function.identity()));
			
			StitchedStandDataFiles<Path> stitchedConfig = new StitchedStandDataFiles<>();
			for (var jsonFileEntry : jsonFiles.entrySet()) {
				stitchedConfig.add(jsonFileEntry.getValue(), jsonFileEntry.getKey());
			}
			JsonObject configJson = stitchedConfig.stitchJson(standId);
			
			applyJson(configJson, false	/* 1 - This gets called very early on, there is nothing TO reset
					 					 * 2 - This may cause the ConfigOption#onChanged to be called for no reason (once again because the value is default)
					 					 */);
		}
		catch (IOException e) {
			JojoMod.getLogger().error("", e);
		}
	}
	
	// TODO move the old data-driven stand json stuff here
	protected void applyJson(JsonObject configJson, boolean doReset) {
		if (doReset) {
			reset();
		}
		JsonObject statsJson = JSONUtil.asObject(get(configJson, ElementType.STATS.path));
		if (statsJson != null) {
			for (var statConfigEntry : statConfigs.entrySet()) {
				String key = statConfigEntry.getKey().nameInJson;
				JsonElement statJson = statsJson.get(key);
				if (statJson != null) {
					ConfigOption<?> statConfig = statConfigEntry.getValue();
					statConfig.fromJsonWithCallback(statJson, key);
				}
			}
		}

		JsonObject abilityConfigsJson = JSONUtil.asObject(get(configJson, ElementType.ABILITY_CONFIG.path));
		if (abilityConfigsJson != null) {
			for (var abilityJsonEntry : abilityConfigsJson.entrySet()) {
				String abilityName = abilityJsonEntry.getKey();
				JsonObject abilityJson = abilityJsonEntry.getValue() instanceof JsonObject __ ? __ : null; 
				Map<String, ConfigOption<?>> abilityConfigs = this.abilityConfigs.get(abilityName);
				if (abilityJson != null && abilityConfigs != null) {
					for (var abilityConfigJsonEntry : abilityJson.entrySet()) {
						String fieldName = abilityConfigJsonEntry.getKey();
						JsonElement configFieldJson = abilityConfigJsonEntry.getValue();
						ConfigOption<?> field = abilityConfigs.get(fieldName);
						if (field != null) {
							field.fromJsonWithCallback(configFieldJson, fieldName);
						}
					}
				}
			}
		}
	}

	@Override
	public void saveToFileSystem() {
		savingState.clear();
		lastSavedState.clear();
		fillSavedState(savingState);
		
		for (var element : savingState.entrySet()) {
			Path elementPath = element.getKey();
			JsonElement elementState = element.getValue();
			JsonElement lastSavedElementState = lastSavedState.get(elementPath);
			if (lastSavedElementState == null || !lastSavedElementState.equals(elementState)) {
				saveFile(elementPath, elementState);
			}
		}
		savingState.clear();
	}

	@Override
	public void saveToFileSystem(Map<String, ConfigOption<?>> editedConfigOptions) {
		Map<Path, JsonElement> filesToSave = new HashMap<>();
		
		for (String fieldName : editedConfigOptions.keySet()) {
			ElementType elementType;
			String param;
			
			String[] split = fieldName.split(ABILITY_CFG_SEPARATOR);
			if (split.length == 2) {
				elementType = ElementType.ABILITY_CONFIG;
				param = split[0];
			}
			else {
				elementType = ElementType.STATS;
				param = null;
			}
			
			Path elementPath = elementType.makePath(param);
			filesToSave.computeIfAbsent(elementPath, p -> makeSavedState(elementType, param).getValue());
		}
		
		for (Map.Entry<Path, JsonElement> fileToSave : filesToSave.entrySet()) {
			Path elementPath = fileToSave.getKey();
			JsonElement elementState = fileToSave.getValue();
			
			JsonElement lastSavedElementState = lastSavedState.get(elementPath);
			if (lastSavedElementState == null || !lastSavedElementState.equals(elementState)) {
				saveFile(elementPath, elementState);
			}
		}
	}
	
	protected void fillSavedState(Map<Path, JsonElement> state) {
		Map.Entry<Path, JsonElement> stats = makeSavedState(ElementType.STATS, null);
		state.put(stats.getKey(), stats.getValue());
		
		for (var abilityEntry : abilityConfigs.entrySet()) {
			String abilityName = abilityEntry.getKey();
			Map.Entry<Path, JsonElement> abilityConfig = makeSavedState(ElementType.ABILITY_CONFIG, abilityName);
			state.put(abilityConfig.getKey(), abilityConfig.getValue());
		}
	}
	
	protected Map.Entry<Path, JsonElement> makeSavedState(ElementType elementType, @Nullable String param) {
		Path path = elementType.makePath(param);
		
		JsonElement element = switch (elementType) {
			case STATS -> {
				yield ObjectConfigHelper.settingsToJson(statConfigs, stat -> stat.nameInJson);
			}
			case ABILITY_CONFIG -> {
				yield ObjectConfigHelper.settingsToJson(abilityConfigs.get(param));
			}
		};
		
		return new AbstractMap.SimpleEntry<>(path, element);
	}
	
	protected void saveFile(Path elementPath, JsonElement elementState) {
		String fileName = elementPath.getFileName().toString();
		Path jsonFilePath = elementPath.resolveSibling(fileName + DataDrivenStandsLoader.FILE_EXT);
		File jsonFileFullPath = new File(standDir, jsonFilePath.toString());
		
		CommonFileConfig.serialize(jsonFileFullPath, writer -> {
			CommonFileConfig.GSON.toJson(elementState, writer);
		});
		lastSavedState.put(elementPath, elementState);
	}
	
	protected enum ElementType {
		STATS("stats"),
		ABILITY_CONFIG("moveset", "abilities");
		
		private ElementType(String path, String... path2) {
			this.path = Path.of(path, path2);
		}
		
		public Path makePath(@Nullable String param) {
			return param != null ? path.resolve(param) : path;
		}
		
		public final Path path;
	}
	
	@Nullable
	protected static JsonElement get(JsonObject root, Path path) {
		int i = 0;
		int depth = path.getNameCount();
		JsonObject obj = root;
		for (Path pathElem : path) {
			if (obj == null) {
				return null;
			}
			
			if (++i < depth) {
				obj = obj.getAsJsonObject(pathElem.toString());
			}
			else {
				return obj.get(pathElem.toString());
			}
		}
		return null;
	}


	@Override
	public void toBuf(RegistryFriendlyByteBuf buf) {
		configState.toBuf(buf);
	}
	
	@Override
	public void fromBuf(RegistryFriendlyByteBuf buf) {
		configState.fromBuf(buf, null);
	}


	
	@Override
	public Map<String, ConfigOption<?>> getAllConfigOptions() {
		return configState.configOptions.configOptions();
	}

}
