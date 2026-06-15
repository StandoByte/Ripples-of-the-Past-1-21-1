package com.github.standobyte.jojoimpl.powers.hamon.data;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.util.functions.JojoModUtil;
import com.github.standobyte.jojoimpl.powers.hamon.HamonData;
import com.google.common.collect.ImmutableList;

import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;

public class HamonDataExercises {
	public final HamonData hamon;

//	public float breathingTrainingDayBonus;
//	//public float prevDayExercisesCount;
//	public int canSkipTrainingDays;
//	public EnumMap<Exercise, Integer> exerciseTicks = new EnumMap<Exercise, Integer>(Exercise.class);
//
//	public boolean isMeditating;
//	public int meditationTicks;
//	public int breathStabilityIncTicks;
//
//	public static final AttributeModifier RUNNING_COMPLETED = new AttributeModifier(
//			UUID.fromString("b730b24e-e970-4a94-b300-57e2555b42b5"), "Movement speed from running exercise", 0.1D, AttributeModifier.Operation.MULTIPLY_BASE);
//	public static final AttributeModifier MINING_COMPLETED = new AttributeModifier(
//			UUID.fromString("8674ea35-6eaf-4e22-98da-4ec0c5a4d20d"), "Attack speed from running exercise", 0.05D, AttributeModifier.Operation.MULTIPLY_BASE);
//	public static final float SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER = 1.1F;
//	public static final float MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION = 20;
//	public boolean swimmingCompleted = false;
//	public boolean meditationCompleted = false;
//	public int exercisesCompleted = 0;

	public HamonDataExercises(HamonData hamon) {
		this.hamon = hamon;
//		for (Exercise exercise : Exercise.values()) {
//			exerciseTicks.put(exercise, 0);
//		}
	}

//	private void updateExerciseAttributes(LivingEntity entity) {
//		exercisesCompleted = 0;
//		for (Exercise exercise : Exercise.values()) {
//			if (isExerciseComplete(exercise)) {
//				++exercisesCompleted;
//				switch (exercise) {
//					case RUNNING:
//						if (!entity.level.isClientSide()) {
//							MCUtil.applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED, RUNNING_COMPLETED);
//						}
//						break;
//					case MINING:
//						if (!entity.level.isClientSide()) {
//							MCUtil.applyAttributeModifier(entity, Attributes.ATTACK_SPEED, MINING_COMPLETED);
//						}
//						break;
//					case SWIMMING:
//						// FIXME also update energy count
//						swimmingCompleted = true;
//						break;
//					case MEDITATION:
//						meditationCompleted = true;            
//						break;
//					default:
//						break;
//				}
//			}
//
//			else {
//				switch (exercise) {
//					case RUNNING:
//						if (!entity.level.isClientSide()) {
//							ModifiableAttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
//							if (attributeInstance != null) {
//								attributeInstance.removeModifier(RUNNING_COMPLETED);
//							}
//						}
//						break;
//					case MINING:
//						if (!entity.level.isClientSide()) {
//							ModifiableAttributeInstance attributeInstance = entity.getAttribute(Attributes.ATTACK_SPEED);
//							if (attributeInstance != null) {
//								attributeInstance.removeModifier(MINING_COMPLETED);
//							}
//						}
//						break;
//					case SWIMMING:
//						swimmingCompleted = false;
//						break;
//					case MEDITATION:
//						meditationCompleted = false;
//						break;
//					default:
//						break;
//				}
//			}
//		}
//	}
//
//	public int getExerciseTicks(Exercise exercise) {
//		return Math.min(exerciseTicks.get(exercise), exercise.getMaxTicks(this));
//	}
//
//	public boolean isExerciseComplete(Exercise exercise) {
//		return getExerciseTicks(exercise) >= exercise.getMaxTicks(this);
//	}
//
//	public float getMaxIncompleteExercise() {
//		return exerciseTicks.entrySet().stream()
//				.filter(entry -> entry.getValue() < entry.getKey().getMaxTicks(this))
//				.map(entry -> (float) entry.getValue() / (float) entry.getKey().getMaxTicks(this))
//				.max(Float::compare).orElse(0F);
//	}
//
//	public boolean has4ExercisesBonus() {
//		return exercisesCompleted >= MAX_EXERCISES_NEEDED;
//	}
//
//	private boolean incExerciseLastTick;
//	private boolean incExerciseThisTick;
//	private boolean exerciseCompleted;
//	private Vector3d prevPos = null;
//
//	private int blocksMiningDelay;
//	public void tickExercises(Player user) {
//		Vector3d pos = user.position();
//		boolean positionChanged = prevPos == null || prevPos.x != pos.x || prevPos.y != pos.y;
//		this.prevPos = pos;
//		incExerciseThisTick = false;
//		exerciseCompleted = false;
//
//		boolean isMining;
//		if (!user.level.isClientSide()) {
//			PlayerInteractionManager gamemode = ((ServerPlayer) user).gameMode;
//			boolean isDestroying = gamemode.isDestroyingBlock;
//			boolean delayedDestroy = gamemode.hasDelayedDestroy;
//			isMining = isDestroying || delayedDestroy;
//		}
//		else {
//			isMining = ClientUtil.isDestroyingBlock();
//		}
//		if (isMining) {
//			blocksMiningDelay = 6;
//		}
//		else {
//			isMining = blocksMiningDelay-- > 0;
//		}
//		if (isMining) {
//			incExerciseTicks(Exercise.MINING, 1, user.level.isClientSide());
//		}
//
//		if (positionChanged && user.isSwimming() && JojoModUtil.playerHasClientInput(user)) {
//			incExerciseTicks(Exercise.SWIMMING, 1, user.level.isClientSide());
//		}
//
//		else if (positionChanged && user.isSprinting() && user.isOnGround() && !user.isSwimming()) {
//			incExerciseTicks(Exercise.RUNNING, 1, user.level.isClientSide());
//		}
//
//		if (isMeditating()) {
//			if (++meditationTicks >= MEDITATION_INC_START) {
//				incExerciseTicks(Exercise.MEDITATION, 1, user.level.isClientSide());
//				breathStabilityIncTicks++;
//			}
//			updateBbHeight(user);
//			if (!user.level.isClientSide()) {
//				user.getFoodData().addExhaustion(-0.0025F);
//				if (user.tickCount % 200 == 0 && user.isHurt() && user.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
//					user.heal(1.0F);
//				}
//			}
//		}
//
//		if (incExerciseLastTick && !incExerciseThisTick || exerciseCompleted) {
//			serverPlayer.ifPresent(player -> {
//				PacketManager.sendToClient(HamonExercisesPacket.exercisesOnly(this), player);
//			});
//		}
//		if (breathingTrainingLevel < MAX_BREATHING_LEVEL && exerciseCompleted && exercisesCompleted <= MAX_EXERCISES_NEEDED) {
//			updateExerciseAttributes(user);
//			serverPlayer.ifPresent(player -> {
//				IFormattableTextComponent message1 = Component.translatable("hamon.exercise.all.count.message" + (exercisesCompleted >= 4 ? ".4" : ""), 
//						exercisesCompleted, MAX_EXERCISES_NEEDED);
//				IFormattableTextComponent message2 = null;
//				switch (exercisesCompleted) {
//					case 3:
//						message2 = Component.translatable("hamon.exercise.all.count.message2.3", 
//								new DecimalFormat("#.##").format(getBreathingIncrease(player, false)));
//						break;
//					case 4:
//						message2 = Component.translatable("hamon.exercise.all.count.message2.4", 
//								CAN_SKIP_DAYS);
//						break;
//				}
//
//				if (message2 == null) {
//					player.sendMessage(message1, ChatType.GAME_INFO, Util.NIL_UUID);
//				}
//				else {
//					PacketManager.sendToClient(new MultiLineOverlayMsgPacket(ImmutableList.of(message1, message2)), player);
//				}
//			});
//		}
//		incExerciseLastTick = incExerciseThisTick;
//	}
//
//	public static final int MAX_EXERCISES_NEEDED = 4;
//	public static final int CAN_SKIP_DAYS = 2;
//
//	private static final int MEDITATION_INC_START = 40;
//	private float bbHeightMult = 1;
//	private boolean updateHeight = false;
//	private void updateBbHeight(LivingEntity user) {
//		if (isMeditating) {
//			if (meditationTicks <= 35) {
//				bbHeightMult = 1 - meditationTicks * 0.0085F;
//				updateHeight = true;
//				user.refreshDimensions();
//			}
//		}
//		else {
//			bbHeightMult = 1;
//			updateHeight = true;
//			user.refreshDimensions();
//		}
//	}
//
//	private void actuallyUpdateBbHeight(EntityEvent.Size event) {
//		if (updateHeight) {
//			EntitySize size = event.getNewSize();
//			float width = size.width;
//			float height = size.height * bbHeightMult;
//			float heightDiff = size.height - height;
//			size = size.fixed ? EntitySize.fixed(width, height) : EntitySize.scalable(width, height);
//			event.setNewSize(size, bbHeightMult == 1);
//			if (bbHeightMult != 1) {
//				event.setNewEyeHeight(1.62F - heightDiff);
//			}
//		}
//	}
//
//	@SubscribeEvent
//	public static void updateBoundingBox(EntityEvent.Size event) {
//		Entity entity = event.getEntity();
//		if (entity instanceof LivingEntity) {
//			LivingEntity user = (LivingEntity) entity;
//			if (user.getAttributes() == null) { // means that the event was created in the constructor, when capabilities haven't been initialized yet
//				return;
//			}
//			INonStandPower.getNonStandPowerOptional(user).ifPresent(cap -> {
//				cap.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
//					hamon.actuallyUpdateBbHeight(event);
//				});
//			});
//		}
//	}
//
//
//	private void incExerciseTicks(Exercise exercise, float multiplier, boolean clientSide) {
//		int ticks = exerciseTicks.get(exercise);
//		int maxTicks = exercise.getMaxTicks(this);
//		if (ticks < maxTicks) {
//			int inc = 1;
//			if (multiplier > 1F) {
//				inc = Mth.floor(multiplier);
//				if (random.nextFloat() < Mth.frac(multiplier)) inc++;
//			}
//			inc = Math.min(inc, maxTicks - ticks);
//			if (ticks + inc == maxTicks) {
//				if (clientSide) {
//					return;
//				}
//				else {
//					this.exerciseCompleted = true;
//				}
//			}
//			setExerciseValue(exercise, ticks + inc, clientSide);
//			this.incExerciseThisTick = true;
//		}
//	}
//
//	public void setExerciseTicks(int[] ticks, boolean clientSide) {
//		Exercise[] exercises = Exercise.values();
//		for (int i = 0; i < exercises.length; i++) {
//			setExerciseValue(exercises[i], ticks[i], clientSide);
//		}
//		if (power != null) {
//			updateExerciseAttributes(power.getUser());
//		}
//	}
//
//	private void setExerciseValue(Exercise exercise, int value, boolean clientSide) {
//		if (exerciseTicks.put(exercise, value) != value && clientSide) {
//			ActionsOverlayGui.getInstance().onHamonExerciseValueChanged(exercise);
//		}
//	}
//
//	public void setIsMeditating(LivingEntity user, boolean isMeditating) {
//		if (this.isMeditating != isMeditating) {
//			this.isMeditating = isMeditating;
//			this.meditationTicks = 0;
//			this.breathStabilityIncTicks = 0;
//			if (!user.level.isClientSide()) {
//				PacketManager.sendToClientsTrackingAndSelf(new TrHamonMeditationPacket(user.getId(), isMeditating), user);
//			}
//			if (isMeditating) {
//				user.yBodyRot = user.yRot;
//			}
//			else {
//				updateBbHeight(user);
//			}
//		}
//	}
//
//	public float getTrainingBonus(boolean perksAndConfigMult) {
//		if (!isUserWearingBreathMask()) {
//			return 0;
//		}
//		return perksAndConfigMult ? multiplyPositiveBreathingTraining(breathingTrainingDayBonus) : breathingTrainingDayBonus;
//	}
//
//	private float multiplyPositiveBreathingTraining(float training) {
//		if (training > 0) {
//			if (isSkillLearned(ModHamonSkills.NATURAL_TALENT.get())) {
//				training *= 2;
//			}
//			training *= JojoModConfig.getCommonConfigInstance(false).breathingTrainingMultiplier.get().floatValue();
//		}
//		return training;
//	}
//
//	public void breathingTrainingDay(Player user) {
//		Level world = user.level();
//		if (!world.isClientSide()) {
//			float lvlInc = getBreathingIncrease(user, true);
//			setBreathingLevel(getBreathingLevel() + lvlInc);
//			if (isSkillLearned(ModHamonSkills.CHEAT_DEATH.get())) {
//				HamonUtil.updateCheatDeathEffect(power.getUser());
//			}
//		}
//		for (Exercise exercise : exerciseTicks.keySet()) {
//			setExerciseValue(exercise, 0, world.isClientSide());
//		}
//		updateExerciseAttributes(user);
//	}
//
//	public boolean breathingCanGoDown(Player user) {
//		return JojoModConfig.getCommonConfigInstance(false).breathingTrainingDeterioration.get() 
//				&& breathingTrainingLevel < MAX_BREATHING_LEVEL;
//	}
//
//	public float getBreathingIncrease(Player user, boolean newTrainingDay) {
//		float completedExercises = getCompleteExercisesCount() + getMaxIncompleteExercise();
//		/* at least 2 exercises to get positive increase, 
//           >= 3 exercises give max increase */
//		float lvlInc = Mth.clamp(completedExercises - 2, -1, 1);
//		float bonusIncrease = lvlInc * 0.25f;
//		boolean keepLvlThisDay = canSkipTrainingDays > 0;
//
//		if (lvlInc <= 0) {
//			if (!breathingCanGoDown(user) || keepLvlThisDay) {
//				lvlInc = 0;
//			}
//			else {
//				lvlInc *= 0.25F;
//			}
//			bonusIncrease = 0;
//		}
//		else {
//			lvlInc = multiplyPositiveBreathingTraining(lvlInc + getTrainingBonus(false));
//		}
//
//		if (newTrainingDay) {
//			if (lvlInc <= 0 && !keepLvlThisDay) {
//				breathingTrainingDayBonus = 0;
//			}
//			else if (isUserWearingBreathMask()) {
//				breathingTrainingDayBonus += bonusIncrease;
//			}
//
//			if (canSkipTrainingDays > 0) --canSkipTrainingDays;
//			if (completedExercises >= MAX_EXERCISES_NEEDED) {
//				canSkipTrainingDays = Math.max(canSkipTrainingDays, CAN_SKIP_DAYS);
//			}
//		}
//
//		lvlInc = Mth.clamp(lvlInc, -breathingTrainingLevel, MAX_BREATHING_LEVEL - breathingTrainingLevel);
//		return lvlInc;
//	}
//
//	public enum Exercise {
//		MINING(75),
//		RUNNING(67.5f),
//		SWIMMING(60),
//		MEDITATION(37.5f),
//		PLACEHOLDER_1(60),
//		PLACEHOLDER_2(60);
//
//		public static final boolean TMP_HAS_PLACEHOLDERS = true;
//
//		private final float maxTicks;
//
//		private Exercise(float seconds) {
//			this.maxTicks = seconds * 20F;
//		}
//
//		public int getMaxTicks(@Nullable HamonData hamon) {
//			float multiplier = hamon != null ? (MAX_BREATHING_LEVEL - hamon.getBreathingLevel()) / MAX_BREATHING_LEVEL * 0.75F + 0.25F : 1;
//			return Mth.floor(maxTicks * multiplier);
//		}
//
//		public double getBuffPercentage() {
//			switch (this) {
//				case MINING:
//					return MINING_COMPLETED.getAmount() * 100;
//				case RUNNING:
//					return RUNNING_COMPLETED.getAmount() * 100;
//				case SWIMMING:
//					return (SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER - 1) * 100; 
//				case MEDITATION:
//					return MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION / 20;
//				default:
//					return 0;
//			}
//		}
//
//		private Component tlName;
//		public Component getName() {
//			if (tlName == null) {
//				tlName = Component.translatable("hamon." + name().toLowerCase() + "_exercise");
//			}
//			return tlName;
//		}
//	}
}
