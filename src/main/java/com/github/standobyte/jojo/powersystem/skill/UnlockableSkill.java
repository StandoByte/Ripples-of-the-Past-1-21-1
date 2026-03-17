package com.github.standobyte.jojo.powersystem.skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// XXX tick the unlocked stand skills
public abstract class UnlockableSkill {
	public final String skillName;
	public boolean isStarting;
	public List<String> prerequisiteSkills;
	public Optional<String> mainSkill;
	public List<String> unlocksAbilities;
	
	public Component textName;
	public Component textDesc;
	public Component textControls;
	public DevStatus implemented = DevStatus.IMPLEMENTED;

	public UnlockableSkill(String name) {
		this.skillName = name;
		this.prerequisiteSkills = new ArrayList<>();
		this.mainSkill = Optional.empty();
		this.unlocksAbilities = new ArrayList<>();
		this.textName = skillName(name);
		this.textDesc = Component.translatable("jojo_ripples.skill." + name + ".desc");
		this.textControls = Component.translatable("jojo_ripples.skill." + name + ".controls");
	}
	
	public ConditionCheck canUnlockFromMenu(Power<?> userPower, PowerData data) {
		List<String> missingPrerequsites = null;
		for (String prerequisite : prerequisiteSkills) {
			if (!data.isSkillUnlocked(prerequisite)) {
				if (missingPrerequsites == null) missingPrerequsites = new ArrayList<>(prerequisiteSkills.size());
				missingPrerequsites.add(prerequisite);
			}
		}
		if (missingPrerequsites != null) {
			MutableComponent allNames = Component.empty();
			int count = missingPrerequsites.size();
			for (int i = 0; i < count; i++) {
				String skill = missingPrerequsites.get(i);
				MutableComponent skillName = skillName(skill);
				allNames = allNames.append(Component.translatable("commands.neoforge.data_components.list.entry", skillName));
			}
			Component fullMessage = Component.translatable("jojo_ripples.stand_skills.prerequisites", allNames);
			return ConditionCheck.createNegative(fullMessage);
		}
		
		return ConditionCheck.POSITIVE;
	}
	
	// Initialization methods
	
	public UnlockableSkill setIsStartingSkill() {
		this.isStarting = true;
		return this;
	}
	
	@Deprecated
	public UnlockableSkill setIncomplete() {
		this.implemented = DevStatus.WIP;
		return this;
	}
	
	@Deprecated
	public UnlockableSkill setNotYetImplemented() {
		this.implemented = DevStatus.NYI;
		return this;
	}
	
	
	protected static MutableComponent skillName(String internalName) {
		return Component.translatable("jojo_ripples.skill." + internalName);
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
	
	public enum DevStatus {
		IMPLEMENTED,
		WIP,
		NYI
	}
	
}
