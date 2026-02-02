package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Optional;

import com.github.standobyte.jojo.powersystem.skill.UnlockableSkill;

public class StandUnlockableSkill extends UnlockableSkill {
	public int expToUnlock;

	public StandUnlockableSkill(String name) {
		super(name);
	}
	
	@Deprecated
	public StandUnlockableSkill setPointsToUnlock(int skillPoints) {
		return this;
	}
	
	public StandUnlockableSkill setExpToUnlock(int exp) {
		this.expToUnlock = exp;
		return this;
	}
	
	public StandUnlockableSkill setIsStartingSkill() {
		setExpToUnlock(0);
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
	
	public static StandUnlockableSkill tiedToMainSkill(String name, String mainSkill) {
		StandUnlockableSkill skill = new StandUnlockableSkill(name);
		skill.withAbility(name);
		skill.mainSkill = Optional.of(mainSkill);
		return skill;
	}

}
