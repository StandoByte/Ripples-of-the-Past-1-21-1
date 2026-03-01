package com.github.standobyte.jojo.powersystem.standpower;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.skill.UnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class StandUnlockableSkill extends UnlockableSkill {
	public boolean isStarting;
	public int expToUnlock;
	public boolean NYI = false;

	public StandUnlockableSkill(String name) {
		super(name);
	}
	
	public static final ConditionCheck NOT_ENOUGH_EXP = ConditionCheck.createNegative(Component.translatable("jojo_ripples.stand_skills.not_enough_exp"));
	public ConditionCheck canUnlockFromMenu(StandPower userPower, StandTypePersistentData data) {
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
		
		if (data.getExp() < this.expToUnlock) {
			return NOT_ENOUGH_EXP;
		}
		
		return ConditionCheck.POSITIVE;
	}
	
	// Initialization methods
	
	@Deprecated
	public StandUnlockableSkill setPointsToUnlock(int skillPoints) {
		return this;
	}
	
	public StandUnlockableSkill setExpToUnlock(int exp) {
		this.expToUnlock = exp;
		return this;
	}
	
	public StandUnlockableSkill setIsStartingSkill() {
		this.isStarting = true;
		setExpToUnlock(0);
		return this;
	}
	
	@Deprecated
	public StandUnlockableSkill setNotYetImplemented() {
		this.NYI = true;
		return this;
	}
	
	public static StandUnlockableSkill unlockableAbility(String name, int exp) {
		StandUnlockableSkill skill = new StandUnlockableSkill(name);
		skill.withAbility(name);
		skill.setExpToUnlock(exp);
		return skill;
	}
	
	public static StandUnlockableSkill startingAbility(String name) {
		StandUnlockableSkill skill = new StandUnlockableSkill(name);
		skill.withAbility(name);
		skill.setIsStartingSkill();
		return skill;
	}
	
	@Deprecated
	public static StandUnlockableSkill tiedToMainSkill(String name, String mainSkill) {
		StandUnlockableSkill skill = new StandUnlockableSkill(name);
		skill.withAbility(name);
		skill.mainSkill = Optional.of(mainSkill);
		return skill;
	}

}
