package com.github.standobyte.jojo.powersystem;

import java.util.Map;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.config.JsonConfigurable;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class PowerType implements JsonConfigurable {
	protected final DefaultedValue<MovesetBuilder> movesetConfigured;
	protected Moveset baseMoveset;
	
	public PowerType(MovesetBuilder defaultMoveset) {
		this.movesetConfigured = new DefaultedValue<>(defaultMoveset);
		initBaseMoveset();
	}
	
	@Nonnull public abstract PowerData newDataInstance();
	
	public abstract ResourceLocation getId();
	
	public abstract PowerClass<?> getPowerClass();
	
	public boolean isEnabled() {
		return true;
	}
	
	public abstract Component getName(Power<?> power);
	
	
	@Override
	public JsonObject makeConfigTemplate() {
		JsonObject json = new JsonObject();
		
		Moveset defaultMoveset = getDefaultMoveset().build(getPowerClass(), getId());
		JsonElement movesetJson = Moveset.toJson(defaultMoveset);
		json.add("moveset", movesetJson);
		
		return json;
	}
	
	// XXX (data-driven stands) control scheme in data-driven stands
	@Override
	public void applyConfig(JsonElement json) {
		JsonObject config = json.getAsJsonObject();
		
		JsonObject movesetEditsJson = config.getAsJsonObject("moveset");
		if (movesetEditsJson != null) {
			MovesetBuilder configured = getDefaultMoveset().deepCopy();
			Moveset.applyJsonConfig(configured, movesetEditsJson);
			movesetConfigured.value = configured;
		}
		else {
			movesetConfigured.reset();
		}
		initBaseMoveset();
	}
	
	@Override
	public void restoreDefaults() {
		movesetConfigured.reset();
	}
	
	
	@ApiStatus.Internal
	public MovesetBuilder getDefaultMoveset() {
		return this.movesetConfigured.defaultValue;
	}
	
	protected void initBaseMoveset() {
		this.baseMoveset = this.movesetConfigured.value.build(getPowerClass(), getId());
	}
	
	public Moveset getBaseMoveset() {
		return baseMoveset;
	}
	
	public Moveset makeMoveset(Power<?> userPower) {
		Moveset moveset = this.movesetConfigured.value.build(getPowerClass(), getId());
		return moveset;
	}
	
	
	public Map<String, ? extends UnlockableSkill> getUnlockableSkills() {
		return this.movesetConfigured.value.unlockableSkills;
	}
	
}
