package com.github.standobyte.jojo.powersystem.skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.chat.Component;

// XXX tick the unlocked stand skills
public abstract class UnlockableSkill {
	public final String skillName;
	public List<String> prerequisiteSkills;
	public Optional<String> mainSkill;
	public List<String> unlocksAbilities;
	
	public Component textName;
	public Component textDesc;
	public Component textControls;

	public UnlockableSkill(String name) {
		this.skillName = name;
		this.prerequisiteSkills = new ArrayList<>();
		this.mainSkill = Optional.empty();
		this.unlocksAbilities = new ArrayList<>();
		this.textName = Component.translatable("jojo_ripples.skill." + name);
		this.textDesc = Component.translatable("jojo_ripples.skill." + name + ".desc");
		this.textControls = Component.translatable("jojo_ripples.skill." + name + ".controls");
	}
	
	public UnlockableSkill withAbility(String abilityName, String... extra) {
		this.unlocksAbilities.add(abilityName);
		Collections.addAll(this.unlocksAbilities, extra);
		return this;
	}
	
	public UnlockableSkill prerequisiteSkill(String name, String... other) {
		this.prerequisiteSkills.add(name);
		Collections.addAll(this.prerequisiteSkills, other);
		return this;
	}
	
}
