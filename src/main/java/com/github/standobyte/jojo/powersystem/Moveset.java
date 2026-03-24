package com.github.standobyte.jojo.powersystem;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.config.ConfigAbilityFactory;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;

@ApiStatus.NonExtendable
public class Moveset {
	@ApiStatus.Internal public final Map<String, Ability> abilities;
	@ApiStatus.Internal public ControlSchemeTemplate controlScheme = new ControlSchemeTemplate();
	
	public Moveset(Map<String, Ability> abilities, 
			PowerClass<?> powerClass, ResourceLocation powerTypeId) {
		this.abilities = abilities;
	}
	
	public Ability getAbility(String name) {
		return abilities.get(name);
	}
	
	
	protected static final Moveset EMPTY = new Moveset(ImmutableMap.of(), null, null);
	
	public static Moveset empty() {
		return EMPTY;
	}
	
	
	public static JsonElement toJson(Moveset moveset) {
		JsonObject json = new JsonObject();
		
		JsonObject abilities = new JsonObject();
		for (var abilityEntry : moveset.abilities.entrySet()) {
			String abilityName = abilityEntry.getKey();
			Ability ability = abilityEntry.getValue();
			JsonObject abilityJson = new JsonObject();
			
			JsonElement abilityTypeJson = JojoRegistries.ABILITY_TYPES_REG.byNameCodec()
					.encodeStart(JsonOps.INSTANCE, ability.abilityType).getOrThrow();
			abilityJson.add("type", abilityTypeJson);

			JsonObject configJson = ability.toConfigJson();
			if (!configJson.isEmpty()) {
				abilityJson.add("config", configJson);
			}
			
			abilities.add(abilityName, abilityJson);
		}
		json.add("abilities", abilities);
		
		return json;
	}

	// XXX (data-driven stands) deserialize default control schemes
	public static MovesetBuilder applyJsonConfig(MovesetBuilder moveset, JsonObject movesetJson) {
		JsonArray disable = movesetJson.getAsJsonArray("disable");
		if (disable != null) {
			for (JsonElement element : disable) {
				moveset.abilities.remove(element.getAsString());
			}
		}
		
		JsonObject abilities = movesetJson.getAsJsonObject("abilities");
		if (abilities != null) {
			for (var configEntry : abilities.entrySet()) {
				String abilityName = configEntry.getKey();
				JsonElement json = configEntry.getValue();
				if (json.isJsonObject()) {
					JsonObject abilityJson = json.getAsJsonObject();
					if (moveset.abilities.containsKey(abilityName)) {
						ConfigAbilityFactory<?> abilityEntry = moveset.abilities.get(abilityName);
						JsonObject configJson = abilityJson.getAsJsonObject("config");
						if (configJson != null) {
							abilityEntry.addInitBehavior(ability -> ability.applyConfig(configJson));
						}
					}
					else {
						DataResult<Pair<AbilityType<?>, JsonElement>> abilityTypeParsed = JojoRegistries.ABILITY_TYPES_REG.byNameCodec()
								.decode(JsonOps.INSTANCE, abilityJson.get("type"));
						abilityTypeParsed.ifError(error -> {
							JojoMod.getLogger().error("Moveset: failed to parse ability type (ability {}: {})", abilityName, error.message());
						}).ifSuccess(success -> {
							AbilityType<?> abilityType = success.getFirst();
							
							JsonObject configJson = abilityJson.getAsJsonObject("config");
							if (configJson != null) {
								moveset.addAbility(abilityName, abilityType, ability -> ability.applyConfig(configJson));
							}
						});
					}
				}
			}
		}
		
		return moveset;
	}
	
}
