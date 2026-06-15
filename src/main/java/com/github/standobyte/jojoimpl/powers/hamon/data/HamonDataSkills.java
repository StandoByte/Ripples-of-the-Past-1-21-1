package com.github.standobyte.jojoimpl.powers.hamon.data;

import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojoimpl.powers.hamon.HamonData;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class HamonDataSkills {
	public final HamonData hamon;

//	public MainHamonSkillsManager hamonSkills;

	public HamonDataSkills(HamonData hamon) {
		this.hamon = hamon;
	}


//	public boolean isSkillLearned(AbstractHamonSkill skill) {
//		return hamonSkills.containsSkill(skill);
//	}
//
//	public ActionConditionResult canLearnSkillTeacherIrrelevant(LivingEntity user, AbstractHamonSkill skill) {
//		return hamonSkills.canLearnSkill(user, this, skill);
//	}
//
//	public ActionConditionResult canLearnSkill(LivingEntity user, AbstractHamonSkill skill, @Nullable Collection<? extends AbstractHamonSkill> teachersSkills) {
//		return hamonSkills.canLearnSkill(user, this, skill, teachersSkills);
//	}
//
//	public boolean addHamonSkill(LivingEntity user, AbstractHamonSkill skill, boolean checkRequirements, boolean sync) {
//		if (!checkRequirements || !isSkillLearned(skill) && canLearnSkill(user, skill, HamonUtil.nearbyTeachersSkills(power.getUser())).isPositive()) {
//			hamonSkills.addSkill(skill);
//			power.clUpdateHud();
//			serverPlayer.ifPresent(player -> {
//				if (skill == ModHamonSkills.CHEAT_DEATH.get()) {
//					HamonUtil.updateCheatDeathEffect(player);
//				}
//				else if (skill == ModHamonSkills.SATIPOROJA_SCARF.get()
//						&& user.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.onScarfPerk()).orElse(true)) {
//					player.addItem(new ItemStack(ModItems.SATIPOROJA_SCARF.get()));
//				}
//				if (sync) {
//					PacketManager.sendToClient(new HamonSkillAddPacket(skill), (ServerPlayer) player);
//				}
//			});
//			return true;
//		}
//		return false;
//	}
//
//	public void removeHamonSkill(AbstractHamonSkill skill) {
//		if (!skill.isUnlockedByDefault() && isSkillLearned(skill)) {
//			hamonSkills.removeSkill(skill);
//			power.clUpdateHud();
//			serverPlayer.ifPresent(player -> {
//				PacketManager.sendToClient(new HamonSkillRemovePacket(skill), player);
//				if (skill == ModHamonSkills.CHEAT_DEATH.get()) {
//					player.removeEffect(ModStatusEffects.CHEAT_DEATH.get());
//				}
//			});
//		}
//	}
//
//	public static boolean canResetTab(Player user, HamonSkillsTab type) {
//		return user.abilities.instabuild;
//	}
//
//	public void resetHamonSkills(LivingEntity user, HamonSkillsTab type) {
//		if (user instanceof Player && !canResetTab((Player) user, type)) return;
//
//		Stream<? extends AbstractHamonSkill> toReset;
//		switch (type) {
//			case STRENGTH:
//				toReset = JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues().stream()
//				.filter(skill -> skill instanceof BaseHamonSkill && ((BaseHamonSkill) skill).getStat() == HamonStat.STRENGTH);
//				break;
//			case CONTROL:
//				toReset = JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues().stream()
//				.filter(skill -> skill instanceof BaseHamonSkill && ((BaseHamonSkill) skill).getStat() == HamonStat.CONTROL);
//				break;
//			case TECHNIQUE:
//				toReset = JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues().stream()
//				.filter(skill -> !skill.isBaseSkill());
//				break;
//			default:
//				toReset = Stream.empty();
//				break;
//		}
//		toReset.forEach(this::removeHamonSkill);
//		if (type == HamonSkillsTab.TECHNIQUE) {
//			resetCharacterTechnique(user);
//		}
//	}
//
//	public Iterable<AbstractHamonSkill> getLearnedSkills() {
//		return hamonSkills.getLearnedSkills();
//	}
//
//	public void pickHamonTechnique(LivingEntity user, CharacterHamonTechnique technique) {
//		HamonTechniqueManager data = hamonSkills.getTechniqueData();
//		if (data.canPickTechnique(user)) {
//			data.setTechnique(technique);
//			data.addPerks(user, this);
//			if (!user.level.isClientSide()) {
//				PacketManager.sendToClientsTrackingAndSelf(new TrHamonCharacterTechniquePacket(user.getId(), technique, true), user);
//			}
//		}
//	}
//
//	public void resetCharacterTechnique(LivingEntity user) {
//		HamonTechniqueManager data = hamonSkills.getTechniqueData();
//		if (data.getTechnique() != null) {
//			data.resetTechnique();
//			if (!user.level.isClientSide()) {
//				PacketManager.sendToClientsTrackingAndSelf(TrHamonCharacterTechniquePacket.reset(user.getId()), user);
//			}
//		}
//	}
//
//
//
//	@Nullable
//	public CharacterHamonTechnique getCharacterTechnique() {
//		return hamonSkills.getTechniqueData().getTechnique();
//	}
//
//	public boolean characterIs(CharacterHamonTechnique character) {
//		return getCharacterTechnique() == character;
//	}
//
//	public boolean hasTechniqueLevel(int techniqueSkillSlot, boolean clientSide) {
//		if (techniqueSkillSlot > HamonTechniqueManager.techniqueSlotsCount(clientSide)) {
//			return false;
//		}
//		return getHamonStrengthLevel() >= HamonTechniqueManager.techniqueSkillRequirement(techniqueSkillSlot, clientSide)
//				&& getHamonControlLevel() >= HamonTechniqueManager.techniqueSkillRequirement(techniqueSkillSlot, clientSide);
//	}
//
//	public HamonTechniqueManager.Accessor getTechniqueData() {
//		return new HamonTechniqueManager.Accessor(hamonSkills.getTechniqueData());
//	}
}
