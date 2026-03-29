package com.github.standobyte.jojoimpl.powers.hamon.data;

import java.util.UUID;

import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.util.functions.AttributeUtil;
import com.github.standobyte.jojoimpl.powers.hamon.HamonData;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HamonDataStats {
//	public static final int MAX_STAT_LEVEL = 60;
//	public static final float MAX_BREATHING_LEVEL = 100;
//
//	public static final int[] POINTS_AT_LEVEL;
//	static {
//		POINTS_AT_LEVEL = new int[MAX_STAT_LEVEL + 1];
//		int diff = 0;
//
//		POINTS_AT_LEVEL[0] = 0;
//		POINTS_AT_LEVEL[1] = 2;
//		for (int i = 2; i < POINTS_AT_LEVEL.length; i++) {
//			diff += 3 + (i - 1) / 20;
//
//			POINTS_AT_LEVEL[i] = POINTS_AT_LEVEL[i - 1] + POINTS_AT_LEVEL[1] + diff;
//		}
//	}
//	
//	public static final int MAX_HAMON_POINTS = pointsAtLevel(MAX_STAT_LEVEL);
//	public static int pointsAtLevel(int level) {
//		level = Mth.clamp(level, 0, MAX_STAT_LEVEL);
//		return POINTS_AT_LEVEL[level];
//	}
	
	public final HamonData hamon;

	public HamonDataStats(HamonData hamon) {
		this.hamon = hamon;
	}

//	public int hamonStrengthPoints;
//	public int hamonStrengthLevel;
//	public int hamonControlPoints;
//	public int hamonControlLevel;
//	public float breathingTrainingLevel;
//	public float hamonDamageFactor = 1F;
//	public float pointsIncFrac = 0;
//	
//	public int getHamonStrengthPoints() {
//		//return hamonStrengthPoints;
//		return pointsAtLevel(getHamonStrengthLevel()) + 4;
//	}
//
//	public int getHamonStrengthLevel() {
//		//return hamonStrengthLevel;
//		return 40;
//	}
//
//	public int getHamonControlPoints() {
//		//return hamonControlPoints;
//		return pointsAtLevel(getHamonControlLevel()) + 6;
//	}
//
//	public int getHamonControlLevel() {
//		//return hamonControlLevel;
//		return 40;
//	}
//
//	public float getBreathingLevel() {
//		//return breathingTrainingLevel;
//		return 70.5f;
//	}

//	public static int levelFromPoints(int points) {
//		return GeneralUtil.largestLessOrEqualBinarySearch(POINTS_AT_LEVEL, points);
//	}
//
//	public static int pointsAtLevelFraction(float level) {
//		int lvlFloored = Mth.floor(level);
//		int pointsFullLvls = pointsAtLevel(lvlFloored);
//		int pointsNextLvl = pointsAtLevel(lvlFloored + 1);
//		return pointsFullLvls + Mth.floor((float) (pointsNextLvl - pointsFullLvls) * Mth.frac(level));
//	}
//
//	public static float levelFractionFromPoints(int points) {
//		int curLvl = levelFromPoints(points);
//		int curLvlPointsInt = pointsAtLevel(curLvl);
//		int pointsNextLvl = pointsAtLevel(curLvl + 1);
//		return (float) curLvl + (float) (points - curLvlPointsInt) / (float) (pointsNextLvl - curLvlPointsInt);
//	}
//
//	public void setHamonStatPoints(HamonStat stat, int points, boolean ignoreTraining, boolean allowLesserValue) {
//		setHamonStatPoints(stat, points, ignoreTraining, allowLesserValue, false, false);
//	}
//
//	public void setHamonStatPoints(HamonStat stat, int points, boolean ignoreTraining, 
//			boolean allowLesserValue, boolean clientSide, boolean notifyInUI) {
//		int oldPoints = getStatPoints(stat);
//		int oldLevel = getStatLevel(stat);
//		if (!ignoreTraining) {
//			int levelLimit = getStatLevelLimit(clientSide);
//			if (levelFromPoints(points) > levelLimit) {
//				points = pointsAtLevel(levelLimit + 1) - 1;
//			}
//		}
//		if (!allowLesserValue && points <= oldPoints) {
//			return;
//		}
//		int newPoints = Mth.clamp(points, 0, MAX_HAMON_POINTS);
//		switch (stat) {
//			case STRENGTH:
//				hamonStrengthPoints = newPoints;
//				hamonStrengthLevel = levelFromPoints(newPoints);
//				break;
//			case CONTROL:
//				hamonControlPoints = newPoints;
//				hamonControlLevel = levelFromPoints(newPoints);
//				break;
//		}
//		if (oldPoints != newPoints) {
//			LivingEntity user = power.getUser();
//			if (!user.level.isClientSide()) {
//				PacketManager.sendToClientsTrackingAndSelf(new TrHamonStatsPacket(user.getId(), true, stat, newPoints), user);
//				serverPlayer.ifPresent(player -> {
//					ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
//				});
//			}
//			int newLevel = getStatLevel(stat);
//			if (oldLevel != newLevel) {
//				switch (stat) {
//					case STRENGTH:
//						recalcHamonDamage();
//						break;
//					case CONTROL:
//						float energyRatio = getMaxBreathStabilityAt(newLevel) / getMaxBreathStabilityAt(oldLevel);
//						breathStability *= energyRatio;
//						power.setEnergy(power.getEnergy() * energyRatio);
//						break;
//				}
//				if (newLevel > oldLevel && notifyInUI && user.level.isClientSide() && user == ClientUtil.getClientPlayer()) {
//					ActionsOverlayGui.getInstance().onHamonStatIncreased(stat == HamonStat.STRENGTH ? HamonStatIncNotif.STRENGTH : HamonStatIncNotif.CONTROL);
//				}
//			}
//		}
//	}
//
//	public int getStatLevelLimit(boolean clientSide) {
//		int config = JojoModConfig.getCommonConfigInstance(clientSide).breathingHamonStatGap.get();
//		if (config < 0) {
//			return Integer.MAX_VALUE;
//		}
//		return (int) getBreathingLevel() + config;
//	}
//
//	public static final float MAX_HAMON_STRENGTH_MULTIPLIER = dmgFormula(MAX_STAT_LEVEL); // 7
//	private void recalcHamonDamage() {
//		hamonDamageFactor = dmgFormula(hamonStrengthLevel);
//	}
//
//	private static float dmgFormula(float strength) {
//		return (float) 1F + strength * 0.1F;
//	}
//
//	public float getHamonStrengthLevelRatio() {
//		return (float) getHamonStrengthLevel() / (float) MAX_STAT_LEVEL;
//	}
//
//	public float getHamonControlLevelRatio() {
//		return (float) getHamonControlLevel() / (float) MAX_STAT_LEVEL;
//	}
//
//	private int getStatPoints(HamonStat stat) {
//		switch (stat) {
//			case STRENGTH:
//				return getHamonStrengthPoints();
//			case CONTROL:
//				return getHamonControlPoints();
//			default:
//				throw new IllegalArgumentException("Unexpected HamonStat constant: " + stat.name());
//		}
//	}
//
//	public int getStatLevel(HamonStat stat) {
//		switch (stat) {
//			case STRENGTH:
//				return getHamonStrengthLevel();
//			case CONTROL:
//				return getHamonControlLevel();
//			default:
//				throw new IllegalArgumentException("Unexpected HamonStat constant: " + stat.name());
//		}
//	}
//
//	public int getSkillPoints(HamonStat stat) {
//		int lvl = getStatLevel(stat);
//		int spentPoints;
//		switch (stat) {
//			case STRENGTH:
//				spentPoints = hamonSkills.getBaseSkills().getSpentStrengthPoints();
//				break;
//			case CONTROL:
//				spentPoints = hamonSkills.getBaseSkills().getSpentControlPoints();
//				break;
//			default:
//				throw new IllegalArgumentException("Unexpected HamonStat constant: " + stat.name());
//		}
//		return Mth.clamp(lvl, 0, MAX_STAT_LEVEL) / 5 - spentPoints;
//	}
//
//	public int nextSkillPointLvl(HamonStat stat) {
//		return Mth.clamp(getStatLevel(stat), 0, MAX_STAT_LEVEL - 1) / 5 * 5 + 5;
//	}
//
//	private static final float ENERGY_PER_POINT = 750F;
//	public void hamonPointsFromAction(HamonStat stat, float energyCost) {
//		if (isSkillLearned(ModHamonSkills.NATURAL_TALENT.get())) {
//			energyCost *= 2;
//		}
//		energyCost *= JojoModConfig.getCommonConfigInstance(false).hamonPointsMultiplier.get().floatValue();
//		int points = (int) (energyCost / ENERGY_PER_POINT);
//		pointsIncFrac += (energyCost % ENERGY_PER_POINT) / ENERGY_PER_POINT;
//		if (pointsIncFrac >= 1) {
//			points++;
//			pointsIncFrac--;
//		}
//		setHamonStatPoints(stat, getStatPoints(stat) + points, false, false);
//	}
//
//	public float getBreathingLevel() {
//		return breathingTrainingLevel;
//	}
//
//	public void setBreathingLevel(float level) {
//		setBreathingLevel(level, false);
//	}
//
//	public void setBreathingLevel(float level, boolean notifyInUI) {
//		float oldLevel = breathingTrainingLevel;
//		breathingTrainingLevel = Mth.clamp(level, 0, MAX_BREATHING_LEVEL);
//		LivingEntity user = power.getUser();
//		if (oldLevel != breathingTrainingLevel) {
//			recalcHamonDamage();
//			if (!user.level().isClientSide()) {
//				PacketManager.sendToClientsTrackingAndSelf(new TrHamonStatsPacket(user.getId(), true, getBreathingLevel()), user);
//				serverPlayer.ifPresent(player -> {
//					ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
//				});
//			}
//			else {
//				if ((int) breathingTrainingLevel > (int) oldLevel && notifyInUI && user == ClientUtil.getClientPlayer()) {
//					ActionsOverlayGui.getInstance().onHamonStatIncreased(HamonStatIncNotif.BREATHING);
//				}
//			}
//		}
//		if (!user.level().isClientSide()) {
//			giveBreathingTrainingBuffs(user);
//		}
//	}
//
//	private static final AttributeModifier ATTACK_DAMAGE = new AttributeModifier(
//			UUID.fromString("8dcb2ad7-6067-4615-b7b6-af5256537c10"), "Attack damage from Hamon Training", 0.03, AttributeModifier.Operation.ADDITION);
//	private static final AttributeModifier ATTACK_SPEED = new AttributeModifier(
//			UUID.fromString("995b2915-9053-472c-834c-f94251e81659"), "Attack speed from Hamon Training", 0.015, AttributeModifier.Operation.ADDITION);
//	private static final AttributeModifier MOVEMENT_SPEED = new AttributeModifier(
//			UUID.fromString("ffa9ba4e-3811-44f7-a4a9-887ffbd47390"), "Movement speed from Hamon Training", 0.0005, AttributeModifier.Operation.ADDITION);
//	private static final AttributeModifier SWIMMING_SPEED = new AttributeModifier(
//			UUID.fromString("34dcb563-6759-4a2b-9dd8-ad2dd7e70404"), "Swimming speed from Hamon Training", 0.01, AttributeModifier.Operation.ADDITION);
//
//	private void giveBreathingTrainingBuffs(LivingEntity entity) {
//		setBreathingTrainingAttributes(entity, (int) getBreathingLevel());
//	}
//
//	private void clearBreathingTrainingBuffs(LivingEntity entity) {
//		setBreathingTrainingAttributes(entity, 0);
//	}
//
//	private void setBreathingTrainingAttributes(LivingEntity entity, int lvl) {
//		AttributeUtil.applyAttributeModifierMultiplied(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE, lvl);
//		AttributeUtil.applyAttributeModifierMultiplied(entity, Attributes.ATTACK_SPEED, ATTACK_SPEED, lvl);
//		AttributeUtil.applyAttributeModifierMultiplied(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED, lvl);
//		AttributeUtil.applyAttributeModifierMultiplied(entity, ForgeMod.SWIM_SPEED.get(), SWIMMING_SPEED, lvl);
//	}

	
    public static enum HamonStat {
        STRENGTH,
        CONTROL
    }
}
