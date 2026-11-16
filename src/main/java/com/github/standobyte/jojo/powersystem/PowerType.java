package com.github.standobyte.jojo.powersystem;

import java.util.Map;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ui.skill_tree.advancement_copypaste.SkillTree;
import com.github.standobyte.jojo.config.util.JsonConfigurable;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public abstract class PowerType implements JsonConfigurable {
	protected final MovesetBuilder movesetOriginal;
	protected final DefaultedValue<Moveset> movesetConfigured;
	
	public PowerType(ResourceLocation id, MovesetBuilder defaultMoveset) {
		this.movesetOriginal = defaultMoveset;
		this.movesetConfigured = new DefaultedValue<>(movesetOriginal.build(getPowerClass(), id));
	}
	
	
	@Nonnull public abstract PowerData newDataInstance();
	
	public abstract ResourceLocation getId();
	
	public abstract PowerClass<?> getPowerClass();
	
	public boolean isEnabled() {
		return true;
	}
	
	public void onUserActivateHUD(LivingEntity user, Power<?> power, boolean state) {}
	
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
			MovesetBuilder configuredBuilder = getDefaultMoveset().deepCopy();
			Moveset.applyJsonConfig(configuredBuilder, movesetEditsJson);
			Moveset configuredMoveset = configuredBuilder.build(getPowerClass(), getId());
			movesetConfigured.value = configuredMoveset;
		}
		else {
			movesetConfigured.reset();
		}
	}
	
	@Override
	public void restoreDefaults() {
		movesetConfigured.reset();
	}
	
	
	@ApiStatus.Internal
	public MovesetBuilder getDefaultMoveset() {
		return this.movesetOriginal;
	}
	
	public DefaultedValue<Moveset> getMoveset() {
		return movesetConfigured;
	}
	
	
	public Map<String, ? extends UnlockableSkill> getUnlockableSkills() {
		return this.movesetOriginal.unlockableSkills;
	}
	
	public SkillTree<UnlockableSkill> getSkillTree() {
		return this.movesetOriginal.getSkillTree();
	}
	
}
