package com.github.standobyte.jojo.powersystem.skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// XXX tick the unlocked stand skills
public abstract class UnlockableSkill {
	public final String skillName;
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
